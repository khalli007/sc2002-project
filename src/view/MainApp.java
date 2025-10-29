package view;

import controller.Database;

public class MainApp {

    public static void main(String[] args) {
        
        // 1. Get the one-and-only Database instance
        Database db = Database.getInstance();
        
        // 2. Load all data from files (or CSVs if no files exist)
        db.loadData();

        System.out.println("=================================================");
        System.out.println("  Welcome to the Internship Placement Management System (IPMS)");
        System.out.println("=================================================");

        // 3. Start the main login UI (which we will create next)
        // This will run the entire application loop
        LoginUI.showLoginMenu();

        // 4. When the user quits the LoginUI, the program comes back here.
        // We save all data before exiting.
        System.out.println("Saving all data... Thank you for using IPMS!");
        db.saveData();
    }
}