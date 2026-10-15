
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HelloServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<body style='font-family:Arial;text-align:center;'>");
        out.println("<h1>CodeCravers Canteen</h1>");
        out.println("<h2>Java Servlet is working!</h2>");
        out.println("<p>Our Java backend is running successfully.</p>");
        out.println("</body>");
        out.println("</html>");
    }
}
