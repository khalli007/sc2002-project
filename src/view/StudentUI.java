package view;

import controller.Database;
import model.Student;
import model.Internship;
import model.InternshipLevel;
import model.InternshipStatus;
import model.Application;
import model.ApplicationStatus;
import model.WithdrawalStatus;

import java.util.List;
import java.util.Scanner;

/**
 * StudentUI - Handles all menus and interactions for Student users.
 */
public class StudentUI {
    private static final Scanner sc = new Scanner(System.in);
    private static final Database db = Database.getInstance();

    public static void showMenu(Student student) {
        while (true) {
            System.out.println("\n===== Student Menu =====");
            System.out.println("Welcome, " + student.getName() + " (" + student.getMajor() + ", Year " + student.getYear() + ")");
            System.out.println("1. View Available Internships");
            System.out.println("2. Apply for Internship");
            System.out.println("3. View My Applications");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewInternships(student);
                    break;
                case "2":
                    applyInternship(student);
                    break;
                case "3":
                    viewApplications(student);
                    break;
                case "4":
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // View student's applications

    private static void viewInternships(Student student) {
        System.out.println("\n--- Available Internships ---");
        List<Internship> internships = db.getInternships();

        boolean found = false;
        for (Internship i : internships) {
            // Conditions: must be APPROVED, VISIBLE, and match student's profile
            if (i.getStatus() == InternshipStatus.APPROVED && i.isVisible()) {
                boolean levelAllowed = (student.getYear() >= 3) ||
                        (student.getYear() <= 2 && i.getLevel() == InternshipLevel.BASIC);
                if (levelAllowed && i.getPreferredMajor().equalsIgnoreCase(student.getMajor())) {
                    System.out.println("[" + i.getInternshipID() + "] " + i.getInternshipTitle() +
                            " (" + i.getCompanyName() + ") - Level: " + i.getLevel());
                    found = true;
                }
            }
        }
        if (!found) System.out.println("No internships available currently.");
    }

    //apply for internship

    private static void applyInternship(Student student) {
        System.out.println("\n--- Apply for Internship ---");

        long activeApps = db.getApplicationsByStudent(student.getUserID()).stream()
                .filter(a -> a.getWithdrawalStatus() != WithdrawalStatus.APPROVED)
                .count();

        if (activeApps >= 3) {
            System.out.println("You already have 3 active applications.");
            return;
        }

        System.out.print("Enter Internship ID to apply: ");
        String input = sc.nextLine();
        int internshipID;

        try {
            internshipID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Internship internship = db.findInternshipById(internshipID);
        if (internship == null) {
            System.out.println("Invalid Internship ID.");
            return;
        }

        // Check eligibility
        if (student.getYear() <= 2 && internship.getLevel() != InternshipLevel.BASIC) {
            System.out.println("You can only apply for Basic-level internships.");
            return;
        }

        boolean alreadyApplied = db.getApplicationsByStudent(student.getUserID()).stream()
                .anyMatch(a -> a.getInternshipID() == internshipID && a.getWithdrawalStatus() != WithdrawalStatus.APPROVED);

        if (alreadyApplied) {
            System.out.println("You have already applied for this internship.");
            return;
        }

        Application newApp = new Application(student.getUserID(), internshipID);
        db.addApplication(newApp);
        System.out.println("Application submitted successfully! Status: PENDING");
    }

    //view applications

    private static void viewApplications(Student student) {
        System.out.println("\n--- My Internship Applications ---");
        List<Application> apps = db.getApplicationsByStudent(student.getUserID());

        if (apps.isEmpty()) {
            System.out.println("You have not applied for any internships yet.");
            return;
        }

        for (Application app : apps) {
            Internship i = db.findInternshipById(app.getInternshipID());
            String internshipInfo = (i == null)
                    ? "[Internship deleted]"
                    : i.getInternshipTitle() + " (" + i.getCompanyName() + ")";
            System.out.println("Application ID: " + app.getApplicationID());
            System.out.println("    Internship: " + internshipInfo);
            System.out.println("    Status    : " + app.getStatus());
            System.out.println("    Withdrawal: " + app.getWithdrawalStatus());
            System.out.println("    Accepted  : " + (app.isAccepted() ? "Yes" : "No"));
            System.out.println("-----------------------------------");
        }

        System.out.println("1. Accept Successful Internship");
        System.out.println("2. Request Withdrawal");
        System.out.println("3. Back");
        System.out.print("Enter choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                acceptInternship(student);
                break;
            case "2":
                requestWithdrawal(student);
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    //accept internship

    private static void acceptInternship(Student student) {
        List<Application> successfulApps = db.getApplicationsByStudent(student.getUserID()).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SUCCESSFUL)
                .filter(a -> !a.isAccepted())
                .toList();

        if (successfulApps.isEmpty()) {
            System.out.println("You have no successful applications to accept.");
            return;
        }

        System.out.print("Enter Application ID to accept: ");
        String input = sc.nextLine();
        int appID;

        try {
            appID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Application chosen = successfulApps.stream()
                .filter(a -> a.getApplicationID() == appID)
                .findFirst()
                .orElse(null);

        if (chosen == null) {
            System.out.println("Invalid Application ID.");
            return;
        }

        chosen.setAccepted(true);
        chosen.setWithdrawalStatus(WithdrawalStatus.NONE);
        System.out.println("Internship accepted successfully!");

        // Withdraw all others
        for (Application other : db.getApplicationsByStudent(student.getUserID())) {
            if (other.getApplicationID() != chosen.getApplicationID()) {
                other.setStatus(ApplicationStatus.UNSUCCESSFUL);
                other.setWithdrawalStatus(WithdrawalStatus.APPROVED);
                other.setAccepted(false);
            }
        }

        db.saveData();
    }

    //request withdrawal

    private static void requestWithdrawal(Student student) {
        List<Application> apps = db.getApplicationsByStudent(student.getUserID());

        System.out.print("Enter Application ID to request withdrawal: ");
        String input = sc.nextLine();
        int appID;

        try {
            appID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        Application app = apps.stream()
                .filter(a -> a.getApplicationID() == appID)
                .findFirst()
                .orElse(null);

        if (app == null) {
            System.out.println("Application not found.");
            return;
        }

        if (app.getWithdrawalStatus() == WithdrawalStatus.APPROVED) {
            System.out.println("This application has already been withdrawn.");
            return;
        }

        app.setWithdrawalStatus(WithdrawalStatus.REQUESTED);
        System.out.println("Withdrawal request submitted (awaiting staff approval).");
        db.saveData();
    }

    
}