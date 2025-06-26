import java.util.*;
import java.text.SimpleDateFormat;

public class ATMSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Map<String, User> users = new HashMap<>();
    private static User currentUser = null;

    public static void main(String[] args) {
        initializeUsers();
        showWelcomeScreen();
    }

    private static void initializeUsers() {
        // Initialize with some sample users
        users.put("123456", new User("123456", "1234", "John Doe", 5000));
        users.put("654321", new User("654321", "4321", "Jane Smith", 3000));
    }

    private static void showWelcomeScreen() {
        while (true) {
            System.out.println("\n===== WELCOME TO ATM SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        login();
                        break;
                    case 2:
                        register();
                        break;
                    case 3:
                        System.out.println("Thank you for using our ATM. Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (1-3).");
            }
        }
    }

    private static void register() {
        System.out.println("\n===== REGISTRATION =====");
        
        // Generate random 6-digit user ID
        String userId = String.format("%06d", new Random().nextInt(999999));
        while (users.containsKey(userId)) {
            userId = String.format("%06d", new Random().nextInt(999999));
        }
        
        System.out.println("Your generated User ID: " + userId);
        
        System.out.print("Enter PIN (4 digits): ");
        String pin = scanner.nextLine();
        if (!pin.matches("\\d{4}")) {
            System.out.println("PIN must be exactly 4 digits.");
            return;
        }
        
        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();
        if (name.trim().isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        
        double initialBalance = 0;
        System.out.print("Enter initial deposit amount: ");
        try {
            initialBalance = Double.parseDouble(scanner.nextLine());
            if (initialBalance < 0) {
                System.out.println("Initial deposit cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
            return;
        }
        
        User newUser = new User(userId, pin, name, initialBalance);
        users.put(userId, newUser);
        
        // Record initial deposit
        newUser.addTransaction(new Transaction(
            "DEPOSIT", initialBalance, null, initialBalance));
        
        System.out.println("\nRegistration successful!");
        System.out.println("Please note your User ID: " + userId);
        System.out.println("Your current balance: $" + initialBalance);
    }

    private static void login() {
        System.out.println("\n===== ATM LOGIN =====");
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();

        if (users.containsKey(userId) && users.get(userId).getPin().equals(pin)) {
            currentUser = users.get(userId);
            System.out.println("\nLogin successful! Welcome, " + currentUser.getName() + "!");
            showMainMenu();
        } else {
            System.out.println("Invalid User ID or PIN. Please try again.");
        }
    }

    private static void showMainMenu() {
        while (currentUser != null) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Transactions History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Quit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        showTransactionHistory();
                        break;
                    case 2:
                        withdraw();
                        break;
                    case 3:
                        deposit();
                        break;
                    case 4:
                        transfer();
                        break;
                    case 5:
                        currentUser = null;
                        System.out.println("Logged out successfully.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (1-5).");
            }
        }
    }

    private static void showTransactionHistory() {
        System.out.println("\n===== TRANSACTION HISTORY =====");
        List<Transaction> transactions = currentUser.getTransactionHistory();

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.printf("%-20s %-12s %-10s %-10s %-15s\n", 
                         "Date/Time", "Type", "Amount", "To/From", "Balance");
        System.out.println("------------------------------------------------------------");

        for (Transaction t : transactions) {
            System.out.printf("%-20s %-12s %-10.2f %-10s %-15.2f\n",
                             dateFormat.format(t.getTimestamp()),
                             t.getType(),
                             t.getAmount(),
                             t.getOtherParty() == null ? "N/A" : t.getOtherParty(),
                             t.getBalanceAfter());
        }
    }

    private static void withdraw() {
        System.out.println("\n===== WITHDRAW =====");
        System.out.print("Enter amount to withdraw: ");

        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            if (amount > currentUser.getBalance()) {
                System.out.println("Insufficient funds.");
                return;
            }

            currentUser.setBalance(currentUser.getBalance() - amount);
            currentUser.addTransaction(new Transaction(
                "WITHDRAW", amount, null, currentUser.getBalance()));
            
            System.out.printf("Successfully withdrew $%.2f. New balance: $%.2f\n", 
                            amount, currentUser.getBalance());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        }
    }

    private static void deposit() {
        System.out.println("\n===== DEPOSIT =====");
        System.out.print("Enter amount to deposit: ");

        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            currentUser.setBalance(currentUser.getBalance() + amount);
            currentUser.addTransaction(new Transaction(
                "DEPOSIT", amount, null, currentUser.getBalance()));
            
            System.out.printf("Successfully deposited $%.2f. New balance: $%.2f\n", 
                            amount, currentUser.getBalance());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        }
    }

    private static void transfer() {
        System.out.println("\n===== TRANSFER =====");
        System.out.print("Enter recipient's User ID: ");
        String recipientId = scanner.nextLine();

        if (!users.containsKey(recipientId)) {
            System.out.println("Recipient not found.");
            return;
        }

        if (recipientId.equals(currentUser.getUserId())) {
            System.out.println("Cannot transfer to yourself.");
            return;
        }

        System.out.print("Enter amount to transfer: ");

        try {
            double amount = Double.parseDouble(scanner.nextLine());

            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            if (amount > currentUser.getBalance()) {
                System.out.println("Insufficient funds.");
                return;
            }

            User recipient = users.get(recipientId);
            
            // Perform transfer
            currentUser.setBalance(currentUser.getBalance() - amount);
            recipient.setBalance(recipient.getBalance() + amount);
            
            // Record transactions for both users
            currentUser.addTransaction(new Transaction(
                "TRANSFER_OUT", amount, recipientId, currentUser.getBalance()));
            recipient.addTransaction(new Transaction(
                "TRANSFER_IN", amount, currentUser.getUserId(), recipient.getBalance()));
            
            System.out.printf("Successfully transferred $%.2f to %s (%s). New balance: $%.2f\n", 
                            amount, recipient.getName(), recipientId, currentUser.getBalance());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a valid number.");
        }
    }

    // User class
    static class User {
        private String userId;
        private String pin;
        private String name;
        private double balance;
        private List<Transaction> transactionHistory;

        public User(String userId, String pin, String name, double balance) {
            this.userId = userId;
            this.pin = pin;
            this.name = name;
            this.balance = balance;
            this.transactionHistory = new ArrayList<>();
        }

        public String getUserId() { return userId; }
        public String getPin() { return pin; }
        public String getName() { return name; }
        public double getBalance() { return balance; }
        public void setBalance(double balance) { this.balance = balance; }
        public List<Transaction> getTransactionHistory() { return transactionHistory; }
        
        public void addTransaction(Transaction transaction) {
            transactionHistory.add(0, transaction); // Add to beginning to show newest first
        }
    }

    // Transaction class
    static class Transaction {
        private Date timestamp;
        private String type;
        private double amount;
        private String otherParty;
        private double balanceAfter;

        public Transaction(String type, double amount, String otherParty, double balanceAfter) {
            this.timestamp = new Date();
            this.type = type;
            this.amount = amount;
            this.otherParty = otherParty;
            this.balanceAfter = balanceAfter;
        }

        public Date getTimestamp() { return timestamp; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getOtherParty() { return otherParty; }
        public double getBalanceAfter() { return balanceAfter; }
    }
}