package za.ac.pmu.foundation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    private static final String URL = "jdbc:derby://localhost:1527/phethe_foundation";
    private static final String USER = "app";
    private static final String PASSWORD = "app";

    public static Connection getConnection() {
        Connection conn = null;
        
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("Derby Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
        
        return conn;
    }
    public static void main(String[] args) {
    Connection testConn = getConnection();
    
    if (testConn != null) {
        System.out.println("Test successful!");
    } else {
        System.out.println("Test failed!");
    }
    }
}
