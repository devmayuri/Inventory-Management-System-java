import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryGUI extends JFrame {
    private ProductManager manager;
    private JTable table;
    private DefaultTableModel model;
    private static final int LOW_STOCK_THRESHOLD = 5;

    public InventoryGUI(ProductManager manager) {
        this.manager = manager;

        setTitle("Inventory Management System");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Table Setup ---
        model = new DefaultTableModel(new Object[]{"ID", "Name", "Quantity", "Price", "Status"}, 0);
        table = new JTable(model);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int qty = (int) table.getModel().getValueAt(row, 2);
                if (qty == 0) c.setBackground(Color.RED);
                else if (qty < LOW_STOCK_THRESHOLD) c.setBackground(Color.PINK);
                else c.setBackground(Color.WHITE);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // --- Buttons ---
        JButton addBtn = new JButton("Add Product");
        JButton updateBtn = new JButton("Update Product");
        JButton deleteBtn = new JButton("Delete Product");
        JButton sellBtn = new JButton("Sell Product");
        JButton saveBtn = new JButton("Save CSV");
        JButton loadBtn = new JButton("Load CSV");

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(sellBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(loadBtn);

        // --- Search Field ---
        JTextField searchField = new JTextField(15);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchProduct(searchField.getText());
            }
        });

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        add(searchPanel, BorderLayout.NORTH);

        loadProducts();

        // --- Button Actions ---
        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        sellBtn.addActionListener(e -> sellProduct());
        saveBtn.addActionListener(e -> {
            manager.saveToCSV("inventory.csv");
            JOptionPane.showMessageDialog(this, "Inventory saved to inventory.csv");
        });
        loadBtn.addActionListener(e -> {
            manager.loadFromCSV("inventory.csv");
            loadProducts();
            JOptionPane.showMessageDialog(this, "Inventory loaded from inventory.csv");
        });

        setVisible(true);
    }

    private void loadProducts() {
        model.setRowCount(0);
        boolean lowStockFound = false;

        for (Product p : manager.getAllProducts()) {
            String status;
            if (p.getQuantity() == 0) status = "Out of Stock";
            else if (p.getQuantity() < LOW_STOCK_THRESHOLD) status = "Low Stock";
            else status = "In Stock";

            if (p.getQuantity() < LOW_STOCK_THRESHOLD) lowStockFound = true;

            model.addRow(new Object[]{p.getId(), p.getName(), p.getQuantity(), p.getPrice(), status});
        }

        if (lowStockFound) {
            JOptionPane.showMessageDialog(this, "⚠ Some products are low in stock!", "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addProduct() {
        try {
            String name = JOptionPane.showInputDialog(this, "Enter Product Name:");
            if (name == null || name.trim().isEmpty()) return;

            int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Quantity:"));
            double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Price:"));

            manager.addProduct(name, quantity, price);
            loadProducts();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }

    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to update.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        Product product = manager.findProduct(id);

        if (product != null) {
            try {
                String name = JOptionPane.showInputDialog(this, "Enter New Name:", product.getName());
                int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Quantity:", product.getQuantity()));
                double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter New Price:", product.getPrice()));

                product.setName(name);
                product.setQuantity(quantity);
                product.setPrice(price);
                manager.updateProduct(product);
                loadProducts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to delete.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        manager.deleteProduct(id);
        loadProducts();
    }

    private void sellProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a product to sell.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        Product product = manager.findProduct(id);

        if (product != null && product.getQuantity() > 0) {
            product.setQuantity(product.getQuantity() - 1);
            manager.updateProduct(product);
            loadProducts();
        } else {
            JOptionPane.showMessageDialog(this, "Product out of stock!");
        }
    }

    private void searchProduct(String keyword) {
        model.setRowCount(0);

        if (keyword == null || keyword.trim().isEmpty()) {
            loadProducts();
            return;
        }

        keyword = keyword.toLowerCase();
        for (Product p : manager.getAllProducts()) {
            if (String.valueOf(p.getId()).contains(keyword) || p.getName().toLowerCase().contains(keyword)) {
                String status;
                if (p.getQuantity() == 0) status = "Out of Stock";
                else if (p.getQuantity() < LOW_STOCK_THRESHOLD) status = "Low Stock";
                else status = "In Stock";

                model.addRow(new Object[]{p.getId(), p.getName(), p.getQuantity(), p.getPrice(), status});
            }
        }
    }
}
