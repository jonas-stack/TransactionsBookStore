package BookPurchaseExample;

import DatabaseConnection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JohnDoeBookPurchaseExample {

    public static void purchaseBook() {
        Connection con = null;
        try {
            con = new DatabaseConnection().getConnection();
            con.setAutoCommit(false);
            System.out.println("Database connection established");

            int bookId = 1; // Example ID for "Harry Potter and the Philosopher's Stone"
            int customerId = 1; // Example ID for "John Doe"

            // Check inventory and get price
            double bookPrice = checkInventoryAndGetPrice(con, bookId); // This already checks for quantity

            // Check if customer has enough balance
            double customerBalance = checkCustomerBalance(con, customerId);
            if (customerBalance < bookPrice) {
                throw new Exception("Customer does not have enough money.");
            }

            // Process payment and update inventory
            processPayment(con, customerId, bookPrice);
            updateInventory(con, bookId);

            con.commit();
            System.out.println("Purchase successful, transaction committed.");
        } catch (SQLException ex) {
            System.err.println("SQL error occurred: " + ex.getMessage());
            tryRollback(con);
        } catch (Exception e) {
            System.err.println("Purchase failed: " + e.getMessage());
            tryRollback(con);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Re-enable auto-commit mode
                    con.close(); // Ensure connection is closed
                } catch (SQLException ex) {
                    System.err.println("Failed to re-enable auto-commit or close connection: " + ex.getMessage());
                }
            }
        }
    }

    private static double checkCustomerBalance(Connection con, int customerId) throws SQLException, Exception {
        String sql = "SELECT balance FROM customers WHERE customer_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    throw new Exception("Customer not found.");
                }
            }
        }
    }

    private static double checkInventoryAndGetPrice(Connection con, int bookId) throws Exception {
        String sql = "SELECT quantity, price FROM inventory WHERE book_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    if (quantity < 1) throw new Exception("Book is out of stock.");
                    System.out.println("Inventory checked");
                    return rs.getDouble("price");
                } else {
                    throw new Exception("Book not found.");
                }
            }
        }
    }

    private static void processPayment(Connection con, int customerId, double bookPrice) throws SQLException, Exception {
        String sql = "UPDATE customers SET balance = balance - ? WHERE customer_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDouble(1, bookPrice);
            stmt.setInt(2, customerId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) throw new Exception("Customer not found or insufficient balance.");
            System.out.println("Payment processed");
        }
    }

    private static void updateInventory(Connection con, int bookId) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity - 1 WHERE book_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            System.out.println("Inventory updated");
        }
    }

    private static void tryRollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
                System.out.println("Transaction rolled back.");
            } catch (SQLException ex) {
                System.err.println("Failed to rollback transaction: " + ex.getMessage());
            }
        }
    }
}
