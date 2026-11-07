package com.csa09.events.ui;

import com.csa09.events.dao.DaoException;
import com.csa09.events.model.Registration;
import com.csa09.events.service.RegistrationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main window. Layout: BorderLayout -> NORTH filter bar, CENTER JTable in JScrollPane, SOUTH pager + status.
 * Every database call runs in a SwingWorker so the window never freezes.
 */
public class MainFrame extends JFrame {
    private static final String ALL = "All events";
    private final RegistrationService service;
    final JComboBox<String> eventFilter = new JComboBox<>(new String[]{ALL});
    final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Register No", "Name", "Email", "Event", "Registered At"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    final JTable table = new JTable(model);
    final JButton prev = new JButton("< Prev"), next = new JButton("Next >"), refresh = new JButton("Refresh");
    final JLabel pageLabel = new JLabel("Page 1 of 1");
    final JLabel status = new JLabel("Ready");
    final JProgressBar busy = new JProgressBar();
    private int page = 0, totalPages = 1, total = 0;
    private boolean updatingFilter = false;
    final Action registerAction = new AbstractAction("Register Student...") {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) { openRegisterDialog(); } };
    final Action exportAction = new AbstractAction("Export CSV...") {
        @Override public void actionPerformed(java.awt.event.ActionEvent e) { exportCsv(); } };

    public MainFrame(RegistrationService service, String modeText) {
        super("Campus Event Registrations" + modeText);
        this.service = service;
        registerAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        exportAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        setJMenuBar(buildMenuBar());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.add(new JLabel("Filter by event:")); top.add(eventFilter); top.add(refresh); top.add(new JButton(registerAction));
        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        pager.add(prev); pager.add(pageLabel); pager.add(next);
        busy.setIndeterminate(true); busy.setVisible(false); busy.setPreferredSize(new Dimension(110, 14));
        JPanel statusBar = new JPanel(new BorderLayout(8, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 8, 4, 8));
        statusBar.add(status, BorderLayout.CENTER); statusBar.add(busy, BorderLayout.EAST);
        JPanel south = new JPanel(new BorderLayout()); south.add(pager, BorderLayout.CENTER); south.add(statusBar, BorderLayout.SOUTH);
        table.setAutoCreateRowSorter(true); table.setRowHeight(22);
        add(top, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER); add(south, BorderLayout.SOUTH);

        eventFilter.addActionListener(e -> { if (!updatingFilter) { page = 0; loadPage(); } });
        refresh.addActionListener(e -> loadPage());
        prev.addActionListener(e -> { if (page > 0) { page--; loadPage(); } });
        next.addActionListener(e -> { if (page + 1 < totalPages) { page++; loadPage(); } });
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 520); setLocationRelativeTo(null);
        loadEventsThenPage();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File"); file.setMnemonic('F');
        file.add(new JMenuItem(registerAction)); file.add(new JMenuItem(exportAction)); file.addSeparator();
        JMenuItem exit = new JMenuItem("Exit"); exit.addActionListener(e -> dispose()); file.add(exit);
        JMenu view = new JMenu("View"); view.setMnemonic('V');
        JMenuItem rf = new JMenuItem("Refresh"); rf.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); rf.addActionListener(e -> loadPage()); view.add(rf);
        JMenu help = new JMenu("Help"); help.setMnemonic('H');
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Campus Event Registration\nCSA09 - Assignment 4 (Task 6)\nSwing + JDBC, layered design", "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);
        bar.add(file); bar.add(view); bar.add(help);
        return bar;
    }

    private String selectedEvent() { String s = (String) eventFilter.getSelectedItem(); return ALL.equals(s) ? null : s; }

    private void setBusy(boolean b, String msg) {
        busy.setVisible(b); prev.setEnabled(!b && page > 0); next.setEnabled(!b && page + 1 < totalPages);
        refresh.setEnabled(!b); eventFilter.setEnabled(!b); status.setText(msg);
    }

    private void loadEventsThenPage() {
        setBusy(true, "Loading events...");
        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try {
                    updatingFilter = true;
                    for (String s : get()) eventFilter.addItem(s);
                    updatingFilter = false;
                    loadPage();
                } catch (Exception ex) { setBusy(false, "Error: " + root(ex).getMessage()); }
            }
        }.execute();
    }

    /** Loads ONE page (LIMIT/OFFSET) in a background thread; updates the table on the EDT in done(). */
    void loadPage() {
        final String ev = selectedEvent(); final int pg = page;
        setBusy(true, "Loading page " + (pg + 1) + "...");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception { return new Object[]{service.total(ev), service.page(ev, pg)}; }
            @Override @SuppressWarnings("unchecked") protected void done() {
                try {
                    Object[] r = get();
                    total = (Integer) r[0];
                    totalPages = Math.max(1, (int) Math.ceil(total / (double) RegistrationService.PAGE_SIZE));
                    model.setRowCount(0);
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
                    for (Registration x : (List<Registration>) r[1])
                        model.addRow(new Object[]{x.id(), x.studentReg(), x.studentName(), x.email(), x.eventName(), x.registeredAt().format(f)});
                    pageLabel.setText("Page " + (page + 1) + " of " + totalPages + "  (" + total + " registrations)");
                    setBusy(false, "Loaded " + model.getRowCount() + " row(s)" + (ev == null ? "." : " for " + ev + "."));
                } catch (Exception ex) { setBusy(false, "Error: " + root(ex).getMessage()); }
            }
        }.execute();
    }

    private void openRegisterDialog() {
        try {
            List<String> ev = new java.util.ArrayList<>();
            for (int i = 1; i < eventFilter.getItemCount(); i++) ev.add(eventFilter.getItemAt(i));
            RegistrationDialog d = new RegistrationDialog(this, service, ev);
            d.setVisible(true);
            if (d.wasSaved()) { page = 0; loadPage(); status.setText("Registration saved."); }
        } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("registrations.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final Path p = fc.getSelectedFile().toPath(); final String ev = selectedEvent();
        setBusy(true, "Exporting...");
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception { return service.exportCsv(p, ev); }
            @Override protected void done() {
                try { setBusy(false, "Exported " + get() + " row(s) to " + p.getFileName()); }
                catch (Exception ex) { setBusy(false, "Export failed: " + root(ex).getMessage()); }
            }
        }.execute();
    }

    private static Throwable root(Throwable t) { while (t.getCause() != null) t = t.getCause(); return t; }
}
