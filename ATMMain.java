import java.util.List;
import java.util.Scanner;

public class ATMMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ATMDAO dao = new ATMDAO();

        System.out.println("=== Welcome to Simple ATM ===");

        while (true) {
            System.out.print("\nEnter Account Number (or 0 to exit): ");
            int accNo = sc.nextInt();
            if (accNo == 0) break;
            System.out.print("Enter PIN: ");
            int pin = sc.nextInt();

            User user = dao.login(accNo, pin);
            if (user == null) {
                System.out.println("Incorrect account number or PIN. Try again.");
                continue;
            }

            System.out.println("\nHello, " + user.getName() + "!");
            boolean session = true;
            while (session) {
                System.out.println("\n1. Check Balance");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Transaction History");
                System.out.println("5. Logout");
                System.out.println("6. Change PIN");

                System.out.print("Choose: ");
                int ch = sc.nextInt();

                switch (ch) {
                    case 1:
                        double bal = dao.getBalance(accNo);
                        System.out.println("Current balance: " + bal);
                        break;
                    case 2:
                        System.out.print("Enter amount to deposit: ");
                        double dAmt = sc.nextDouble();
                        if (dao.deposit(accNo, dAmt)) {
                            System.out.println("Deposit successful.");
                        } else {
                            System.out.println("Deposit failed.");
                        }
                        break;
                    case 3:
                        System.out.print("Enter amount to withdraw: ");
                        double wAmt = sc.nextDouble();
                        if (dao.withdraw(accNo, wAmt)) {
                            System.out.println("Withdrawal successful.");
                        } else {
                            System.out.println("Withdrawal failed (insufficient funds or error).");
                        }
                        break;
                    case 4:
                        List<TransactionRecord> tx = dao.getTransactions(accNo, 10);
                        if (tx.isEmpty()) System.out.println("No transactions found.");
                        else {
                            System.out.println("id | acc | type | amount | datetime");
                            for (TransactionRecord t : tx) System.out.println(t);
                        }
                        break;
                    case 5:
                        session = false;
                        System.out.println("Logged out.");
                        break;
                    default:
                        System.out.println("Invalid option.");
                    case 6:
                    	Scanner sr= new Scanner(System.in);
                    	System.out.print("Enter your Account Number: ");
                    	int acc_No = sr.nextInt();
                    	System.out.print("Enter your current PIN: ");
                    	int oldPin = sr.nextInt();
                    	System.out.print("Enter new PIN: ");
                    	int newPin1 = sc.nextInt();
                    	System.out.print("Re-enter new PIN: ");
                    	int newPin2 = sc.nextInt();

                    	if(newPin1 != newPin2){
                    	    System.out.println("PINs do not match. Try again.");
                    	} else {
                    	    ATMDAO dao1 = new ATMDAO();
                    	    boolean changed = dao1.changePin(accNo, oldPin, newPin1);
                    	    if(changed){
                    	        System.out.println("PIN changed successfully!");
                    	    } else {
                    	        System.out.println("Incorrect current PIN or error occurred.");
                    	    }
                    	    dao1.close();
                    	}

                }
            }
        }

        dao.close();
        sc.close();
        System.out.println("Goodbye!");
    }
}
