package za.ac.pmu.foundation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/DashboardServlet"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("Login.html");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        try (Connection conn = DBConnection.getConnection()) {

            // 1️⃣ Total Logins
            int totalLogins = 0;
            try (PreparedStatement pst = conn.prepareStatement(
                    "SELECT COUNT(*) AS logins FROM login_history WHERE user_id=?")) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) totalLogins = rs.getInt("logins");
                }
            }

            // 2️⃣ Total Money Donated
            double totalMoney = 0;
            try (PreparedStatement pst = conn.prepareStatement(
                    "SELECT SUM(amount) AS money_donated FROM donations WHERE user_id=?")) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) totalMoney = rs.getDouble("money_donated");
                }
            }

            // 3️⃣ Total Items Donated
            int totalItems = 0;
            try (PreparedStatement pst = conn.prepareStatement(
                    "SELECT COUNT(*) AS total_items FROM donations WHERE user_id=?")) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) totalItems = rs.getInt("total_items");
                }
            }

            // Format money
            NumberFormat saFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
            String totalMoneyFormatted = saFormatter.format(totalMoney);

            // Start HTML output
            out.println("<html><head><title>Dashboard</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 40px; text-align: center; }");
            out.println(".summary-box { display:inline-block; width:200px; margin:15px; padding:20px; background:white; border-radius:12px; box-shadow:0 0 15px rgba(0,0,0,0.1); }");
            out.println(".summary-box h3 { margin:10px 0; color:#27ae60; }");
            out.println("table { margin:30px auto; border-collapse:collapse; width:90%; background:white; box-shadow:0 0 15px rgba(0,0,0,0.1); }");
            out.println("th, td { padding:12px; border:1px solid #ddd; }");
            out.println("th { background:#1f2d3d; color:white; }");
            out.println("tr:nth-child(even){background:#f9f9f9;} tr:hover{background:#f1f1f1;}");
            out.println(".btn { padding:8px 16px; background:#27ae60; color:white; text-decoration:none; border-radius:6px; display:inline-block; margin:5px; }");
            out.println(".btn:hover { background:#219150; }");
            out.println("</style></head><body>");

            out.println("<h2>Welcome, " + userName + "</h2>");

            // Summary boxes
            out.println("<div class='summary-box'><h3>Total Logins</h3><p>" + totalLogins + "</p></div>");
            out.println("<div class='summary-box'><h3>Total Money Donated</h3><p>" + totalMoneyFormatted + "</p></div>");
            out.println("<div class='summary-box'><h3>Total Donations</h3><p>" + totalItems + "</p></div>");

            // Donations table
            out.println("<h3>Your Donations</h3>");
            out.println("<table>");
            out.println("<tr><th>Type</th><th>Amount</th><th>Item Name</th><th>Proof</th><th>Date</th></tr>");

            String sqlDonations = "SELECT type, amount, item_name, proof_file, donated_at FROM donations WHERE user_id=? ORDER BY donated_at DESC";
            try (PreparedStatement pst = conn.prepareStatement(sqlDonations)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type");
                        double amount = rs.getDouble("amount");
                        String itemName = rs.getString("item_name");
                        String proofFile = rs.getString("proof_file");
                        String date = rs.getTimestamp("donated_at").toString();

                        String proofLink = (proofFile != null && !proofFile.isEmpty())
                                ? "<a class='btn' href='ProofServlet?file=" + proofFile + "' target='_blank'>View Proof</a>"
                                : "No Proof";

                        out.println("<tr>");
                        out.println("<td>" + type + "</td>");
                        out.println("<td>R " + amount + "</td>");
                        out.println("<td>" + itemName + "</td>");
                        out.println("<td>" + proofLink + "</td>");
                        out.println("<td>" + date + "</td>");
                        out.println("</tr>");
                    }
                }
            }

            out.println("</table>");

            // Buttons row: Home, Donate Money, Donate Items, Logout
            out.println("<div>");
            out.println("<a class='btn' href='index.html'>Home</a>");
            out.println("<a class='btn' href='donateMoney.html'>Donate Money</a>");
            out.println("<a class='btn' href='paymentMethods.html'>Donate Items</a>");
            out.println("<a class='btn' href='LogoutServlet'>Logout</a>");
            out.println("</div>");

            out.println("</body></html>");

        } catch (SQLException e) {
            out.println("<h3>Database error: " + e.getMessage() + "</h3>");
        }
    }
}