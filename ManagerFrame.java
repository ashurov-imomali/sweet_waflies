import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManagerFrame extends JFrame {

    public ManagerFrame() {
        setTitle("Manager Dashboard - Sweet Waffles");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Создаем панель для кнопок с отступами
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Добавляем отступы: верх, левый, нижний, правый

        JButton manageEmployeesButton = new JButton("Manage Employees");
        JButton manageProductsButton = new JButton("Manage Products");
        JButton manageCustomersButton = new JButton("Manage Customers");

        buttonPanel.add(manageEmployeesButton);
        buttonPanel.add(manageProductsButton);
        buttonPanel.add(manageCustomersButton);

        add(buttonPanel, BorderLayout.WEST);

        // Панель для отображения CRUD-форм с отступами
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Отступы вокруг контента
        add(contentPanel, BorderLayout.CENTER);

        // Создаем CRUD формы и добавляем их с отступами
        JPanel employeePanel = new JPanel();
        employeePanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Отступы внутри формы
        employeePanel.add(new JLabel("Employee CRUD Form Here"));

        JPanel productPanel = new JPanel();
        productPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        productPanel.add(new JLabel("Product CRUD Form Here"));

        JPanel customerPanel = new JPanel();
        customerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        customerPanel.add(new JLabel("Customer CRUD Form Here"));

        contentPanel.add(employeePanel, "Employees");
        contentPanel.add(productPanel, "Products");
        contentPanel.add(customerPanel, "Customers");

        // Слушатели для кнопок
        manageEmployeesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                contentPanel.add(new EmployeePanel()); // Добавляем EmployeePanel
                contentPanel.revalidate();
                contentPanel.repaint();
            }});

        manageProductsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                contentPanel.add(new ProductPanel()); // Добавляем EmployeePanel
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });

        manageCustomersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout)(contentPanel.getLayout());
                cl.show(contentPanel, "Customers");
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ManagerFrame::new);
    }
}
