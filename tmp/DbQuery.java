import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DbQuery {
    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://35.225.67.182:3306/sindicato?zeroDateTimeBehavior=convertToNull&autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false",
                "sindicatoindependencia",
                "hvf850K#");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(args[0])) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) {
                    System.out.print("\t");
                }
                System.out.print(meta.getColumnLabel(i));
            }
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        System.out.print("\t");
                    }
                    System.out.print(rs.getString(i));
                }
                System.out.println();
            }
        }
    }
}
