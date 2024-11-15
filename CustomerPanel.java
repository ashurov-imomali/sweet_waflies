import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Vector;

public class CustomerPanel extends JPanel {

    private JTextField firstNameFilterField;
    private JTextField lastNameFilterField;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private void exportToXls() {
        try (Workbook workbook = new HSSFWorkbook()  ) {
            Sheet sheet = workbook.createSheet("Data");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < customerTable.getColumnCount(); i++) {
                headerRow.createCell(i).setCellValue(customerTable.getColumnName(i));
            }
            for (int i = 0; i < customerTable.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < customerTable.getColumnCount(); j++) {
                    row.createCell(j).setCellValue(customerTable.getValueAt(i, j).toString());
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream("customer.xlsx")) {
                workbook.write(fileOut);
            }

            JOptionPane.showMessageDialog(null, "Data exported to XLS successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportToJson() {
        try {
            FileWriter writer = new FileWriter("customer.json");
            JsonArray jsonArray = new JsonArray();

            // Пример: Для каждой строки в таблице получаем данные и конвертируем их в JSON
            for (int i = 0; i < customerTable.getRowCount(); i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", (Integer) customerTable.getValueAt(i, 0));
                jsonObject.addProperty("first_name", (String) customerTable.getValueAt(i, 1));
                jsonObject.addProperty("last_name", (String) customerTable.getValueAt(i, 2));
                jsonObject.addProperty("email", (String) customerTable.getValueAt(i, 3));
                jsonObject.addProperty("city", (String) customerTable.getValueAt(i, 4));
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

    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Панель кнопок управления заказчиками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Customer");
        JButton editButton = new JButton("Edit Customer");
        JButton exportJsonButton = new JButton("Export to JSON");
        JButton exportXlsButton = new JButton("Export to XLS");
        buttonPanel.add(exportJsonButton);
        buttonPanel.add(exportXlsButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Панель фильтрации
        JPanel filterPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameFilterField = new JTextField();
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameFilterField = new JTextField();

        filterPanel.add(firstNameLabel);
        filterPanel.add(firstNameFilterField);
        filterPanel.add(lastNameLabel);
        filterPanel.add(lastNameFilterField);
        add(filterPanel, BorderLayout.NORTH);

        // Таблица заказчиков
        tableModel = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Email", "Phone Number", "City", "Country"}, 0);
        customerTable = new JTable(tableModel);
        customerTable.setFillsViewportHeight(true);
        rowSorter = new TableRowSorter<>(tableModel);
        customerTable.setRowSorter(rowSorter);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        // Загрузка данных заказчиков
        loadCustomerData();

        // Логика фильтрации
        addFilterListeners();

        // Обработчик кнопки "Добавить"
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCustomerDialog(null);  // null - добавление нового заказчика
            }
        });
        exportJsonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToJson();
            }
        });
        exportXlsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToXls();
            }
        });
        // Обработчик кнопки "Редактировать"
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow != -1) {
                    int customerId = (int) tableModel.getValueAt(customerTable.convertRowIndexToModel(selectedRow), 0);
                    openCustomerDialog(customerId);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a customer to edit.");
                }
            }
        });
    }

    // Метод загрузки данных заказчиков
    private void loadCustomerData() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
            String query = "SELECT id, first_name, last_name, email, phone_number, city, country FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tableModel.setRowCount(0);

            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getInt("id"));
                row.add(resultSet.getString("first_name"));
                row.add(resultSet.getString("last_name"));
                row.add(resultSet.getString("email"));
                row.add(resultSet.getString("phone_number"));
                row.add(resultSet.getString("city"));
                row.add(resultSet.getString("country"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для фильтрации данных
    private void addFilterListeners() {
        firstNameFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        lastNameFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        String firstNameText = firstNameFilterField.getText().trim();
        String lastNameText = lastNameFilterField.getText().trim();

        RowFilter<DefaultTableModel, Object> firstNameFilter = RowFilter.regexFilter("(?i)" + firstNameText, 1);
        RowFilter<DefaultTableModel, Object> lastNameFilter = RowFilter.regexFilter("(?i)" + lastNameText, 2);

        rowSorter.setRowFilter(RowFilter.andFilter(List.of(firstNameFilter, lastNameFilter)));
    }

    // Метод для открытия диалога добавления/редактирования заказчика
    private void openCustomerDialog(Integer customerId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(customerId == null ? "Add Customer" : "Edit Customer");
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneNumberField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField countryField = new JTextField();

        dialog.add(new JLabel("First Name:"));
        dialog.add(firstNameField);
        dialog.add(new JLabel("Last Name:"));
        dialog.add(lastNameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Phone Number:"));
        dialog.add(phoneNumberField);
        dialog.add(new JLabel("City:"));
        dialog.add(cityField);
        dialog.add(new JLabel("Country:"));
        dialog.add(countryField);

        JButton saveButton = new JButton("Save");
        dialog.add(saveButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String email = emailField.getText().trim();
                String phoneNumber = phoneNumberField.getText().trim();
                String city = cityField.getText().trim();
                String country = countryField.getText().trim();

                try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
                    if (customerId == null) {
                        String insertQuery = "INSERT INTO customers (first_name, last_name, email, phone_number, city, country) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                            stmt.setString(1, firstName);
                            stmt.setString(2, lastName);
                            stmt.setString(3, email);
                            stmt.setString(4, phoneNumber);
                            stmt.setString(5, city);
                            stmt.setString(6, country);
                            stmt.executeUpdate();
                        }
                    } else {
                        String updateQuery = "UPDATE customers SET first_name=?, last_name=?, email=?, phone_number=?, city=?, country=? WHERE id=?";
                        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                            stmt.setString(1, firstName);
                            stmt.setString(2, lastName);
                            stmt.setString(3, email);
                            stmt.setString(4, phoneNumber);
                            stmt.setString(5, city);
                            stmt.setString(6, country);
                            stmt.setInt(7, customerId);
                            stmt.executeUpdate();
                        }
                    }
                    loadCustomerData();
                    dialog.dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if (customerId != null) {
            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
                String selectQuery = "SELECT first_name, last_name, email, phone_number, city, country FROM customers WHERE id=?";
                try (PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        firstNameField.setText(rs.getString("first_name"));
                        lastNameField.setText(rs.getString("last_name"));
                        emailField.setText(rs.getString("email"));
                        phoneNumberField.setText(rs.getString("phone_number"));
                        cityField.setText(rs.getString("city"));
                        countryField.setText(rs.getString("country"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        dialog.setVisible(true);
    }
}
