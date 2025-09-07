import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {
    private List<Product> products;
    private int nextId = 1;

    public ProductManager() {
        products = new ArrayList<>();
    }

    public void addProduct(String name, int quantity, double price) {
        Product product = new Product(nextId++, name, quantity, price);
        products.add(product);
    }

    public Product findProduct(int id) {
        for (Product p : products) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public void updateProduct(Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updatedProduct.getId()) {
                products.set(i, updatedProduct);
                return;
            }
        }
    }

    public void deleteProduct(int id) {
        products.removeIf(p -> p.getId() == id);
    }

    public List<Product> getAllProducts() {
        return products;
    }

    // --- CSV Saving ---
    public void saveToCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("ID,Name,Quantity,Price");
            for (Product p : products) {
                pw.println(p.getId() + "," + p.getName() + "," + p.getQuantity() + "," + p.getPrice());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CSV Loading ---
    public void loadFromCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            products.clear();
            String line;
            br.readLine(); // skip header
            int maxId = 0;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                double price = Double.parseDouble(parts[3]);
                products.add(new Product(id, name, quantity, price));
                if (id > maxId) maxId = id;
            }

            nextId = maxId + 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
