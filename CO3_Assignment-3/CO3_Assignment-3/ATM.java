import java.io.*;
import java.util.*;

/** Checked, user-defined exception: a business failure the caller must handle. */
class InsufficientFundsException extends Exception {
    InsufficientFundsException(String msg) { super(msg); }
}

public class ATM {

    /** Withdraws money. Returns the catch path taken (for the test table). */
    static String withdraw(String cardFile, String account, String amountText) {
        BufferedReader card = null;
        String path = "no exception (success)";
        try {
            card = new BufferedReader(new FileReader(cardFile));          // FileNotFoundException (checked)
            int amount = Integer.parseInt(amountText.trim());             // NumberFormatException (unchecked)
            if (amount <= 0)
                throw new IllegalArgumentException("Amount must be positive: " + amount); // unchecked
            int balance = -1;
            String line;
            while ((line = card.readLine()) != null) {                    // IOException (checked)
                String[] p = line.split(",");
                if (p[0].equals(account.trim())) {                        // p[1] may not exist -> ArrayIndexOutOfBounds
                    balance = Integer.parseInt(p[1]);
                    break;
                }
            }
            if (balance < 0) throw new NoSuchElementException("Account not found: " + account);
            if (amount > balance)
                throw new InsufficientFundsException("Balance " + balance + " < requested " + amount);
            System.out.println("   Dispensed " + amount + ". New balance " + (balance - amount));
        } catch (FileNotFoundException e) {            // narrower checked first
            path = "catch (FileNotFoundException)";
            System.out.println("   Card reader file missing: " + e.getMessage());
        } catch (IOException e) {                      // broader checked second
            path = "catch (IOException)";
            System.out.println("   Card reader I/O error: " + e.getMessage());
        } catch (NumberFormatException | NoSuchElementException e) {   // multi-catch (unrelated types)
            path = "catch (NumberFormatException | NoSuchElementException)";
            System.out.println("   Invalid input/data: " + e.getMessage());
        } catch (IllegalArgumentException e) {         // NumberFormatException is its subclass -> must come AFTER it
            path = "catch (IllegalArgumentException)";
            System.out.println("   Invalid amount: " + e.getMessage());
        } catch (InsufficientFundsException e) {       // user-defined checked
            path = "catch (InsufficientFundsException)";
            System.out.println("   Declined: " + e.getMessage());
        } catch (RuntimeException e) {                 // last-resort safety net for unexpected bugs
            path = "catch (RuntimeException) " + e.getClass().getSimpleName();
            System.out.println("   Unexpected internal error: " + e);
        } finally {
            System.out.println("   [finally] card released");     // runs on EVERY path
            try { if (card != null) card.close(); } catch (IOException ignored) { }
        }
        return path;
    }

    public static void main(String[] args) {
        String[][] tests = {
            {"cards.txt",   "1001", "500"},      // T1 valid
            {"cards.txt",   "1001", "abc"},      // T2 letters instead of number
            {"cards.txt",   "1001", "-200"},     // T3 negative amount
            {"cards.txt",   "1001", "99999"},    // T4 more than balance
            {"nofile.txt",  "1001", "100"},      // T5 card-reader file missing
            {"cards.txt",   "9999", "100"},      // T6 unknown account
            {"cards.txt",   "1004", "100"},      // T7 corrupt record (no balance field)
            {"cards.txt",   "1003", "100"},      // T8 corrupt record (non-numeric balance)
        };
        int n = 1;
        for (String[] t : tests) {
            System.out.println("T" + n + ": file=" + t[0] + " acct=" + t[1] + " amount=" + t[2]);
            String path = withdraw(t[0], t[1], t[2]);
            System.out.println("   => PATH: " + path);
            n++;
        }
    }
}
