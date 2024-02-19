package BookPurchaseExample;

import DatabaseConnection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JonhDoeBookPurchaseExample {
    public static void purchaseBook() {
        Connection con = null;
        try {
            // 1. Establish a database connection
            con = new DatabaseConnection().getConnection();
            // 2. Disable auto-commit mode to start the transaction
            con.setAutoCommit(false);

            /*
            Remember, hardcoding IDs and prices like this is primarily for example purposes
             */
            int bookId = 1; // ID for "Harry Potter and the Philosopher's Stone"
            int customerId = 1; // ID for "John Doe"
            double bookPrice = 20.0; // Price for "Harry Potter and the Philosopher's Stone"

            // 3. Check Inventory
            String checkInventorySql = "SELECT quantity, price FROM inventory WHERE book_id = ?";
            try (PreparedStatement checkInventoryStmt = con.prepareStatement(checkInventorySql)) {
                checkInventoryStmt.setInt(1, bookId);
                ResultSet rs = checkInventoryStmt.executeQuery();
                if (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    bookPrice = rs.getDouble("price");
                    if (quantity < 1) {
                        throw new Exception("Book is out of stock.");
                    }
                } else {
                    throw new Exception("Book not found.");
                }
            }

            // 4. Update Inventory
            String updateInventorySql = "UPDATE inventory SET quantity = quantity - 1 WHERE book_id = ?";
            try (PreparedStatement updateInventoryStmt = con.prepareStatement(updateInventorySql)) {
                updateInventoryStmt.setInt(1, bookId);
                updateInventoryStmt.executeUpdate();
            }

            // 5. Process Payment
            String processPaymentSql = "UPDATE customers SET balance = balance - ? WHERE customer_id = ?";
            try (PreparedStatement processPaymentStmt = con.prepareStatement(processPaymentSql)) {
                processPaymentStmt.setDouble(1, bookPrice);
                processPaymentStmt.setInt(2, customerId);
                int rowsAffected = processPaymentStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new Exception("Customer not found or insufficient balance.");
                }
            }

            // 6. Commit Transaction
            con.commit();
            System.out.println("Purchase successful, transaction committed.");

        } catch (Exception e) {
            System.err.println("Purchase failed: " + e.getMessage());
            if (con != null) {
                try {
                    // Rollback transaction
                    con.rollback();
                    System.out.println("Transaction rolled back.");
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback transaction: ");
                    ex.printStackTrace();
                }
            }
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Re-enable auto-commit mode
                    con.close(); // Close connection
                } catch (SQLException ex) {
                    System.err.println("Failed to re-enable auto-commit or close connection: ");
                    ex.printStackTrace();
                }
            }
        }
    }
}
