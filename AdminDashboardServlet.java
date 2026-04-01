package za.ac.pmu.foundation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name="AdminDashboardServlet", urlPatterns={"/AdminDashboardServlet"})
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("adminLogin.html");
            return;
        }

        int adminId = (Integer) session.getAttribute("adminId");
        String adminName = (String) session.getAttribute("adminName");

        try (Connection conn = DBConnection.getConnection()) {

            // Total money donated
            double totalMoney = 0;
            try (PreparedStatement pst = conn.prepareStatement("SELECT SUM(amount) AS total FROM donations")) {
                try (ResultSet rs = pst.executeQuery()) {
                    if(rs.next()) totalMoney = rs.getDouble("total");
                }
            }

            // Total items donated
            int totalItems = 0;
            try (PreparedStatement pst = conn.prepareStatement("SELECT SUM(quantity) AS total FROM item_donations")) {
                try (ResultSet rs = pst.executeQuery()) {
                    if(rs.next()) totalItems = rs.getInt("total");
                }
            }

            // All money donations
            String moneySql = "SELECT user_id, type, amount, proof_file, donated_at FROM donations ORDER BY donated_at DESC";

            // All item donations
            String itemSql = "SELECT user_id, item_type, quantity, description, donation_date FROM item_donations ORDER BY donation_date DESC";

            NumberFormat saFormatter = NumberFormat.getCurrencyInstance(new Locale("en","ZA"));
            String totalMoneyFormatted = saFormatter.format(totalMoney);

            out.println("<html><head><title>Admin Dashboard</title>");
            out.println("<style>");
            out.println("body { font-family: Arial; background:#f4f4f4; padding:40px; text-align:center; }");
            out.println(".summary-box { display:inline-block; width:200px; margin:15px; padding:20px; background:white; border-radius:12px; box-shadow:0 0 15px rgba(0,0,0,0.1); }");
            out.println(".summary-box h3 { margin:10px 0; color:#27ae60; }");
            out.println("table { margin:30px auto; border-collapse:collapse; width:90%; background:white; box-shadow:0 0 15px rgba(0,0,0,0.1); }");
            out.println("th, td { padding:12px; border:1px solid #ddd; }");
            out.println("th { background:#1f2d3d; color:white; }");
            out.println("tr:nth-child(even){background:#f9f9f9;} tr:hover{background:#f1f1f1;}");
            out.println(".btn { padding:8px 16px; background:#27ae60; color:white; text-decoration:none; border-radius:6px; }");
            out.println(".btn:hover { background:#219150; }");
            out.println("</style></head><body>");

            out.println("<h2>Welcome, Admin " + adminName + "</h2>");
            out.println("<div class='summary-box'><h3>Total Money Donated</h3><p>" + totalMoneyFormatted + "</p></div>");
            out.println("<div class='summary-box'><h3>Total Items Donated</h3><p>" + totalItems + "</p></div>");

            // Money donations table
            out.println("<h3>Money Donations</h3>");
            out.println("<table>");
            out.println("<tr><th>User ID</th><th>Type</th><th>Amount</th><th>Proof</th><th>Date</th></tr>");
            try (PreparedStatement pst = conn.prepareStatement(moneySql);
                 ResultSet rs = pst.executeQuery()) {
                while(rs.next()) {
                    int userId = rs.getInt("user_id");
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    String proofFile = rs.getString("proof_file");
                    String date = rs.getTimestamp("donated_at").toString();

                    String proofLink = (proofFile != null && !proofFile.isEmpty())
                            ? "<a class='btn' href='ProofServlet?file=" + proofFile + "' target='_blank'>View Proof</a>"
                            : "No Proof";

                    out.println("<tr>");
                    out.println("<td>"+userId+"</td>");
                    out.println("<td>"+type+"</td>");
                    out.println("<td>R "+amount+"</td>");
                    out.println("<td>"+proofLink+"</td>");
                    out.println("<td>"+date+"</td>");
                    out.println("</tr>");
                }
            }

            out.println("</table>");

            // Item donations table
            out.println("<h3>Item Donations</h3>");
            out.println("<table>");
            out.println("<tr><th>User ID</th><th>Item Type</th><th>Quantity</th><th>Description</th><th>Date</th></tr>");
            try (PreparedStatement pst = conn.prepareStatement(itemSql);
                 ResultSet rs = pst.executeQuery()) {
                while(rs.next()) {
                    int userId = rs.getInt("user_id");
                    String type = rs.getString("item_type");
                    int quantity = rs.getInt("quantity");
                    String description = rs.getString("description");
                    String date = rs.getDate("donation_date").toString();

                    out.println("<tr>");
                    out.println("<td>"+userId+"</td>");
                    out.println("<td>"+type+"</td>");
                    out.println("<td>"+quantity+"</td>");
                    out.println("<td>"+description+"</td>");
                    out.println("<td>"+date+"</td>");
                    out.println("</tr>");
                }
            }

            out.println("</table>");
            out.println("<a class='btn' href='LogoutServlet'>Logout</a>");
            out.println("</body></html>");

        } catch(SQLException e) {
            out.println("<h3>Database error: "+e.getMessage()+"</h3>");
        }
    }
}