import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ATMDAO {
    private Connection con;

    // Update these to match your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/atmdb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    public ATMDAO() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database connection failed. Check URL/user/password and connector jar.");
        }
    }

    // Authenticate user by acc_no and pin, return User object if ok, else null
    public User login(int accNo, int pin) {
        String sql = "SELECT acc_no, pin, name, balance FROM users WHERE acc_no = ? AND pin = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            ps.setInt(2, pin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("acc_no"), rs.getInt("pin"), rs.getString("name"), rs.getDouble("balance"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get current balance
    public double getBalance(int accNo) {
        String sql = "SELECT balance FROM users WHERE acc_no = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Deposit money (uses transaction)
    public boolean deposit(int accNo, double amount) {
        if (amount <= 0) return false;

        String updateSql = "UPDATE users SET balance = balance + ? WHERE acc_no = ?";
        String transSql = "INSERT INTO transactions (acc_no, trans_type, amount) VALUES (?, 'deposit', ?)";
        try {
            con.setAutoCommit(false);

            try (PreparedStatement psUpd = con.prepareStatement(updateSql);
                 PreparedStatement psTrans = con.prepareStatement(transSql)) {

                psUpd.setDouble(1, amount);
                psUpd.setInt(2, accNo);
                int rows = psUpd.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    return false;
                }

                psTrans.setInt(1, accNo);
                psTrans.setDouble(2, amount);
                psTrans.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Withdraw money (check balance, use transaction)
    public boolean withdraw(int accNo, double amount) {
        if (amount <= 0) return false;

        String checkSql = "SELECT balance FROM users WHERE acc_no = ? FOR UPDATE";
        String updateSql = "UPDATE users SET balance = balance - ? WHERE acc_no = ?";
        String transSql = "INSERT INTO transactions (acc_no, trans_type, amount) VALUES (?, 'withdraw', ?)";

        try {
            con.setAutoCommit(false);

            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, accNo);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false; // account not found
                    }
                    double balance = rs.getDouble("balance");
                    if (balance < amount) {
                        con.rollback();
                        return false; // insufficient funds
                    }
                }
            }

            try (PreparedStatement psUpd = con.prepareStatement(updateSql);
                 PreparedStatement psTrans = con.prepareStatement(transSql)) {

                psUpd.setDouble(1, amount);
                psUpd.setInt(2, accNo);
                psUpd.executeUpdate();

                psTrans.setInt(1, accNo);
                psTrans.setDouble(2, amount);
                psTrans.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get last N transactions for an account
    public List<TransactionRecord> getTransactions(int accNo, int limit) {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = "SELECT trans_id, acc_no, trans_type, amount, date_time FROM transactions WHERE acc_no = ? ORDER BY date_time DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactionRecord(
                        rs.getInt("trans_id"),
                        rs.getInt("acc_no"),
                        rs.getString("trans_type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("date_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Change PIN method
    public boolean changePin(int accNo, int oldPin, int newPin) {
        try {
            // Check old PIN
            String checkSql = "SELECT pin FROM users WHERE acc_no = ? AND pin = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                psCheck.setInt(1, accNo);
                psCheck.setInt(2, oldPin);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) return false; // old PIN incorrect
                }
            }

            // Update to new PIN
            String updateSql = "UPDATE users SET pin = ? WHERE acc_no = ?";
            try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
                psUpdate.setInt(1, newPin);
                psUpdate.setInt(2, accNo);
                int rows = psUpdate.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Close connection
    public void close() {
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
