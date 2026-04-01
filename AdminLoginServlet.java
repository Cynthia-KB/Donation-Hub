package za.ac.pmu.foundation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name="AdminLoginServlet", urlPatterns={"/AdminLoginServlet"})
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password"); // ideally hashed

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name FROM admins WHERE username=? AND password=?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, username);
                pst.setString(2, password);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        HttpSession session = request.getSession();
                        session.setAttribute("adminId", rs.getInt("id"));
                        session.setAttribute("adminName", rs.getString("name"));
                        response.sendRedirect("AdminDashboardServlet");
                    } else {
                        out.println("<h2>Invalid admin credentials.</h2>");
                        out.println("<a href='adminLogin.html'>Try Again</a>");
                    }
                }
            }
        } catch (SQLException e) {
            out.println("<h2>Database error: " + e.getMessage() + "</h2>");
        }
    }
}