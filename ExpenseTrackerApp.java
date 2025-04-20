import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ExpenseTrackerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}

class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private static final String USER_DATA_FILE = "users.dat";

    public LoginFrame() {
        setTitle("Login - Expense Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);

        JPanel panel = new JPanel();
        panel.add(loginBtn);
        panel.add(registerBtn);
        add(panel);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());

        setVisible(true);
    }

    private void login() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());
        Map<String, String> users = loadUsers();

        if (users.containsKey(user) && users.get(user).equals(pass)) {
            new ExpenseTrackerFrame(user);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid login.");
        }
    }

    private void register() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter valid details.");
            return;
        }

        Map<String, String> users = loadUsers();
        if (users.containsKey(user)) {
            JOptionPane.showMessageDialog(this, "User already exists.");
            return;
        }

        users.put(user, pass);
        saveUsers(users);
        JOptionPane.showMessageDialog(this, "Registered successfully.");
    }

    private Map<String, String> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
            return (Map<String, String>) ois.readObject();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveUsers(Map<String, String> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ExpenseTrackerFrame extends JFrame {
    private String username;
    private DefaultTableModel model;
    private List<Expense> expenses;
    private static final String FILE_PREFIX = "expenses_";

    public ExpenseTrackerFrame(String username) {
        this.username = username;
        setTitle("Expense Tracker - " + username);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        expenses = loadExpenses();

        // Top Panel: Expense Entry
        JPanel topPanel = new JPanel(new FlowLayout());
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        JTextField categoryField = new JTextField(10);
        JTextField amountField = new JTextField(7);
        JButton addBtn = new JButton("Add Expense");

        topPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        topPanel.add(dateField);
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryField);
        topPanel.add(new JLabel("Amount:"));
        topPanel.add(amountField);
        topPanel.add(addBtn);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Table
        model = new DefaultTableModel(new String[]{"Date", "Category", "Amount"}, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        refreshTable();

        // Bottom Panel: Summary
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton sumBtn = new JButton("Show Category Totals");
        bottomPanel.add(sumBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            try {
                String date = dateField.getText();
                String cat = categoryField.getText();
                double amt = Double.parseDouble(amountField.getText());
                Expense expense = new Expense(date, cat, amt);
                expenses.add(expense);
                saveExpenses();
                refreshTable();
                dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                categoryField.setText("");
                amountField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid data.");
            }
        });

        sumBtn.addActionListener(e -> showCategorySums());

        setVisible(true);
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (Expense e : expenses) {
            model.addRow(new Object[]{e.date, e.category, String.format("%.2f", e.amount)});
        }
    }

    private void showCategorySums() {
        Map<String, Double> sums = new HashMap<>();
        for (Expense e : expenses) {
            sums.put(e.category, sums.getOrDefault(e.category, 0.0) + e.amount);
        }
        StringBuilder sb = new StringBuilder("Category-wise Totals:\n");
        for (String cat : sums.keySet()) {
            sb.append(cat).append(": ").append(String.format("%.2f", sums.get(cat))).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private List<Expense> loadExpenses() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PREFIX + username + ".dat"))) {
            return (List<Expense>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveExpenses() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PREFIX + username + ".dat"))) {
            oos.writeObject(expenses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Expense implements Serializable {
    String date;
    String category;
    double amount;

    public Expense(String date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }
}
