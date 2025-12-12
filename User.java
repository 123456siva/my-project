public class User {
    private int accNo;
    private int pin;
    private String name;
    private double balance;

    public User(int accNo, int pin, String name, double balance) {
        this.accNo = accNo;
        this.pin = pin;
        this.name = name;
        this.balance = balance;
    }

    public int getAccNo() { return accNo; }
    public int getPin() { return pin; }
    public String getName() { return name; }
    public double getBalance() { return balance; }

    public void setBalance(double balance) { this.balance = balance; }
}
