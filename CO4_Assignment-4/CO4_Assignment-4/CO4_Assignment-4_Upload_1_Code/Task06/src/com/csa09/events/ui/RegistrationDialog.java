package com.csa09.events.ui;

import com.csa09.events.dao.DaoException;
import com.csa09.events.dao.DuplicateRegistrationException;
import com.csa09.events.service.RegistrationService;
import com.csa09.events.service.ValidationException;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Modal registration form. Validates on the EDT (fast) and saves in a SwingWorker (slow). */
public class RegistrationDialog extends JDialog {
    final JTextField reg = new JTextField(16), name = new JTextField(16), email = new JTextField(16);
    final JComboBox<String> event;
    final JButton save = new JButton("Register"), cancel = new JButton("Cancel");
    final JLabel message = new JLabel(" ");
    private final RegistrationService service;
    private boolean saved;

    public RegistrationDialog(JFrame owner, RegistrationService service, List<String> events) {
        super(owner, "Register for an event", true);
        this.service = service;
        event = new JComboBox<>(events.toArray(new String[0]));
        JPanel form = new JPanel(new GridLayout(4, 2, 6, 8));
        form.add(new JLabel("Register No:")); form.add(reg);
        form.add(new JLabel("Name:")); form.add(name);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Event:")); form.add(event);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(save); buttons.add(cancel);
        JPanel south = new JPanel(new BorderLayout());
        south.add(message, BorderLayout.NORTH); south.add(buttons, BorderLayout.SOUTH);
        JPanel root = new JPanel(new BorderLayout(8, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        root.add(form, BorderLayout.CENTER); root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(save);
        save.addActionListener(e -> submit());
        cancel.addActionListener(e -> dispose());
        pack(); setLocationRelativeTo(owner);
    }

    boolean wasSaved() { return saved; }

    void submit() {
        List<String> problems = service.validate(reg.getText(), name.getText(), email.getText(), (String) event.getSelectedItem());
        if (!problems.isEmpty()) { show("<html>" + String.join("<br>", problems) + "</html>", true); return; }
        save.setEnabled(false); show("Saving...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.register(reg.getText(), name.getText(), email.getText(), (String) event.getSelectedItem());
                return null;
            }
            @Override protected void done() {
                save.setEnabled(true);
                try { get(); saved = true; dispose(); }
                catch (java.util.concurrent.ExecutionException ex) {
                    Throwable c = ex.getCause();
                    if (c instanceof DuplicateRegistrationException) show(c.getMessage(), true);
                    else if (c instanceof ValidationException v) show("<html>" + String.join("<br>", v.problems()) + "</html>", true);
                    else if (c instanceof DaoException) show(c.getMessage(), true);
                    else show("Unexpected error: " + c, true);
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }.execute();
    }

    private void show(String m, boolean err) { message.setForeground(err ? new Color(0xB00020) : new Color(0x1B5E20)); message.setText(m); }
}
