import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CashierFrame extends JFrame {
    private JTable customersTable, ordersTable;
    private JTextField searchField;
    private JButton addCustomerButton, editCustomerButton, deleteCustomerButton, addOrderButton;

    public CashierFrame() {
        setTitle("Cashier Panel - Sweet Waffles");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Верхняя панель: поиск
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel searchLabel = new JLabel("Search Customer:");
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
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

        // Нижняя панель: действия
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Действия кнопок
        logoutButton.addActionListener(e -> {
            // Логика выхода
            JOptionPane.showMessageDialog(this, "Logged out!");
            dispose();
        });
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
        addCustomerButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add Customer clicked!"));
        editCustomerButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Edit Customer clicked!"));
        deleteCustomerButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Delete Customer clicked!"));

        return panel;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Таблица заказов
        ordersTable = new JTable(new DefaultTableModel(
                new Object[]{"Order ID", "Product", "Quantity", "Total Price"}, 0
        ));
        JScrollPane tableScrollPane = new JScrollPane(ordersTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Панель действий
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addOrderButton = new JButton("Add Order");
        actionsPanel.add(addOrderButton);
        panel.add(actionsPanel, BorderLayout.SOUTH);

        // Действия кнопок
        addOrderButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add Order clicked!"));

        return panel;
    }

    public static void main(String[] args) {
        // Установка современного стиля FlatLaf
        try {
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            CashierFrame frame = new CashierFrame();
            frame.setVisible(true);
        });
    }
}
