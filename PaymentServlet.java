package za.ac.pmu.foundation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/PaymentServlet"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 10
)
public class PaymentServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.getWriter().println("Please login first.");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String name = request.getParameter("name");      // item_name
        String method = request.getParameter("method");  // type
        double amount;

        try {
            amount = Double.parseDouble(request.getParameter("amount"));
            if (amount <= 0) throw new NumberFormatException("Amount must be positive");
        } catch (NumberFormatException e) {
            response.getWriter().println("Invalid amount entered.");
            return;
        }

        // Handle file upload
        Part filePart = request.getPart("proof");
        String fileName = null;

        if (filePart != null && filePart.getSize() > 0) {
            fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            String uploadPath = "C:/pmu_uploads/" + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File file = new File(uploadDir, fileName);

            try (InputStream input = filePart.getInputStream();
                 FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        }

        // Save donation to DB
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO donations (user_id, type, amount, item_name, proof_file, donated_at) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, userId);
                pst.setString(2, method);
                pst.setDouble(3, amount);
                pst.setString(4, name);
                pst.setString(5, fileName);
                pst.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

                int rows = pst.executeUpdate();
                if (rows > 0) {
                    response.getWriter().println(
                        "<div style='max-width:400px;margin:50px auto;padding:30px;text-align:center;"
                        + "border:2px solid #27ae60;border-radius:12px;background-color:#ecf9f1;'>"
                        + "<h2 style='color:#27ae60;'>Donation Submitted Successfully!</h2>"
                        + "<p>Thank you for your generosity.</p>"
                        + "<a href='DashboardServlet' style='display:inline-block;margin-top:15px;padding:10px 20px;"
                        + "background-color:#27ae60;color:white;text-decoration:none;border-radius:6px;font-weight:bold;'>"
                        + "Go to Dashboard</a></div>"
                    );
                } else {
                    response.getWriter().println("Failed to submit donation.");
                }
            }
        } catch (SQLException e) {
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
}