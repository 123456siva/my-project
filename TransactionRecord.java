import java.sql.Timestamp;

public class TransactionRecord {
    private int transId;
    private int accNo;
    private String type;
    private double amount;
    private Timestamp dateTime;

    public TransactionRecord(int transId, int accNo, String type, double amount, Timestamp dateTime) {
        this.transId = transId;
        this.accNo = accNo;
        this.type = type;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return transId + " | " + accNo + " | " + type + " | " + amount + " | " + dateTime;
    }
}
