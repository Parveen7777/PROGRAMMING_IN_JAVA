import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Campus Library Search Screen (AWT).
 *
 * Frame                      BorderLayout
 *  |- NORTH  northPanel      BorderLayout
 *  |    |- NORTH  title Label
 *  |    |- CENTER searchRow  FlowLayout(LEFT)  [Label][TextField][Choice][Search]
 *  |- CENTER results (java.awt.List)           <- the only region that stretches
 *  |- SOUTH  southPanel      BorderLayout
 *       |- CENTER buttonRow  FlowLayout(CENTER)[Prev][Next][Reserve]
 *       |- SOUTH  status Label
 */
public class LibrarySearch extends Frame {
    private static final int PAGE = 6;
    private final TextField query = new TextField(24);
    private final Choice category = new Choice();
    private final Button search = new Button("Search");
    private final Button prev = new Button("Prev"), next = new Button("Next"), reserve = new Button("Reserve");
    private final java.awt.List results = new java.awt.List(10);
    private final Label status = new Label("Enter a title or author and press Search.");
    private final List<String[]> catalogue = new ArrayList<>();   // {title, author, category}
    private List<String[]> hits = new ArrayList<>();
    private int page = 0;

    public LibrarySearch() {
        super("Campus Library - Search");
        seed();
        setLayout(new BorderLayout());

        Label title = new Label("CAMPUS LIBRARY CATALOGUE", Label.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBackground(new Color(0x1F3A5F)); title.setForeground(Color.WHITE);

        Panel searchRow = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        for (String c : new String[]{"All", "Computer Science", "Mathematics", "Physics", "Literature"}) category.add(c);
        searchRow.add(new Label("Search:")); searchRow.add(query); searchRow.add(category); searchRow.add(search);
        Panel north = new Panel(new BorderLayout());
        north.add(title, BorderLayout.NORTH); north.add(searchRow, BorderLayout.CENTER);

        Panel buttonRow = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttonRow.add(prev); buttonRow.add(next); buttonRow.add(reserve);
        Panel south = new Panel(new BorderLayout());
        south.add(buttonRow, BorderLayout.CENTER); south.add(status, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH); add(results, BorderLayout.CENTER); add(south, BorderLayout.SOUTH);

        search.addActionListener(e -> doSearch());
        query.addActionListener(e -> doSearch());
        prev.addActionListener(e -> { if (page > 0) { page--; showPage(); } });
        next.addActionListener(e -> { if ((page + 1) * PAGE < hits.size()) { page++; showPage(); } });
        reserve.addActionListener(e -> reserveSelected());
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        setSize(640, 420);
        setMinimumSize(new Dimension(520, 300));
    }

    private void doSearch() {
        String q = query.getText().trim().toLowerCase(), cat = category.getSelectedItem();
        hits = new ArrayList<>();
        for (String[] b : catalogue)
            if ((cat.equals("All") || b[2].equals(cat)) && (q.isEmpty() || b[0].toLowerCase().contains(q) || b[1].toLowerCase().contains(q)))
                hits.add(b);
        page = 0; showPage();
    }

    private void showPage() {
        results.removeAll();
        int from = page * PAGE, to = Math.min(from + PAGE, hits.size());
        for (int i = from; i < to; i++) results.add(hits.get(i)[0] + "  -  " + hits.get(i)[1]);
        prev.setEnabled(page > 0); next.setEnabled(to < hits.size());
        status.setText(hits.isEmpty() ? "No books found." : "Showing " + (from + 1) + "-" + to + " of " + hits.size() + " result(s).");
    }

    private void reserveSelected() {
        int i = results.getSelectedIndex();
        status.setText(i < 0 ? "Select a book first, then press Reserve." : "Reserved: " + results.getItem(i));
    }

    private void seed() {
        String[][] d = {
            {"Java: The Complete Reference", "Herbert Schildt", "Computer Science"}, {"Effective Java", "Joshua Bloch", "Computer Science"},
            {"Core Java Volume I", "Cay Horstmann", "Computer Science"}, {"Introduction to Algorithms", "Cormen et al.", "Computer Science"},
            {"Database System Concepts", "Silberschatz", "Computer Science"}, {"Operating System Concepts", "Galvin", "Computer Science"},
            {"Computer Networks", "Tanenbaum", "Computer Science"}, {"Clean Code", "Robert C. Martin", "Computer Science"},
            {"Calculus", "James Stewart", "Mathematics"}, {"Linear Algebra Done Right", "Sheldon Axler", "Mathematics"},
            {"Concepts of Physics", "H. C. Verma", "Physics"}, {"Fundamentals of Physics", "Halliday & Resnick", "Physics"},
            {"Wings of Fire", "A. P. J. Abdul Kalam", "Literature"}, {"The Guide", "R. K. Narayan", "Literature"}};
        for (String[] b : d) catalogue.add(b);
    }

    public static void main(String[] args) { new LibrarySearch().setVisible(true); }
}
