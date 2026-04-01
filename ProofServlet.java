package za.ac.pmu.foundation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ProofServlet", urlPatterns = {"/ProofServlet"})
public class ProofServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "C:/pmu_uploads/uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fileName = request.getParameter("file");
        if (fileName == null || fileName.isEmpty()) {
            response.getWriter().println("File not specified.");
            return;
        }

        File file = new File(UPLOAD_DIR, fileName);
        if (!file.exists()) {
            response.getWriter().println("File not found.");
            return;
        }

        if (fileName.toLowerCase().endsWith(".pdf")) {
            response.setContentType("application/pdf");
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            response.setContentType("image/jpeg");
        } else if (fileName.toLowerCase().endsWith(".png")) {
            response.setContentType("image/png");
        } else {
            response.setContentType("application/octet-stream");
        }

        response.setContentLengthLong(file.length());

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}