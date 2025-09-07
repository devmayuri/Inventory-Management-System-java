import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProductManager manager = new ProductManager();
            new InventoryGUI(manager);
        });
    }
}
