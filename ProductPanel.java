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
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Vector;

public class ProductPanel extends JPanel {

    private JTextField nameFilterField;
    private JTextField descriptionFilterField;
    private JTextField priceFilterField;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private void exportToXls() {
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

    private void exportToJson() {
        try {
            FileWriter writer = new FileWriter("product.json");
            JsonArray jsonArray = new JsonArray();

            // Пример: Для каждой строки в таблице получаем данные и конвертируем их в JSON
            for (int i = 0; i < productTable.getRowCount(); i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", (Integer) productTable.getValueAt(i, 0));
                jsonObject.addProperty("name", (String) productTable.getValueAt(i, 1));
                jsonObject.addProperty("description", (String) productTable.getValueAt(i, 2));
                jsonObject.addProperty("price", (BigDecimal) productTable.getValueAt(i, 3));
                jsonObject.addProperty("stock_quantity", (Integer) productTable.getValueAt(i, 4));
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

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Панель кнопок управления продуктами
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton exportJsonButton = new JButton("Export to JSON");
        JButton exportXlsButton = new JButton("Export to XLS");
        JButton exitButton = new JButton("Exit");
        buttonPanel.add(exportJsonButton);
        buttonPanel.add(exportXlsButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Панель фильтрации
        JPanel filterPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        JLabel nameLabel = new JLabel("Name:");
        nameFilterField = new JTextField();
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionFilterField = new JTextField();
        JLabel priceLabel = new JLabel("Price:");
        priceFilterField = new JTextField();

        filterPanel.add(nameLabel);
        filterPanel.add(nameFilterField);
        filterPanel.add(descriptionLabel);
        filterPanel.add(descriptionFilterField);
        filterPanel.add(priceLabel);
        filterPanel.add(priceFilterField);
        add(filterPanel, BorderLayout.NORTH);

        // Таблица продуктов
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Description", "Price", "Stock Quantity"}, 0);
        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        rowSorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(rowSorter);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Загрузка данных продуктов
        loadProductData();

        // Логика фильтрации
        addFilterListeners();

        // Обработчик кнопки "Добавить"
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProductDialog(null);  // null - добавление нового продукта
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
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        // Обработчик кнопки "Редактировать"
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow != -1) {
                    int productId = (int) tableModel.getValueAt(productTable.convertRowIndexToModel(selectedRow), 0);
                    openProductDialog(productId);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a product to edit.");
                }
            }
        });
    }

    // Метод загрузки данных продуктов
    private void loadProductData() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
            String query = "SELECT id, name, description, price, stock_quantity FROM products";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tableModel.setRowCount(0);

            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getInt("id"));
                row.add(resultSet.getString("name"));
                row.add(resultSet.getString("description"));
                row.add(resultSet.getBigDecimal("price"));
                row.add(resultSet.getInt("stock_quantity"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для фильтрации данных
    private void addFilterListeners() {
        nameFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        descriptionFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        priceFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        String nameText = nameFilterField.getText().trim();
        String descriptionText = descriptionFilterField.getText().trim();
        String priseText = priceFilterField.getText().trim();

        RowFilter<DefaultTableModel, Object> nameFilter = RowFilter.regexFilter("(?i)" + nameText, 1);
        RowFilter<DefaultTableModel, Object> descriptionFilter = RowFilter.regexFilter("(?i)" + descriptionText, 2);
        RowFilter<DefaultTableModel, Object> priceFilter = RowFilter.regexFilter("(?i)" + priseText, 3);

        rowSorter.setRowFilter(RowFilter.andFilter(List.of(nameFilter, descriptionFilter, priceFilter)));
    }

    // Метод для открытия диалога добавления/редактирования продукта
    private void openProductDialog(Integer productId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(productId == null ? "Add Product" : "Edit Product");
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockQuantityField = new JTextField();

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Description:"));
        dialog.add(descriptionField);
        dialog.add(new JLabel("Price:"));
        dialog.add(priceField);
        dialog.add(new JLabel("Stock Quantity:"));
        dialog.add(stockQuantityField);

        JButton saveButton = new JButton("Save");
        dialog.add(saveButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                String price = priceField.getText().trim();
                String stockQuantity = stockQuantityField.getText().trim();

                try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
                    if (productId == null) {
                        String insertQuery = "INSERT INTO products (name, description, price, stock_quantity) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                            stmt.setString(1, name);
                            stmt.setString(2, description);
                            stmt.setBigDecimal(3, new BigDecimal(price));
                            stmt.setInt(4, Integer.parseInt(stockQuantity));
                            stmt.executeUpdate();
                        }
                    } else {
                        String updateQuery = "UPDATE products SET name=?, description=?, price=?, stock_quantity=? WHERE id=?";
                        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                            stmt.setString(1, name);
                            stmt.setString(2, description);
                            stmt.setBigDecimal(3, new BigDecimal(price));
                            stmt.setInt(4, Integer.parseInt(stockQuantity));
                            stmt.setInt(5, productId);
                            stmt.executeUpdate();
                        }
                    }
                    loadProductData();
                    dialog.dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if (productId != null) {
            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
                String selectQuery = "SELECT name, description, price, stock_quantity FROM products WHERE id=?";
                try (PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
                    stmt.setInt(1, productId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        nameField.setText(rs.getString("name"));
                        descriptionField.setText(rs.getString("description"));
                        priceField.setText(rs.getBigDecimal("price").toString());
                        stockQuantityField.setText(String.valueOf(rs.getInt("stock_quantity")));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        dialog.setVisible(true);
    }
}
