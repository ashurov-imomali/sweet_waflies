import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CashierPanel extends JPanel {
    private JTable customersTable;
    private JTextField firstNameField, lastNameField, phoneField;
    private JButton addButton, editButton, deleteButton, refreshButton, addOrderButton;

    public CashierPanel() {
        setLayout(new BorderLayout());

        // Панель для клиентов
        JPanel customerPanel = new JPanel(new BorderLayout());

        // Таблица клиентов
        customersTable = new JTable(); // инициализировать с моделью данных
        customerPanel.add(new JScrollPane(customersTable), BorderLayout.CENTER);

        // Поля ввода
        JPanel customerForm = new JPanel(new GridLayout(4, 2, 5, 5));
        customerForm.add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        customerForm.add(firstNameField);

        customerForm.add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        customerForm.add(lastNameField);

        customerForm.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        customerForm.add(phoneField);

        // Панель с кнопками для управления клиентами
        addButton = new JButton("Add Customer");
        editButton = new JButton("Edit Customer");
        deleteButton = new JButton("Delete Customer");
        refreshButton = new JButton("Refresh List");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        customerPanel.add(customerForm, BorderLayout.NORTH);
        customerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Панель добавления заказа
        JPanel orderPanel = new JPanel();
        orderPanel.setLayout(new FlowLayout());
        addOrderButton = new JButton("Add Order");
        orderPanel.add(addOrderButton);

        // Добавляем панели на главный
        add(customerPanel, BorderLayout.CENTER);
        add(orderPanel, BorderLayout.SOUTH);

        // Добавить действия для кнопок
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Логика добавления клиента
            }
        });

        // Аналогично добавьте действия для других кнопок
    }
}
