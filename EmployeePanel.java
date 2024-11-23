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

public class EmployeePanel extends JPanel {

    private JTextField firstNameFilterField;
    private JTextField salaryFilterField;
    private JTextField lastNameFilterField;
    private JTextField positionFilterField;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private void exportToXls() {
        try (Workbook workbook = new HSSFWorkbook()  ) {
            Sheet sheet = workbook.createSheet("Data");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < employeeTable.getColumnCount(); i++) {
                headerRow.createCell(i).setCellValue(employeeTable.getColumnName(i));
            }
            for (int i = 0; i < employeeTable.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < employeeTable.getColumnCount(); j++) {
                    row.createCell(j).setCellValue(employeeTable.getValueAt(i, j).toString());
                }
            }

            try (FileOutputStream fileOut = new FileOutputStream("employee.xlsx")) {
                workbook.write(fileOut);
            }

            JOptionPane.showMessageDialog(null, "Data exported to XLS successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportToJson() {
        try {
            FileWriter writer = new FileWriter("employee.json");
            JsonArray jsonArray = new JsonArray();

            // Пример: Для каждой строки в таблице получаем данные и конвертируем их в JSON
            for (int i = 0; i < employeeTable.getRowCount(); i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", (Integer) employeeTable.getValueAt(i, 0));
                jsonObject.addProperty("first_name", (String) employeeTable.getValueAt(i, 1));
                jsonObject.addProperty("last_name", (String) employeeTable.getValueAt(i, 2));
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


    public EmployeePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Панель с кнопками для управления сотрудниками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Employee");
        JButton editButton = new JButton("Edit Employee");
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
        JPanel filterPanel = new JPanel(new GridLayout(1, 6, 5, 5));
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameFilterField = new JTextField();
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameFilterField = new JTextField();
        JLabel positionLabel = new JLabel("Position:");
        positionFilterField = new JTextField();
        JLabel salaryLabel = new JLabel("Salary:");
        salaryFilterField = new JTextField();

        filterPanel.add(firstNameLabel);
        filterPanel.add(firstNameFilterField);
        filterPanel.add(lastNameLabel);
        filterPanel.add(lastNameFilterField);
        filterPanel.add(positionLabel);
        filterPanel.add(positionFilterField);
        filterPanel.add(salaryLabel);
        filterPanel.add(salaryFilterField);

        add(filterPanel, BorderLayout.NORTH);

        // Таблица с сотрудниками
        tableModel = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Email", "Position", "Salary"}, 0);
        employeeTable = new JTable(tableModel);
        employeeTable.setFillsViewportHeight(true);
        rowSorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(rowSorter);
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        add(scrollPane, BorderLayout.CENTER);

        // Загрузка данных сотрудников
        loadEmployeeData();

        // Логика фильтрации
        addFilterListeners();

        // Обработчик кнопки "Добавить"
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openEmployeeDialog(null);  // null - добавление нового сотрудника
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
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
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow != -1) {
                    int employeeId = (int) tableModel.getValueAt(employeeTable.convertRowIndexToModel(selectedRow), 0);
                    openEmployeeDialog(employeeId);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select an employee to edit.");
                }
            }
        });
    }

    // Метод загрузки данных сотрудников
    private void loadEmployeeData() {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
            String query = "SELECT id, first_name, last_name, email, position, salary FROM employees";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            tableModel.setRowCount(0);

            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getInt("id"));
                row.add(resultSet.getString("first_name"));
                row.add(resultSet.getString("last_name"));
                row.add(resultSet.getString("email"));
                row.add(resultSet.getString("position"));
                row.add(resultSet.getString("salary"));
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
        salaryFilterField.addKeyListener(new KeyAdapter() {
           @Override
           public void keyReleased(KeyEvent e) {
               applyFilters();
           }
        });
        positionFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        String firstNameText = firstNameFilterField.getText().trim();
        String lastNameText = lastNameFilterField.getText().trim();
        String positionText = positionFilterField.getText().trim();
        String salaryText = salaryFilterField.getText().trim();
        RowFilter<DefaultTableModel, Object> firstNameFilter = RowFilter.regexFilter("(?i)" + firstNameText, 1);
        RowFilter<DefaultTableModel, Object> lastNameFilter = RowFilter.regexFilter("(?i)" + lastNameText, 2);
        RowFilter<DefaultTableModel, Object> positionFilter = RowFilter.regexFilter("(?i)" + positionText, 4);
        RowFilter<DefaultTableModel, Object> salaryFilter = RowFilter.regexFilter("(?i)" + salaryText, 5);

        rowSorter.setRowFilter(RowFilter.andFilter(List.of(firstNameFilter, lastNameFilter, positionFilter, salaryFilter)));
    }

    // Метод для открытия диалога добавления/редактирования сотрудника
    private void openEmployeeDialog(Integer employeeId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(employeeId == null ? "Add Employee" : "Edit Employee");
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField salaryField = new JTextField();

        dialog.add(new JLabel("First Name:"));
        dialog.add(firstNameField);
        dialog.add(new JLabel("Last Name:"));
        dialog.add(lastNameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Position:"));
        dialog.add(positionField);
        dialog.add(new JLabel("Salary:"));
        dialog.add(salaryField);

        JButton saveButton = new JButton("Save");
        dialog.add(saveButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String email = emailField.getText().trim();
                String position = positionField.getText().trim();
                String salary = salaryField.getText().trim();

                try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
                    if (employeeId == null) {
                        String insertQuery = "INSERT INTO employees (first_name, last_name, email, position, salary) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                            stmt.setString(1, firstName);
                            stmt.setString(2, lastName);
                            stmt.setString(3, email);
                            stmt.setString(4, position);
                            stmt.setBigDecimal(5, new BigDecimal(salary));
                            stmt.executeUpdate();
                        }
                    } else {
                        String updateQuery = "UPDATE employees SET first_name=?, last_name=?, email=?, position=?, salary=? WHERE id=?";
                        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                            stmt.setString(1, firstName);
                            stmt.setString(2, lastName);
                            stmt.setString(3, email);
                            stmt.setString(4, position);
                            BigDecimal s = new BigDecimal(salary);
                            stmt.setBigDecimal(5, s);
                            stmt.setInt(6, employeeId);
                            stmt.executeUpdate();
                        }
                    }
                    loadEmployeeData();
                    dialog.dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if (employeeId != null) {
            try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sweet_waffles", "postgres", "postgres")) {
                String selectQuery = "SELECT first_name, last_name, email, position, salary FROM employees WHERE id=?";
                try (PreparedStatement stmt = connection.prepareStatement(selectQuery)) {
                    stmt.setInt(1, employeeId);
                    ResultSet resultSet = stmt.executeQuery();
                    if (resultSet.next()) {
                        firstNameField.setText(resultSet.getString("first_name"));
                        lastNameField.setText(resultSet.getString("last_name"));
                        emailField.setText(resultSet.getString("email"));
                        positionField.setText(resultSet.getString("position"));
                        salaryField.setText(resultSet.getString("salary"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        dialog.setVisible(true);
    }
}
