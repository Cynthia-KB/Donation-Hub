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

            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                out.println("<h2>Email and Password are required!</h2>");
                out.println("<a href='Login.html'>Go Back</a>");
                return;
            }

            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                out.println("<h2>Database connection failed!</h2>");
                return;
            }

            String sql = "SELECT user_id, full_name FROM users WHERE email=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");

                PreparedStatement loginHistoryPst = conn.prepareStatement(
                        "INSERT INTO login_history (user_id) VALUES (?)");
                loginHistoryPst.setInt(1, userId);
                loginHistoryPst.executeUpdate();
                loginHistoryPst.close();

                HttpSession session = request.getSession();
                session.setAttribute("userEmail", email);
                session.setAttribute("userName", fullName);
                session.setAttribute("userId", userId);

                // Redirect properly after login
                if (redirectPage != null && !redirectPage.isEmpty()) {
                    response.sendRedirect(redirectPage);
                } else {
                    response.sendRedirect("DashboardServlet");
                }

            } else {
                out.println("<h2>Invalid email or password!</h2>");
                out.println("<a href='Login.html'>Try Again</a>");
            }

            rs.close();
            pst.close();
            conn.close();

        } catch (SQLException e) {
            response.getWriter().println("<h2>Database Error: " + e.getMessage() + "</h2>");
        }
    }
}