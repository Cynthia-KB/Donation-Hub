package za.ac.pmu.foundation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet(name = "DonateItemsServlet", urlPatterns = {"/DonateItemsServlet"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 10
)
public class DonateItemsServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "C:\\pmu_uploads\\uploads"; // make sure this folder exists

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.sendRedirect("Login.html?redirect=donateItems.html");
                return;
            }

            int userId = (Integer) session.getAttribute("userId");

            String itemType = request.getParameter("itemType");
            String quantityStr = request.getParameter("quantity");
            String description = request.getParameter("description");
            String deliveryMethod = request.getParameter("deliveryMethod");
            String donationDate = request.getParameter("donationDate");

            if (itemType == null || itemType.isEmpty() ||
                quantityStr == null || quantityStr.isEmpty() ||
                deliveryMethod == null || deliveryMethod.isEmpty() ||
                donationDate == null || donationDate.isEmpty()) {

                out.println("<h2>All required fields must be filled in!</h2>");
                out.println("<a href='donateItems.html'>Go Back</a>");
                return;
            }

            int quantity = Integer.parseInt(quantityStr);

            // Handle proof file safely
            Part filePart = null;
            String proofFile = null;
            try {
                filePart = request.getPart("proof");
            } catch (Exception e) {
                // No file uploaded, ignore
            }

            if (filePart != null && filePart.getSize() > 0) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                proofFile = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                filePart.write(UPLOAD_DIR + File.separator + proofFile);
            }

            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    out.println("<h2>Database connection failed!</h2>");
                    return;
                }

                // Insert into donations table for dashboard counting
                String sqlDonations = "INSERT INTO donations (user_id, type, amount, item_name, proof_file, donated_at) VALUES (?, 'Item', ?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement pstDonations = conn.prepareStatement(sqlDonations)) {
                    for (int i = 0; i < quantity; i++) {
                        pstDonations.setInt(1, userId);
                        pstDonations.setDouble(2, 0); // amount for items
                        pstDonations.setString(3, itemType + (description != null && !description.isEmpty() ? " - " + description : ""));
                        pstDonations.setString(4, proofFile);
                        pstDonations.executeUpdate();
                    }
                }

                // Insert into item_donations table
                String sqlItem = "INSERT INTO item_donations (user_id, item_type, quantity, description, delivery_method, donation_date) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstItem = conn.prepareStatement(sqlItem)) {
                    pstItem.setInt(1, userId);
                    pstItem.setString(2, itemType);
                    pstItem.setInt(3, quantity);
                    pstItem.setString(4, description);
                    pstItem.setString(5, deliveryMethod);
                    pstItem.setDate(6, java.sql.Date.valueOf(donationDate));
                    pstItem.executeUpdate();
                }
            }

            // Success page
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Donation Successful</title>");
            out.println("<style>");
            out.println("body { font-family: Arial; text-align: center; padding: 50px; background: #f4f4f4; }");
            out.println(".box { background: white; padding: 30px; border-radius: 12px; display: inline-block; box-shadow: 0 0 15px rgba(0,0,0,0.1); }");
            out.println(".btn { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #27ae60; color: white; text-decoration: none; border-radius: 6px; font-weight: bold; }");
            out.println(".btn:hover { background-color: #219150; }");
            out.println("</style></head><body>");
            out.println("<div class='box'>");
            out.println("<h2>Thank you! Your item donation has been submitted successfully.</h2>");
            out.println("<a href='DashboardServlet' class='btn'>Go to Dashboard</a>");
            out.println("</div></body></html>");

        } catch (SQLException e) {
            out.println("<h2>Database Error: " + e.getMessage() + "</h2>");
        } catch (NumberFormatException e) {
            out.println("<h2>Quantity must be a valid number.</h2>");
        } catch (Exception e) {
            out.println("<h2>Unexpected error: " + e.getMessage() + "</h2>");
        } finally {
            out.close();
        }
    }
}