package za.ac.pmu.foundation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "VolunteerServlet", urlPatterns = {"/VolunteerServlet"})
public class VolunteerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("Login.html");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String message = request.getParameter("message");
        LocalDate volunteerDate = LocalDate.now();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "INSERT INTO volunteering (user_id, name, email, message, volunteer_date) "
                           + "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, userId);
                pst.setString(2, name);
                pst.setString(3, email);
                pst.setString(4, message);
                pst.setDate(5, java.sql.Date.valueOf(volunteerDate));
                pst.executeUpdate();
                pst.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Redirect back to dashboard with confirmation
        response.sendRedirect("DashboardServlet");
    }
}