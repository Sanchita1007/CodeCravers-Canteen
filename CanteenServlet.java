import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CanteenServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String STUDENT = "STUDENT";
    private static final String ADMIN = "ADMIN";
    private static final String PLACED = "PLACED";
    private static final String ACCEPTED = "ACCEPTED";
    private static final String PREPARING = "PREPARING";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = getAction(request);
        try {
            if ("menu-data".equals(action)) {
                showMenuData(response);
            } else if ("checkout-token".equals(action)) {
                showCheckoutToken(request, response);
            } else if ("register".equals(action)) {
                showAccountForm(request, response, "register", null);
            } else if ("login".equals(action)) {
                showAccountForm(request, response, "login", null);
            } else if ("logout".equals(action)) {
                logout(request, response);
            } else if ("orders".equals(action)) {
                showOrders(request, response);
            } else if ("bill".equals(action)) {
                showBill(request, response);
            } else if ("admin".equals(action)) {
                showAdmin(request, response, null);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException exception) {
            log("Canteen database request failed.", exception);
            sendDatabaseError(response);
        } catch (IllegalStateException exception) {
            log("Canteen database configuration is incomplete.", exception);
            sendDatabaseError(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String action = getAction(request);
        try {
            if ("register".equals(action)) {
                register(request, response);
            } else if ("login".equals(action)) {
                login(request, response);
            } else if ("checkout".equals(action)) {
                checkout(request, response);
            } else if ("admin-food".equals(action)) {
                saveFood(request, response);
            } else if ("admin-availability".equals(action)) {
                updateAvailability(request, response);
            } else if ("admin-status".equals(action)) {
                updateOrderStatus(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException exception) {
            log("Canteen database request failed.", exception);
            sendDatabaseError(response);
        } catch (IllegalStateException exception) {
            log("Canteen database configuration is incomplete.", exception);
            sendDatabaseError(response);
        }
    }

    private String getAction(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.length() < 2) {
            return "";
        }
        return path.substring(1);
    }

    private void showMenuData(HttpServletResponse response) throws SQLException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("[");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT food_id, name, description, price, available "
                             + "FROM food_items ORDER BY name");
             ResultSet results = statement.executeQuery()) {
            boolean first = true;
            while (results.next()) {
                if (!first) {
                    out.print(",");
                }

                first = false;
                out.print("{\"foodId\":" + results.getInt("food_id")
                        + ",\"name\":\"" + json(results.getString("name"))
                        + "\",\"description\":\"" + json(results.getString("description"))
                        + "\",\"price\":" + results.getBigDecimal("price").toPlainString()
                        + ",\"available\":" + (results.getBoolean("available") ? "true" : "false")
                        + "}");
            }
        }
        out.print("]");
    }

    private void showCheckoutToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null
                || !STUDENT.equals(session.getAttribute("userRole"))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Log in with a student account before placing an order.");
            return;
        }

        String csrfToken = (String) session.getAttribute("csrfToken");
        if (csrfToken == null) {
            csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
        }
        String checkoutToken = UUID.randomUUID().toString();
        session.setAttribute("checkoutToken", checkoutToken);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print("{\"csrfToken\":\"" + json(csrfToken)
                + "\",\"checkoutToken\":\"" + json(checkoutToken) + "\"}");
    }

    private void showAccountForm(HttpServletRequest request, HttpServletResponse response,
                                 String type, String error)
            throws IOException {
        String title = "register".equals(type) ? "Student Registration" : "Student Login";
        StringBuilder body = new StringBuilder();
        body.append("<h2>").append(iconMarkup("user-round", "heading-icon"))
                .append(title).append("</h2>");
        appendMessage(body, error);
        body.append("<form method=\"post\" action=\"").append(url(type)).append("\">")
                .append(csrfField(request));

        if ("register".equals(type)) {
            body.append("<label>").append(iconMarkup("user-round", "field-icon"))
                    .append("Name <input name=\"name\" maxlength=\"100\" autocomplete=\"name\" required></label>")
            .append("<label>").append(iconMarkup("mail", "field-icon"))
                    .append("Email <input type=\"email\" name=\"email\" maxlength=\"100\" autocomplete=\"email\" required></label>")
            .append("<label>").append(iconMarkup("lock-keyhole", "field-icon"))
                    .append("Password <input type=\"password\" name=\"password\" minlength=\"8\" autocomplete=\"new-password\" required></label>")
                    .append("<p>Use at least 8 characters for your password.</p>");
        } else {
            body.append("<label>").append(iconMarkup("mail", "field-icon"))
                    .append("Email <input type=\"email\" name=\"email\" maxlength=\"100\" autocomplete=\"email\" required></label>")
            .append("<label>").append(iconMarkup("lock-keyhole", "field-icon"))
                    .append("Password <input type=\"password\" name=\"password\" autocomplete=\"current-password\" required></label>");
        }
        body.append("<button type=\"submit\">")
                .append(iconMarkup("register".equals(type) ? "user-plus" : "log-in", "icon"))
                .append("register".equals(type) ? "Create Account" : "Log In")
                .append("</button></form>");
        if ("register".equals(type)) {
            body.append("<p>Already registered? <a href=\"").append(url("login"))
            .append("\">Log in</a>.</p>");
        } else {
            body.append("<p>New here? <a href=\"").append(url("register"))
            .append("\">Create a student account</a>.</p>");
        }
        body.append("<p><a href=\"../menu.html\">Back to Menu</a></p>");
        writePage(request, response, title, body.toString());
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        if (!validCsrf(request)) {
            showAccountForm(request, response, "register", "The form expired. Please try again.");
            return;
        }

        String name = clean(request.getParameter("name"));
        String email = clean(request.getParameter("email")).toLowerCase();
        String password = request.getParameter("password");
        if (name.isEmpty() || name.length() > 100 || email.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || password == null || password.length() < 8 || password.length() > 200) {
            showAccountForm(request, response, "register",
                    "Enter a name, valid email, and password with at least 8 characters.");
            return;
        }

        try {
            String passwordHash = PasswordUtil.hashPassword(password);
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO users (name, email, password, role) "
                                 + "VALUES (?, ?, ?, 'STUDENT')")) {
                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, passwordHash);
                statement.executeUpdate();
            }
        } catch (GeneralSecurityException exception) {
            log("Password hashing is unavailable.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Account could not be created.");
            return;
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                showAccountForm(request, response, "register",
                        "That email is already registered. Try logging in.");
                return;
            }
            throw exception;
        }

        response.sendRedirect(url("login") + "?registered=1");
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        if (!validCsrf(request)) {
            showAccountForm(request, response, "login", "The form expired. Please try again.");
            return;
        }

        String email = clean(request.getParameter("email")).toLowerCase();
        String password = request.getParameter("password");
        if (email.isEmpty() || password == null) {
            showAccountForm(request, response, "login", "Enter your email and password.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT user_id, name, password, role FROM users WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !PasswordUtil.passwordMatches(
                        password, result.getString("password"))) {
                    showAccountForm(request, response, "login", "Email or password is incorrect.");
                    return;
                }

                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", result.getInt("user_id"));
                session.setAttribute("userName", result.getString("name"));
                session.setAttribute("userRole", result.getString("role"));
                session.setMaxInactiveInterval(30 * 60);

                if (ADMIN.equals(result.getString("role"))) {
                    response.sendRedirect(url("admin"));
                } else {
                    response.sendRedirect("../menu.html");
                }
            }
        } catch (GeneralSecurityException exception) {
            log("Password verification failed.", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Login could not be completed.");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("../index.html");
    }

    private void checkout(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Integer userId = requireRole(request, response, STUDENT);
        if (userId == null) {
            return;
        }
        if (!validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The form expired.");
            return;
        }

        HttpSession session = request.getSession(false);
        String submittedToken = request.getParameter("checkoutToken");
        synchronized (session) {
            String savedToken = (String) session.getAttribute("checkoutToken");
            if (submittedToken == null || !submittedToken.equals(savedToken)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "This order form was already used or expired. Return to your cart.");
                return;
            }
            // Consume the one-use token before any database work to block duplicate submits.
            session.removeAttribute("checkoutToken");
        }

        String[] foodIds = request.getParameterValues("foodId");
        String[] quantities = request.getParameterValues("quantity");
        String paymentMethod = request.getParameter("paymentMethod");
        if (foodIds == null || quantities == null || foodIds.length == 0
                || foodIds.length != quantities.length
                || foodIds.length > 50
                || !("CASH".equals(paymentMethod) || "ONLINE".equals(paymentMethod))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The cart or payment method is invalid.");
            return;
        }

        List<OrderLine> lines = new ArrayList<>();
        for (int i = 0; i < foodIds.length; i++) {
            try {
                int foodId = Integer.parseInt(foodIds[i]);
                int quantity = Integer.parseInt(quantities[i]);
                if (foodId < 1 || quantity < 1 || quantity > 99) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "A cart item has an invalid quantity.");
                    return;
                }
                lines.add(new OrderLine(foodId, quantity));
            } catch (NumberFormatException exception) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "A cart item is invalid.");
                return;
            }
        }

        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            BigDecimal total = BigDecimal.ZERO;

            // Lock selected menu rows and calculate prices on the server.
            for (OrderLine line : lines) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT price, available FROM food_items "
                                + "WHERE food_id = ? FOR UPDATE")) {
                    statement.setInt(1, line.foodId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || !result.getBoolean("available")) {
                            throw new InvalidOrderException(
                                    "A selected menu item is unavailable. Please update your cart.");
                        }
                        line.price = result.getBigDecimal("price");
                        total = total.add(line.price.multiply(BigDecimal.valueOf(line.quantity)));
                    }
                }
            }

            int orderId;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO orders (user_id, total_amount, payment_method, status) "
                            + "VALUES (?, ?, ?, ?) ", Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, userId);
                statement.setBigDecimal(2, total);
                statement.setString(3, paymentMethod);
                statement.setString(4, PLACED);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Database did not return the new order number.");
                    }
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO order_items (order_id, food_id, quantity, price) "
                            + "VALUES (?, ?, ?, ?)")) {
                for (OrderLine line : lines) {
                    statement.setInt(1, orderId);
                    statement.setInt(2, line.foodId);
                    statement.setInt(3, line.quantity);
                    statement.setBigDecimal(4, line.price);
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            connection.commit();
            response.sendRedirect(url("bill") + "?id=" + orderId + "&ordered=1");
        } catch (InvalidOrderException exception) {
            rollback(connection);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (SQLException exception) {
            rollback(connection);
            throw exception;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    log("Could not close a database connection.", exception);
                }
            }
        }
    }

    private void showOrders(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer userId = requireRole(request, response, STUDENT);
        if (userId == null) {
            return;
        }

        StringBuilder body = new StringBuilder("<h2>My Orders</h2>");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT order_id, total_amount, payment_method, status, created_at "
                             + "FROM orders WHERE user_id = ? ORDER BY created_at DESC")) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                body.append("<div class=\"card-list\">");
                boolean found = false;
                while (results.next()) {
                    found = true;
                    int orderId = results.getInt("order_id");
                    body.append("<section class=\"card\"><h3>Order #").append(orderId)
                            .append("</h3><p>Status: ")
                            .append(html(results.getString("status")))
                            .append("</p><p>Total: ₹")
                            .append(results.getBigDecimal("total_amount").toPlainString())
                            .append("</p><p>Payment: ")
                            .append(html(results.getString("payment_method")))
                            .append("</p><p>Placed: ")
                            .append(html(String.valueOf(results.getTimestamp("created_at"))))
                            .append("</p><a href=\"").append(url("bill"))
                            .append("?id=").append(orderId).append("\">View bill</a></section>");
                }
                body.append("</div>");
                if (!found) {
                    body.append("<p>You have not placed an order yet.</p>");
                }
            }
        }
        writePage(request, response, "My Orders", body.toString());
    }

    private void showBill(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer userId = requireRole(request, response, STUDENT);
        if (userId == null) {
            return;
        }

        int orderId = parsePositiveId(request.getParameter("id"));
        if (orderId < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order number.");
            return;
        }

        StringBuilder body = new StringBuilder("<h2>Order Bill</h2>");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement orderStatement = connection.prepareStatement(
                     "SELECT order_id, total_amount, payment_method, status, created_at "
                             + "FROM orders WHERE order_id = ? AND user_id = ?")) {
            orderStatement.setInt(1, orderId);
            orderStatement.setInt(2, userId);
            try (ResultSet order = orderStatement.executeQuery()) {
                if (!order.next()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found.");
                    return;
                }
                body.append("<section class=\"card\"><p>Order #")
                        .append(order.getInt("order_id")).append("</p><p>Status: ")
                        .append(html(order.getString("status"))).append("</p><p>Payment: ")
                        .append(html(order.getString("payment_method"))).append("</p><p>Placed: ")
                        .append(html(String.valueOf(order.getTimestamp("created_at"))))
                        .append("</p></section>");
                body.append("<h3>Items</h3><ul>");
                try (PreparedStatement itemStatement = connection.prepareStatement(
                        "SELECT f.name, oi.quantity, oi.price "
                                + "FROM order_items oi JOIN food_items f ON f.food_id = oi.food_id "
                                + "WHERE oi.order_id = ?")) {
                    itemStatement.setInt(1, orderId);
                    try (ResultSet items = itemStatement.executeQuery()) {
                        while (items.next()) {
                            BigDecimal price = items.getBigDecimal("price");
                            int quantity = items.getInt("quantity");
                            body.append("<li>").append(html(items.getString("name")))
                                    .append(" × ").append(quantity).append(" — ₹")
                                    .append(price.toPlainString()).append(" each; subtotal ₹")
                                    .append(price.multiply(BigDecimal.valueOf(quantity)).toPlainString())
                                    .append("</li>");
                        }
                    }
                }
                body.append("</ul><h3>Total: ₹")
                        .append(order.getBigDecimal("total_amount").toPlainString())
                        .append("</h3><button onclick=\"window.print()\">Print Bill</button>");
            }
        }
        if ("1".equals(request.getParameter("ordered"))) {
            body.append("<script>localStorage.removeItem('cart');</script>");
        }
        writePage(request, response, "Order Bill", body.toString());
    }

    private void showAdmin(HttpServletRequest request, HttpServletResponse response, String message)
            throws SQLException, IOException {
        if (requireRole(request, response, ADMIN) == null) {
            return;
        }

        StringBuilder body = new StringBuilder("<h2>")
                .append(iconMarkup("layout-dashboard", "heading-icon"))
                .append("Admin Dashboard</h2>");
        appendMessage(body, message);
        body.append("<section class=\"card\"><h3>")
                .append(iconMarkup("chef-hat", "heading-icon"))
                .append("Food Management</h3><h4>Add food item</h4>")
                .append("<form method=\"post\" action=\"").append(url("admin-food")).append("\">")
                .append(csrfField(request)).append("<label>Name <input name=\"name\" maxlength=\"100\" required></label>")
                .append("<label>Description <input name=\"description\" maxlength=\"255\"></label>")
                .append("<label>Price (₹) <input name=\"price\" type=\"number\" min=\"0.01\" step=\"0.01\" required></label>")
                .append("<button type=\"submit\">")
                .append(iconMarkup("plus", "icon"))
                .append("Add Food</button></form></section>");

        body.append("<h3>").append(iconMarkup("chef-hat", "heading-icon"))
                .append("Food items</h3><div class=\"card-list\">");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT food_id, name, description, price, available "
                             + "FROM food_items ORDER BY name");
             ResultSet foods = statement.executeQuery()) {
            while (foods.next()) {
                int foodId = foods.getInt("food_id");
                boolean available = foods.getBoolean("available");
                body.append("<section class=\"card\"><h4>")
                        .append(html(foods.getString("name"))).append("</h4><p>₹")
                        .append(foods.getBigDecimal("price").toPlainString())
                        .append(" — ").append(available ? "Available" : "Unavailable")
                        .append("</p><form method=\"post\" action=\"")
                        .append(url("admin-food")).append("\">")
                        .append(csrfField(request)).append("<input type=\"hidden\" name=\"foodId\" value=\"")
                        .append(foodId).append("\"><label>Name <input name=\"name\" maxlength=\"100\" value=\"")
                        .append(html(foods.getString("name"))).append("\" required></label>")
                        .append("<label>Description <input name=\"description\" maxlength=\"255\" value=\"")
                        .append(html(foods.getString("description"))).append("\"></label>")
                        .append("<label>Price (₹) <input name=\"price\" type=\"number\" min=\"0.01\" step=\"0.01\" value=\"")
                        .append(foods.getBigDecimal("price").toPlainString()).append("\" required></label>")
                        .append("<button type=\"submit\">")
                        .append(iconMarkup("check", "icon"))
                        .append("Save Food Details</button></form>")
                        .append("<form method=\"post\" action=\"")
                        .append(url("admin-availability")).append("\">")
                        .append(csrfField(request)).append("<input type=\"hidden\" name=\"foodId\" value=\"")
                        .append(foodId).append("\"><input type=\"hidden\" name=\"available\" value=\"")
                        .append(!available).append("\"><button type=\"submit\">")
                        .append(iconMarkup(available ? "minus" : "check", "icon"))
                        .append(available ? "Mark unavailable" : "Mark available")
                        .append("</button></form></section>");
            }
        }

        body.append("</div><h3>").append(iconMarkup("clipboard-list", "heading-icon"))
                .append("Order Management</h3><h4>Recent orders</h4><div class=\"card-list\">");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT o.order_id, o.total_amount, o.status, o.created_at, u.name "
                             + "FROM orders o JOIN users u ON u.user_id = o.user_id "
                             + "ORDER BY o.created_at DESC LIMIT 50");
             ResultSet orders = statement.executeQuery()) {
            while (orders.next()) {
                int orderId = orders.getInt("order_id");
                body.append("<section class=\"card\"><h4>Order #").append(orderId)
                        .append(" — ").append(html(orders.getString("name")))
                        .append("</h4><p>Total ₹")
                        .append(orders.getBigDecimal("total_amount").toPlainString())
                        .append(" — Status: ").append(html(orders.getString("status")))
                        .append("</p><p>Placed: ")
                        .append(html(String.valueOf(orders.getTimestamp("created_at"))))
                        .append("</p>");
                appendAdminOrderItems(body, connection, orderId);
                appendStatusForm(body, request, orderId);
                body.append("</section>");
            }
        }
        body.append("</div>");
        writePage(request, response, "Admin Dashboard", body.toString());
    }

    private void appendAdminOrderItems(StringBuilder body, Connection connection, int orderId)
            throws SQLException {
        body.append("<ul>");
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT f.name, oi.quantity, oi.price FROM order_items oi "
                        + "JOIN food_items f ON f.food_id = oi.food_id WHERE oi.order_id = ?")) {
            statement.setInt(1, orderId);
            try (ResultSet items = statement.executeQuery()) {
                while (items.next()) {
                    body.append("<li>").append(html(items.getString("name")))
                            .append(" × ").append(items.getInt("quantity"))
                            .append(" at ₹").append(items.getBigDecimal("price").toPlainString())
                            .append("</li>");
                }
            }
        }
        body.append("</ul>");
    }

    private void appendStatusForm(StringBuilder body, HttpServletRequest request, int orderId) {
        body.append("<form method=\"post\" action=\"").append(url("admin-status")).append("\">")
                .append(csrfField(request)).append("<input type=\"hidden\" name=\"orderId\" value=\"")
                .append(orderId).append("\"><label>Update status <select name=\"status\">")
                .append("<option value=\"PLACED\">Placed</option>")
                .append("<option value=\"ACCEPTED\">Accepted</option>")
                .append("<option value=\"PREPARING\">Preparing</option>")
                .append("</select></label><button type=\"submit\">")
                .append(iconMarkup("check", "icon"))
                .append("Save Status</button></form>");
    }

    private void saveFood(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        if (requireRole(request, response, ADMIN) == null) {
            return;
        }
        if (!validCsrf(request)) {
            showAdmin(request, response, "The form expired. Please try again.");
            return;
        }

        String name = clean(request.getParameter("name"));
        String description = clean(request.getParameter("description"));
        String priceText = clean(request.getParameter("price"));
        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException exception) {
            showAdmin(request, response, "Enter a valid food name and price.");
            return;
        }
        if (name.isEmpty() || name.length() > 100 || description.length() > 255
                || price.signum() <= 0 || price.scale() > 2 || price.precision() > 10) {
            showAdmin(request, response, "Check the name, description, and price.");
            return;
        }

        String foodIdText = clean(request.getParameter("foodId"));
        if (foodIdText.isEmpty()) {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO food_items (name, description, price, available) "
                                 + "VALUES (?, ?, ?, 1)")) {
                statement.setString(1, name);
                statement.setString(2, description);
                statement.setBigDecimal(3, price);
                statement.executeUpdate();
            }
        } else {
            int foodId = parsePositiveId(foodIdText);
            if (foodId < 1) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid food item.");
                return;
            }
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE food_items SET name = ?, description = ?, price = ? "
                                 + "WHERE food_id = ?")) {
                statement.setString(1, name);
                statement.setString(2, description);
                statement.setBigDecimal(3, price);
                statement.setInt(4, foodId);
                statement.executeUpdate();
            }
        }
        response.sendRedirect(url("admin"));
    }

    private void updateAvailability(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        if (requireRole(request, response, ADMIN) == null) {
            return;
        }
        if (!validCsrf(request)) {
            showAdmin(request, response, "The form expired. Please try again.");
            return;
        }

        int foodId = parsePositiveId(request.getParameter("foodId"));
        String availability = request.getParameter("available");
        if (foodId < 1 || !("true".equals(availability) || "false".equals(availability))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid food item.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE food_items SET available = ? WHERE food_id = ?")) {
            statement.setBoolean(1, Boolean.parseBoolean(availability));
            statement.setInt(2, foodId);
            statement.executeUpdate();
        }
        response.sendRedirect(url("admin"));
    }

    private void updateOrderStatus(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        if (requireRole(request, response, ADMIN) == null) {
            return;
        }
        if (!validCsrf(request)) {
            showAdmin(request, response, "The form expired. Please try again.");
            return;
        }

        int orderId = parsePositiveId(request.getParameter("orderId"));
        String status = request.getParameter("status");
        if (orderId < 1 || !(PLACED.equals(status)
                || ACCEPTED.equals(status) || PREPARING.equals(status))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order status.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE orders SET status = ? WHERE order_id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
        response.sendRedirect(url("admin"));
    }

    private Integer requireRole(HttpServletRequest request, HttpServletResponse response,
                                String requiredRole) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(url("login"));
            return null;
        }
        if (!requiredRole.equals(session.getAttribute("userRole"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You are not allowed to open this page.");
            return null;
        }
        return (Integer) session.getAttribute("userId");
    }

    private void writePage(HttpServletRequest request, HttpServletResponse response,
                           String title, String body)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        boolean signedIn = session != null && session.getAttribute("userId") != null;
        String role = signedIn ? String.valueOf(session.getAttribute("userRole")) : "";
        String contextPath = getServletContext().getContextPath();
        out.print("<!doctype html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<meta name=\"theme-color\" content=\"#fffaf2\">"
                + "<title>" + html(title) + " - CodeCravers Canteen</title>"
                + "<link rel=\"stylesheet\" href=\"" + html(contextPath)
                + "/site.css\"></head><body class=\"server-page\">"
                + "<header><h1>CodeCravers Canteen</h1><nav aria-label=\"Main navigation\">"
                + navigationLink(contextPath + "/index.html", "home", "Home")
                + navigationLink(contextPath + "/menu.html", "menu", "Menu")
                + navigationLink(contextPath + "/cart.html", "cart", "Cart")
                + (STUDENT.equals(role) ? navigationLink(url("orders"), "orders", "My Orders") : "")
                + (ADMIN.equals(role) ? navigationLink(url("admin"), "admin", "Admin") : "")
                + (!signedIn ? navigationLink(url("login"), "login", "Login")
                        + navigationLink(url("register"), "register", "Register") : "")
                + (signedIn ? navigationLink(url("logout"), "logout", "Logout") : "")
                + "</nav></header><main>" + body + "</main>"
                + "<footer>CodeCravers | Canteen Food Ordering System</footer></body></html>");
    }

    private String navigationLink(String href, String icon, String label) {
        String iconName;
        if ("home".equals(icon)) {
            iconName = "house";
        } else if ("menu".equals(icon)) {
            iconName = "utensils";
        } else if ("cart".equals(icon)) {
            iconName = "shopping-cart";
        } else if ("orders".equals(icon)) {
            iconName = "clipboard-list";
        } else if ("admin".equals(icon)) {
            iconName = "layout-dashboard";
        } else if ("register".equals(icon)) {
            iconName = "user-plus";
        } else if ("logout".equals(icon)) {
            iconName = "log-out";
        } else {
            iconName = "log-in";
        }
        return "<a href=\"" + html(href) + "\">" + iconMarkup(iconName, "nav-icon")
                + html(label) + "</a>";
    }

    private String iconMarkup(String iconName, String className) {
        String iconPath = getServletContext().getContextPath()
                + "/assets/lucide.svg#" + iconName;
        return "<svg class=\"" + html(className)
                + "\" viewBox=\"0 0 24 24\" aria-hidden=\"true\" focusable=\"false\">"
                + "<use href=\"" + html(iconPath) + "\"></use></svg>";
    }

    private String csrfField(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", token);
        }
        return "<input type=\"hidden\" name=\"csrfToken\" value=\"" + html(token) + "\">";
    }

    private boolean validCsrf(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String savedToken = (String) session.getAttribute("csrfToken");
        String submittedToken = request.getParameter("csrfToken");
        return savedToken != null && savedToken.equals(submittedToken);
    }

    private String url(String action) {
        return getServletContext().getContextPath() + "/app/" + action;
    }

    private void appendMessage(StringBuilder body, String message) {
        if (message != null && !message.isEmpty()) {
            body.append("<p class=\"message\" role=\"alert\">")
                    .append(html(message)).append("</p>");
        }
    }

    private String html(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String json(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '\\' || character == '"') {
                escaped.append('\\').append(character);
            } else if (character == '\n') {
                escaped.append("\\n");
            } else if (character == '\r') {
                escaped.append("\\r");
            } else if (character == '\t') {
                escaped.append("\\t");
            } else if (character < 0x20) {
                escaped.append(String.format("\\u%04x", (int) character));
            } else {
                escaped.append(character);
            }
        }
        return escaped.toString();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private int parsePositiveId(String value) {
        try {
            int id = Integer.parseInt(value);
            return id > 0 ? id : -1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void sendDatabaseError(HttpServletResponse response) throws IOException {
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "The database is unavailable. Please try again later.");
        }
    }

    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException exception) {
                log("Could not roll back a failed order.", exception);
            }
        }
    }

    private class OrderLine {
        private final int foodId;
        private final int quantity;
        private BigDecimal price;

        private OrderLine(int foodId, int quantity) {
            this.foodId = foodId;
            this.quantity = quantity;
        }
    }

    private static class InvalidOrderException extends Exception {
        private static final long serialVersionUID = 1L;

        private InvalidOrderException(String message) {
            super(message);
        }
    }
}
