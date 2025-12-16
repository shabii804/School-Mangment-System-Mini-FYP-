// Main.java
public class Main {
    public static void main(String[] args) {
        // Load data
        DataStore.loadData();
        // Open login
        javax.swing.SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}
