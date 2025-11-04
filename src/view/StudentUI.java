package view;

import controller.Database;
import model.Application;
import model.ApplicationStatus;
import model.Internship;
import model.InternshipLevel;
import model.InternshipStatus;
import model.Student;
import model.WithdrawalStatus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Boundary class that contains all student-facing menus and workflows.
 */
public final class StudentUI {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Database DB = Database.getInstance();

    private StudentUI() {
        // Utility class
    }

    /**
     * Entry point for the student flow. Displays the main menu until logout.
     */
    public static void showStudentMenu(Student student) {
        StudentFilter filter = new StudentFilter();
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Student Dashboard ===");
            System.out.println("Welcome, " + student.getName() + " (" + student.getUserID() + ")");
            System.out.println("1. View Internship Opportunities");
            System.out.println("2. Apply for an Internship");
            System.out.println("3. View My Applications");
            System.out.println("4. Change Password");
            System.out.println("5. Logout");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> viewInternships(student, filter);
                case 2 -> applyForInternship(student, filter);
                case 3 -> viewApplications(student);
                case 4 -> PasswordUI.changePassword(student);
                case 5 -> exit = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void viewInternships(Student student, StudentFilter filter) {
        boolean back = false;
        while (!back) {
            List<Internship> baseList = DB.getVisibleInternshipsForStudent(student);
            List<Internship> filtered = filter.apply(baseList);

            System.out.println("\n--- Internship Listings ---");
            if (filtered.isEmpty()) {
                System.out.println("No internships matched your criteria.");
            } else {
                filtered.forEach(StudentUI::printInternshipSummary);
            }

            System.out.println("\nCurrent filters: " + filter);
            System.out.println("1. Update filters");
            System.out.println("2. Reset filters");
            System.out.println("3. Back to student menu");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> updateFilters(filter);
                case 2 -> filter.reset();
                case 3 -> back = true;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void updateFilters(StudentFilter filter) {
        boolean done = false;
        while (!done) {
            System.out.println("\n--- Update Filters ---");
            System.out.println("1. Set internship level (current: " + (filter.level == null ? "All" : filter.level) + ")");
            System.out.println("2. Set status filter (current: " + (filter.status == null ? "All" : filter.status) + ")");
            System.out.println("3. Set company keyword (current: " + (filter.companyKeyword == null ? "None" : filter.companyKeyword) + ")");
            System.out.println("4. Set closing date upper bound (current: " + (filter.closingDate == null ? "None" : filter.closingDate) + ")");
            System.out.println("5. Back");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> filter.level = promptLevel();
                case 2 -> filter.status = promptStatus();
                case 3 -> {
                    System.out.print("Enter keyword (leave blank to clear): ");
                    String input = SCANNER.nextLine().trim();
                    filter.companyKeyword = input.isEmpty() ? null : input;
                }
                case 4 -> {
                    System.out.print("Enter closing date (YYYY-MM-DD) or leave blank: ");
                    String input = SCANNER.nextLine().trim();
                    if (input.isEmpty()) {
                        filter.closingDate = null;
                    } else {
                        try {
                            filter.closingDate = LocalDate.parse(input);
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Filter unchanged.");
                        }
                    }
                }
                case 5 -> done = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static InternshipLevel promptLevel() {
        System.out.print("Enter internship level (Basic/Intermediate/Advanced or leave blank): ");
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return InternshipLevel.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown level. Keeping previous value.");
            return null;
        }
    }

    private static InternshipStatus promptStatus() {
        System.out.print("Enter status (Approved/Filled) or leave blank: ");
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            InternshipStatus status = InternshipStatus.valueOf(input.toUpperCase());
            if (status == InternshipStatus.APPROVED || status == InternshipStatus.FILLED) {
                return status;
            }
            System.out.println("Students can only filter by Approved or Filled statuses.");
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown status. Keeping previous value.");
        }
        return null;
    }

    private static void printInternshipSummary(Internship internship) {
        System.out.println("[" + internship.getInternshipID() + "] " + internship.getInternshipTitle());
        System.out.println("    Company       : " + internship.getCompanyName());
        System.out.println("    Level         : " + internship.getLevel());
        System.out.println("    Preferred Major: " + internship.getPreferredMajor());
        System.out.println("    Status        : " + internship.getStatus());
        System.out.println("    Slots         : " + internship.getSlots());
        System.out.println("    Application Window: " + internship.getOpeningDate() + " to " + internship.getClosingDate());
    }

    private static void applyForInternship(Student student, StudentFilter filter) {
        if (DB.hasStudentAcceptedPlacement(student.getUserID())) {
            System.out.println("You have already accepted an internship placement. You cannot apply for more internships.");
            return;
        }
        long activeCount = DB.countActiveApplicationsForStudent(student.getUserID());
        if (activeCount >= 3) {
            System.out.println("You already have the maximum of 3 active applications.");
            return;
        }

        List<Internship> available = filter.apply(DB.getVisibleInternshipsForStudent(student));
        if (available.isEmpty()) {
            System.out.println("No internships are currently available that meet your filters.");
            return;
        }

        System.out.println("\n--- Eligible Internships ---");
        available.forEach(StudentUI::printInternshipSummary);
        int internshipId = readInt("Enter the internship ID to apply (0 to cancel): ");
        if (internshipId == 0) {
            return;
        }

        Internship internship = DB.findInternshipById(internshipId);
        if (internship == null || !available.contains(internship)) {
            System.out.println("Invalid internship ID or you are not eligible for this internship.");
            return;
        }
        if (internship.getStatus() == InternshipStatus.FILLED) {
            System.out.println("This internship has already been filled.");
            return;
        }
        if (!internship.isAcceptingOn(LocalDate.now())) {
            System.out.println("The application window for this internship is closed.");
            return;
        }

        boolean alreadyApplied = DB.getApplicationsByStudent(student.getUserID()).stream()
                .anyMatch(app -> app.getInternshipID() == internshipId
                        && app.getWithdrawalStatus() != WithdrawalStatus.APPROVED);
        if (alreadyApplied) {
            System.out.println("You have already applied for this internship.");
            return;
        }

        Application application = new Application(student.getUserID(), internshipId);
        DB.addApplication(application);
        System.out.println("Application submitted successfully. Status is currently PENDING.");
    }

    private static void viewApplications(Student student) {
        List<Application> applications = DB.getApplicationsByStudent(student.getUserID());
        applications.sort(Comparator.comparing(Application::getApplicationID));
        if (applications.isEmpty()) {
            System.out.println("You have not submitted any applications yet.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- My Applications ---");
            for (Application application : applications) {
                Internship internship = DB.findInternshipById(application.getInternshipID());
                String internshipTitle = internship == null ? "[Deleted Internship]" : internship.getInternshipTitle();
                System.out.println("Application ID: " + application.getApplicationID());
                System.out.println("    Internship : " + internshipTitle);
                System.out.println("    Status     : " + application.getStatus());
                System.out.println("    Accepted   : " + (application.isAccepted() ? "Yes" : "No"));
                System.out.println("    Withdrawal : " + application.getWithdrawalStatus());
                if (internship != null) {
                    System.out.println("    Internship Visibility: " + (internship.isVisible() ? "Visible" : "Hidden"));
                }
            }

            System.out.println("\n1. Accept a successful application");
            System.out.println("2. Request withdrawal");
            System.out.println("3. Cancel withdrawal request");
            System.out.println("4. Back");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> acceptApplication(student, applications);
                case 2 -> requestWithdrawal(applications);
                case 3 -> cancelWithdrawal(applications);
                case 4 -> back = true;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void acceptApplication(Student student, List<Application> applications) {
        if (DB.hasStudentAcceptedPlacement(student.getUserID())) {
            System.out.println("You have already accepted an internship placement.");
            return;
        }
        List<Application> eligible = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.SUCCESSFUL)
                .filter(app -> !app.isAccepted())
                .filter(app -> app.getWithdrawalStatus() != WithdrawalStatus.APPROVED)
                .collect(Collectors.toList());
        if (eligible.isEmpty()) {
            System.out.println("There are no successful applications awaiting your confirmation.");
            return;
        }

        int applicationId = readInt("Enter the application ID to accept (0 to cancel): ");
        if (applicationId == 0) {
            return;
        }
        Application application = eligible.stream()
                .filter(app -> app.getApplicationID() == applicationId)
                .findFirst()
                .orElse(null);
        if (application == null) {
            System.out.println("Invalid application ID.");
            return;
        }
        Internship internship = DB.findInternshipById(application.getInternshipID());
        if (internship == null) {
            System.out.println("The linked internship no longer exists.");
            return;
        }
        long acceptedCount = DB.countAcceptedForInternship(internship.getInternshipID());
        if (acceptedCount >= internship.getSlots()) {
            System.out.println("All slots for this internship have been filled.");
            return;
        }

        application.setAccepted(true);
        application.setWithdrawalStatus(WithdrawalStatus.NONE);
        System.out.println("You have accepted the internship offer for " + internship.getInternshipTitle() + ".");

        // Withdraw all other active applications automatically
        for (Application other : DB.getApplicationsByStudent(student.getUserID())) {
            if (other.getApplicationID() != application.getApplicationID()
                    && other.getWithdrawalStatus() != WithdrawalStatus.APPROVED) {
                other.setStatus(ApplicationStatus.UNSUCCESSFUL);
                other.setAccepted(false);
                other.setWithdrawalStatus(WithdrawalStatus.APPROVED);
            }
        }

        DB.updateInternshipFilledStatus(internship);
    }

    private static void requestWithdrawal(List<Application> applications) {
        List<Application> eligible = applications.stream()
                .filter(app -> app.getWithdrawalStatus() == WithdrawalStatus.NONE
                        || app.getWithdrawalStatus() == WithdrawalStatus.REJECTED)
                .collect(Collectors.toList());
        if (eligible.isEmpty()) {
            System.out.println("There are no applications eligible for withdrawal requests.");
            return;
        }
        int applicationId = readInt("Enter the application ID to request withdrawal (0 to cancel): ");
        if (applicationId == 0) {
            return;
        }
        Application application = eligible.stream()
                .filter(app -> app.getApplicationID() == applicationId)
                .findFirst()
                .orElse(null);
        if (application == null) {
            System.out.println("Invalid application ID.");
            return;
        }
        application.setWithdrawalStatus(WithdrawalStatus.REQUESTED);
        System.out.println("Withdrawal request submitted. Awaiting Career Center Staff decision.");
    }

    private static void cancelWithdrawal(List<Application> applications) {
        List<Application> pending = applications.stream()
                .filter(app -> app.getWithdrawalStatus() == WithdrawalStatus.REQUESTED)
                .collect(Collectors.toList());
        if (pending.isEmpty()) {
            System.out.println("You do not have any pending withdrawal requests.");
            return;
        }
        int applicationId = readInt("Enter the application ID to cancel the withdrawal request (0 to cancel): ");
        if (applicationId == 0) {
            return;
        }
        Application application = pending.stream()
                .filter(app -> app.getApplicationID() == applicationId)
                .findFirst()
                .orElse(null);
        if (application == null) {
            System.out.println("Invalid application ID.");
            return;
        }
        application.setWithdrawalStatus(WithdrawalStatus.NONE);
        System.out.println("Withdrawal request cancelled.");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static class StudentFilter {
        private InternshipLevel level;
        private InternshipStatus status;
        private String companyKeyword;
        private LocalDate closingDate;

        List<Internship> apply(List<Internship> internships) {
            List<Internship> result = new ArrayList<>(internships);
            if (level != null) {
                result = result.stream()
                        .filter(internship -> internship.getLevel() == level)
                        .collect(Collectors.toList());
            }
            if (status != null) {
                result = result.stream()
                        .filter(internship -> internship.getStatus() == status)
                        .collect(Collectors.toList());
            }
            if (companyKeyword != null) {
                String keywordLower = companyKeyword.toLowerCase();
                result = result.stream()
                        .filter(internship -> internship.getCompanyName().toLowerCase().contains(keywordLower))
                        .collect(Collectors.toList());
            }
            if (closingDate != null) {
                result = result.stream()
                        .filter(internship -> internship.getClosingDate() != null
                                && !internship.getClosingDate().isAfter(closingDate))
                        .collect(Collectors.toList());
            }
            result.sort(Comparator.comparing(Internship::getInternshipTitle, String.CASE_INSENSITIVE_ORDER));
            return result;
        }

        void reset() {
            level = null;
            status = null;
            companyKeyword = null;
            closingDate = null;
        }

        @Override
        public String toString() {
            return "Level=" + (level == null ? "All" : level)
                    + ", Status=" + (status == null ? "All" : status)
                    + ", Company contains='" + (companyKeyword == null ? "" : companyKeyword) + "'"
                    + ", Closing date<= " + (closingDate == null ? "Any" : closingDate);
        }
    }
}
