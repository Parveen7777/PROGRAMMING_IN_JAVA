import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

/** B3.1 - insert 10 records with each approach and verify content and column types. */
public class B3Correctness {
    interface Loader { int load(Connection c, List<String[]> p) throws Exception; }

    static String snapshot(Connection con) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bus_pass ORDER BY pass_id")) {
            while (rs.next())
                sb.append(rs.getString(1)).append('|').append(rs.getString(2)).append('|')
                  .append(rs.getString(3)).append('|').append(rs.getInt(4)).append('|')
                  .append(rs.getBigDecimal(5)).append('\n');
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        List<String[]> data = PassGenerator.generate(10);
        String[] names = {"Statement", "PreparedStatement", "Prepared + addBatch (auto-commit)",
                          "Prepared batch in one transaction (PassLoader.loadAll)"};
        Loader[] loaders = {
                LoaderVariants::statementAutoCommit,
                LoaderVariants::preparedAutoCommit,
                LoaderVariants::batchAutoCommit,
                PassLoader::loadAll};
        String reference = null;
        try (Connection con = DbConfig.connect()) {
            for (int i = 0; i < loaders.length; i++) {
                try (Statement st = con.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }
                int n = loaders[i].load(con, data);
                String snap = snapshot(con);
                if (reference == null) reference = snap;
                System.out.printf("%-58s loaded=%d  rows in table=%d  identical to Statement result: %s%n",
                        names[i], n, snap.split("\n").length, snap.equals(reference));
            }
            System.out.println("\nTable contents (last run):");
            System.out.print(snapshot(con));

            System.out.println("\nResultSetMetaData of: SELECT * FROM bus_pass");
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM bus_pass")) {
                ResultSetMetaData md = rs.getMetaData();
                System.out.printf("%-4s %-10s %-12s %-10s %-10s %-8s%n", "#", "column", "SQL type", "size", "scale", "nullable");
                for (int c = 1; c <= md.getColumnCount(); c++)
                    System.out.printf("%-4d %-10s %-12s %-10d %-10d %-8s%n", c, md.getColumnName(c),
                            md.getColumnTypeName(c), md.getPrecision(c), md.getScale(c),
                            md.isNullable(c) == ResultSetMetaData.columnNoNulls ? "NOT NULL" : "NULL");
            }
        }
    }
}
