import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.ss.usermodel.Workbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class CashierFrame extends JFrame {
    private JTable customersTable, ordersTable;
    private JTextField searchField;
    private JButton searchButton, addCustomerButton, editCustomerButton, deleteCustomerButton, addOrderButton;
    private void exportToJson(JTable productTable) {
        try {
            FileWriter writer = new FileWriter("product.json");
            JsonArray jsonArray = new JsonArray();

            // Пример: Для каждой строки в таблице получаем данные и конвертируем их в JSON
            for (int i = 0; i < productTable.getRowCount(); i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("order_id", (Integer) productTable.getValueAt(i, 0));
                jsonObject.addProperty("customer", (String) productTable.getValueAt(i, 1));
                jsonObject.addProperty("employee", (String) productTable.getValueAt(i, 2));
                jsonObject.addProperty("products", (String) productTable.getValueAt(i, 3));
                jsonObject.addProperty("total_amount", (Double) productTable.getValueAt(i, 4));
                // добавляем другие столбцы по аналогии

                jsonArray.add(jsonObject);
            }

            writer.write(jsonArray.toString());
            writer.close();
            JOptionPane.showMessageDialog(null, "Data exported to JSON successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportToXls(JTable productTable) {
        try (Workbook workbook = new HSSFWorkbook()  ) {
            Sheet sheet = workbook.createSheet("Data");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < productTable.getColumnCount(); i++) {
                headerRow.createCell(i).setCellValue(productTable.getColumnName(i));
            }
            for (int i = 0; i < productTable.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < productTable.getColumnCount(); j++) {
                    row.createCell(j).setCellValue(productTable.getValueAt(i, j).toString());
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream("product.xlsx")) {
                workbook.write(fileOut);
            }

            JOptionPane.showMessageDialog(null, "Data exported to XLS successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public CashierFrame() {
        setTitle("Cashier Panel - Sweet Waffles");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Верхняя панель: Поиск
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel searchLabel = new JLabel("Search Customer:");
        searchField = new JTextField();
        searchButton = new JButton("Search");
        topPanel.add(searchLabel, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Панель вкладок
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка "Клиенты"
        JPanel customersPanel = createCustomersPanel();
        tabbedPane.addTab("Customers", customersPanel);

        // Вкладка "Заказы"
        JPanel ordersPanel = createOrdersPanel();
        tabbedPane.addTab("Orders", ordersPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Нижняя панель: Действия
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Действия кнопок
        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Logged out!");
            dispose();
        });

        // Загрузка данных при запуске
        loadCustomersData();
        loadOrdersData();
    }

    private JPanel createCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Таблица клиентов
        customersTable = new JTable(new DefaultTableModel(
                new Object[]{"ID", "First Name", "Last Name", "Phone"}, 0
        ));
        JScrollPane tableScrollPane = new JScrollPane(customersTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Панель действий
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addCustomerButton = new JButton("Add Customer");
        editCustomerButton = new JButton("Edit Customer");
        deleteCustomerButton = new JButton("Delete Customer");
        actionsPanel.add(addCustomerButton);
        actionsPanel.add(editCustomerButton);
        actionsPanel.add(deleteCustomerButton);
        panel.add(actionsPanel, BorderLayout.SOUTH);

        // Действия кнопок
        addCustomerButton.addActionListener(e -> addCustomer());
        editCustomerButton.addActionListener(e -> editCustomer());
        deleteCustomerButton.addActionListener(e -> deleteCustomer());
        searchButton.addActionListener(e -> searchCustomers());

        return panel;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Таблица заказов
        ordersTable = new JTable(new DefaultTableModel(
                new Object[]{"Order ID", "Customer", "Employee", "Products", "Total Amount"}, 0
        ));
        JScrollPane tableScrollPane = new JScrollPane(ordersTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Кнопка "Add Order"
        JButton exportButton = new JButton("ExportToXls");
        exportButton.addActionListener(e -> exportToXls(ordersTable));
        JButton exportToJson = new JButton("ExportToJson");
        exportToJson.addActionListener(e -> exportToJson(ordersTable));
        JButton addButton = new JButton("Add Order");

// Создаем панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Кнопки будут расположены по центру
        buttonPanel.add(addButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(exportToJson);

// Добавляем панель с кнопками в SOUTH
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            try (Connection connection = getConnection()) {
                // Получаем список продуктов
                String productQuery = "SELECT id, name, price FROM products";
                DefaultTableModel productTableModel = new DefaultTableModel(new Object[]{"Select", "ID", "Product Name", "Price", "Quantity"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        // Только колонки "Select" и "Quantity" редактируемы
                        return column == 0 || column == 4;
                    }

                    @Override
                    public Class<?> getColumnClass(int column) {
                        return column == 0 ? Boolean.class : super.getColumnClass(column);
                    }
                };

                try (PreparedStatement productStatement = connection.prepareStatement(productQuery);
                     ResultSet productResultSet = productStatement.executeQuery()) {
                    while (productResultSet.next()) {
                        int id = productResultSet.getInt("id");
                        String name = productResultSet.getString("name");
                        double price = productResultSet.getDouble("price");

                        productTableModel.addRow(new Object[]{false, id, name, price, 1});
                    }
                }

                JTable productTable = new JTable(productTableModel);
                JScrollPane productScrollPane = new JScrollPane(productTable);

                // Обновляем сумму при изменении таблицы
                JLabel totalAmountLabel = new JLabel("Total Amount: $0.00");
                productTableModel.addTableModelListener(event -> {
                    double total = 0.0;
                    for (int i = 0; i < productTableModel.getRowCount(); i++) {
                        boolean isSelected = (Boolean) productTableModel.getValueAt(i, 0);
                        if (isSelected) {
                            double price = (Double) productTableModel.getValueAt(i, 3);
                            System.out.println(productTableModel.getValueAt(i, 4));
                            String strQuantity = productTableModel.getValueAt(i, 4).toString();
                            int quantity = Integer.parseInt(strQuantity);
                            total += price * quantity;
                        }
                    }
                    totalAmountLabel.setText("Total Amount: $" + total);
                });

                // Получаем список заказчиков
                String customerQuery = "SELECT id, first_name || ' ' || last_name AS full_name FROM customers";
                JComboBox<String> customerComboBox = new JComboBox<>();
                try (PreparedStatement customerStatement = connection.prepareStatement(customerQuery);
                     ResultSet customerResultSet = customerStatement.executeQuery()) {
                    while (customerResultSet.next()) {
                        int id = customerResultSet.getInt("id");
                        String name = customerResultSet.getString("full_name");
                        customerComboBox.addItem(id + " - " + name);
                    }
                }

                // Диалог для ввода данных
                Object[] message = {
                        "Select Products:", productScrollPane,
                        "Select Customer:", customerComboBox,
                        totalAmountLabel
                };

                int option = JOptionPane.showConfirmDialog(this, message, "Add Order", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    // Получаем ID выбранного клиента
                    String selectedCustomer = (String) customerComboBox.getSelectedItem();
                    int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);

                    // Сохраняем данные в базе
                    String insertOrderQuery = "INSERT INTO orders (customer_id, employee_id, total_amount) VALUES (?, ?, ?) RETURNING id";
                    String insertOrderItemQuery = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, 100)";
                    try (PreparedStatement orderStatement = connection.prepareStatement(insertOrderQuery);
                         PreparedStatement orderItemStatement = connection.prepareStatement(insertOrderItemQuery)) {

                        // Вставляем заказ
                        double totalAmount = Double.parseDouble(totalAmountLabel.getText().replace("Total Amount: $", ""));
                        orderStatement.setInt(1, customerId);
                        orderStatement.setInt(2, 1); // ID сотрудника — можно сделать динамическим
                        orderStatement.setDouble(3, totalAmount);

                        ResultSet rs = orderStatement.executeQuery();
                        int orderId = 0;
                        if (rs.next()) {
                            orderId = rs.getInt("id");
                        }

                        // Вставляем элементы заказа
                        for (int i = 0; i < productTableModel.getRowCount(); i++) {
                            boolean isSelected = (Boolean) productTableModel.getValueAt(i, 0);
                            if (isSelected) {
                                int productId = (Integer) productTableModel.getValueAt(i, 1);
                                int quantity = (Integer) productTableModel.getValueAt(i, 4);

                                orderItemStatement.setInt(1, orderId);
                                orderItemStatement.setInt(2, productId);
                                orderItemStatement.setInt(3, quantity);
                                orderItemStatement.addBatch();
                            }
                        }
                        orderItemStatement.executeBatch();

                        JOptionPane.showMessageDialog(this, "Order added successfully!");
                        loadOrdersData(); // Обновляем таблицу заказов
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding order: " + ex.getMessage());
            }
        });


        return panel;
    }


    private Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/sweet_waffles";
        String username = "postgres";
        String password = "postgres";
        return DriverManager.getConnection(url, username, password);
    }

    private void loadCustomersData() {
        DefaultTableModel model = (DefaultTableModel) customersTable.getModel();
        model.setRowCount(0);

        String query = "SELECT id, first_name, last_name, phone_number FROM customers";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String phone = resultSet.getString("phone_number");

                model.addRow(new Object[]{id, firstName, lastName, phone});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }

    private void loadOrdersData() {
        DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
        model.setRowCount(0);

        String query = """
                SELECT o.id, c.first_name AS customer, e.first_name AS employee, 
                       ARRAY_AGG(p.name) AS products, o.total_amount
                FROM orders o
                JOIN employees e ON e.id = o.employee_id
                JOIN customers c ON c.id = o.customer_id
                JOIN order_items oi ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                GROUP BY o.id, c.first_name, e.first_name, o.total_amount
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int orderId = resultSet.getInt("id");
                String customer = resultSet.getString("customer");
                String employee = resultSet.getString("employee");
                String products = resultSet.getArray("products").toString();
                double totalAmount = resultSet.getDouble("total_amount");

                model.addRow(new Object[]{orderId, customer, employee, products, totalAmount});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }

    private void addCustomer() {
        String firstName = JOptionPane.showInputDialog(this, "Enter First Name:");
        String lastName = JOptionPane.showInputDialog(this, "Enter Last Name:");
        String phone = JOptionPane.showInputDialog(this, "Enter Phone:");

        if (firstName != null && lastName != null && phone != null) {
            String query = "INSERT INTO customers (first_name, last_name, phone) VALUES (?, ?, ?)";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, phone);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                loadCustomersData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding customer: " + e.getMessage());
            }
        }
    }

    private void editCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
            return;
        }

        int id = (int) customersTable.getValueAt(selectedRow, 0);
        String newFirstName = JOptionPane.showInputDialog(this, "Enter new First Name:");
        String newLastName = JOptionPane.showInputDialog(this, "Enter new Last Name:");
        String newPhone = JOptionPane.showInputDialog(this, "Enter new Phone:");

        if (newFirstName != null && newLastName != null && newPhone != null) {
            String query = "UPDATE customers SET first_name = ?, last_name = ?, phone = ? WHERE id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, newFirstName);
                statement.setString(2, newLastName);
                statement.setString(3, newPhone);
                statement.setInt(4, id);
                statement.executeUpdate();

                JOptionPane.showMessageDialog(this, "Customer updated successfully!");
                loadCustomersData();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating customer: " + e.getMessage());
            }
        }
    }

    private void deleteCustomer() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
            return;
        }

        int id = (int) customersTable.getValueAt(selectedRow, 0);

        String query = "DELETE FROM customers WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
            loadCustomersData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage());
        }
    }

    private void searchCustomers() {
        String keyword = searchField.getText().trim();
        DefaultTableModel model = (DefaultTableModel) customersTable.getModel();
        model.setRowCount(0);

        String query = "SELECT id, first_name, last_name, phone_number FROM customers WHERE " +
                "first_name ILIKE ? OR last_name ILIKE ? OR phone_number ILIKE ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            String searchTerm = "%" + keyword + "%";
            statement.setString(1, searchTerm);
            statement.setString(2, searchTerm);
            statement.setString(3, searchTerm);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String phone = resultSet.getString("phone_number");

                model.addRow(new Object[]{id, firstName, lastName, phone});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching customers: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CashierFrame frame = new CashierFrame();
            frame.setVisible(true);
        });
    }
}
