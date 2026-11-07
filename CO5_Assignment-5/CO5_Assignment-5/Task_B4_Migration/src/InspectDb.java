import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/** B4.2 - report database product, driver version, tables, columns and capabilities. */
public class InspectDb {
    public static void main(String[] args) throws Exception {
        try (Connection con = DbConfig.connect()) {
            DatabaseMetaData md = con.getMetaData();
            System.out.println("Database product   : " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            System.out.println("Driver             : " + md.getDriverName() + " " + md.getDriverVersion());
            System.out.println("JDBC spec version  : " + md.getJDBCMajorVersion() + "." + md.getJDBCMinorVersion());
            System.out.println("Connected URL/user : " + md.getURL() + " / " + md.getUserName());
            System.out.println("Supports transactions  : " + md.supportsTransactions());
            System.out.println("Supports batch updates : " + md.supportsBatchUpdates());
            System.out.println("Max connections        : " + md.getMaxConnections() + " (0 = no fixed limit reported)");
            System.out.println("\nTables in catalog '" + con.getCatalog() + "':");
            try (ResultSet rs = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) System.out.println("   " + rs.getString("TABLE_NAME") + "  (" + rs.getString("TABLE_TYPE") + ")");
            }
            System.out.println("\nColumns of bus_pass:");
            try (ResultSet rs = md.getColumns(con.getCatalog(), null, "bus_pass", "%")) {
                while (rs.next())
                    System.out.printf("   %-10s %-10s size=%-4d nullable=%s%n", rs.getString("COLUMN_NAME"),
                            rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE"), rs.getString("IS_NULLABLE"));
            }
            System.out.println("\nPrimary key of bus_pass:");
            try (ResultSet rs = md.getPrimaryKeys(con.getCatalog(), null, "bus_pass")) {
                while (rs.next()) System.out.println("   " + rs.getString("COLUMN_NAME") + " (key seq " + rs.getShort("KEY_SEQ") + ")");
            }
        }
    }
}
