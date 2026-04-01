package za.ac.pmu.foundation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "ContactServlet", urlPatterns = {"/ContactServlet"})
public class ContactServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.sendRedirect("Login.html");
                return;
            }

            int userId = (Integer) session.getAttribute("userId");
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String message = request.getParameter("message");

            if (name == null || email == null || message == null ||
                name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                out.println("<h3>All fields are required!</h3>");
                return;
            }

            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "INSERT INTO volunteering (user_id, name, email, message, volunteer_date) "
                           + "VALUES (?, ?, ?, ?, CURRENT_DATE)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, userId);
                pst.setString(2, name);
                pst.setString(3, email);
                pst.setString(4, message);
                pst.executeUpdate();
                pst.close();
                conn.close();
            }

            // Thank-you page
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Thank You - Phethelo Molapo Foundation</title>");
            out.println("<style>");
            out.println("body { font-family: Arial; text-align: center; padding: 50px; background:#f4f4f4; }");
            out.println(".message-box { background:white; display:inline-block; padding:30px; border-radius:12px; box-shadow:0 0 15px rgba(0,0,0,0.1); }");
            out.println(".btn { display:inline-block; margin-top:20px; padding:12px 24px; background-color:#27ae60; color:white; text-decoration:none; border-radius:6px; font-weight:bold; }");
            out.println(".btn:hover { background-color:#219150; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='message-box'>");
            out.println("<h2>Thank you, " + name + "!</h2>");
            out.println("<p>Your message has been sent successfully.</p>");
            out.println("<a href='DashboardServlet' class='btn'>Go to Dashboard</a>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");

        } catch (SQLException e) {
            out.println("<h3>Database Error: " + e.getMessage() + "</h3>");
        } finally {
            out.close();
        }
    }
}