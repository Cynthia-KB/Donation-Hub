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
import javax.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String redirectPage = request.getParameter("redirect");
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String email = request.getParameter("email");
            String password = request.getParameter("password");

            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                out.println("<h2>Email and Password are required!</h2>");
                out.println("<a href='Login.html'>Go Back</a>");
                return;
            }

            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                out.println("<h2>Database connection failed!</h2>");
                return;
            }

            // Get user details + admin flag
            String sql = "SELECT user_id, full_name, is_admin FROM users WHERE email=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                boolean isAdmin = rs.getBoolean("is_admin");

                // Save login history
                PreparedStatement loginHistoryPst = conn.prepareStatement(
                        "INSERT INTO login_history (user_id) VALUES (?)");
                loginHistoryPst.setInt(1, userId);
                loginHistoryPst.executeUpdate();
                loginHistoryPst.close();

                // Create session
                HttpSession session = request.getSession();
                session.setAttribute("userEmail", email);
                session.setAttribute("userName", fullName);
                session.setAttribute("userId", userId);
                session.setAttribute("isAdmin", isAdmin);

                // Debug
                System.out.println("LOGIN SUCCESS");
                System.out.println("Email: " + email);
                System.out.println("User ID: " + userId);
                System.out.println("Name: " + fullName);
                System.out.println("Is Admin: " + isAdmin);

                // Redirect
                if (isAdmin) {
                    response.sendRedirect("AdminDashboardServlet");
                } else {
                    if (redirectPage != null && !redirectPage.isEmpty()) {
                        response.sendRedirect(redirectPage);
                    } else {
                        response.sendRedirect("DashboardServlet");
                    }
                }

            } else {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Login Failed</title>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; background:#f4f4f4; text-align:center; padding:50px; }");
                out.println(".box { background:white; display:inline-block; padding:30px; border-radius:12px; box-shadow:0 0 15px rgba(0,0,0,0.1); }");
                out.println(".btn { display:inline-block; margin-top:20px; padding:12px 24px; background:#27ae60; color:white; text-decoration:none; border-radius:6px; font-weight:bold; }");
                out.println(".btn:hover { background:#219150; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<div class='box'>");
                out.println("<h2>Invalid email or password!</h2>");
                out.println("<a href='Login.html' class='btn'>Try Again</a>");
                out.println("</div>");
                out.println("</body>");
                out.println("</html>");
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            response.getWriter().println("<h2>Database Error: " + e.getMessage() + "</h2>");
        }
    }
}
