package za.ac.pmu.foundation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@WebServlet(name = "RegisterServlet", urlPatterns = {"/RegisterServlet"})
public class RegisterServlet extends HttpServlet {
    
    

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
      
        try {
            // Get form parameters
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");

            // Validate empty fields
            if (fullName == null || fullName.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {

                out.println("<h2>All fields are required!</h2>");
                out.println("<a href='Register.html'>Go Back</a>");
                return;
            }

            // Validate password match
            if (!password.equals(confirmPassword)) {
                out.println("<h2>Passwords do not match!</h2>");
                out.println("<a href='Register.html'>Go Back</a>");
                return;
            }

            // Get database connection
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                out.println("<h2>Database connection failed!</h2>");
                return;
            }

            // Check if email already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE email=?";
            PreparedStatement checkPst = conn.prepareStatement(checkSql);
            checkPst.setString(1, email);
            ResultSet rs = checkPst.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                out.println("<h2>This email is already registered!</h2>");
                out.println("<a href='Register.html'>Go Back</a>");
                rs.close();
                checkPst.close();
                conn.close();
                return;
            }
            rs.close();
            checkPst.close();

            // Insert new user
            String insertSql = "INSERT INTO users (full_name, email, password) VALUES (?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(insertSql);
            pst.setString(1, fullName);
            pst.setString(2, email);
            pst.setString(3, password);

            int rowsInserted = pst.executeUpdate();
            if (rowsInserted > 0) {
                out.println("<h2>Registration successful!</h2>");
                out.println("<a href='Login.html'>Click here to Login</a>");
            } else {
                out.println("<h2>Registration failed. Please try again.</h2>");
                out.println("<a href='Register.html'>Go Back</a>");
            }

            pst.close();
            conn.close();

        } catch (SQLException e) {
            out.println("<h2>Database Error: " + e.getMessage() + "</h2>");
        } finally {
            out.close();
        }
    }
}