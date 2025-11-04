package view;

import controller.Database;
import model.Application;
import model.ApplicationStatus;
import model.CompanyRepresentative;
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
 * Boundary class for the Company Representative workflow.
 */
public final class CompanyRepUI {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Database DB = Database.getInstance();
    private static final int MAX_INTERNSHIPS = 5;

    private CompanyRepUI() {
    }

    public static void showCompanyRepMenu(CompanyRepresentative rep) {
        RepFilter filter = new RepFilter();
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=== Company Representative Dashboard ===");
            System.out.println("Welcome, " + rep.getName() + " from " + rep.getCompanyName());
            System.out.println("1. View & Manage My Internships");
            System.out.println("2. Create New Internship");
            System.out.println("3. Change Password");
            System.out.println("4. Logout");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> manageInternships(rep, filter);
                case 2 -> createInternship(rep);
                case 3 -> PasswordUI.changePassword(rep);
                case 4 -> exit = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void manageInternships(CompanyRepresentative rep, RepFilter filter) {
        boolean back = false;
        while (!back) {
            List<Internship> internships = filter.apply(DB.getInternshipsByCompanyRep(rep.getUserID()));
            System.out.println("\n--- My Internships ---");
            if (internships.isEmpty()) {
                System.out.println("You have not created any internships yet.");
            } else {
                internships.forEach(CompanyRepUI::printInternship);
            }

            System.out.println("\nFilters: " + filter);
            System.out.println("1. View internship details");
            System.out.println("2. Update filters");
            System.out.println("3. Reset filters");
            System.out.println("4. Back");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> {
                    if (internships.isEmpty()) {
                        System.out.println("No internships to view.");
                    } else {
                        int id = readInt("Enter internship ID: ");
                        Internship internship = internships.stream()
                                .filter(i -> i.getInternshipID() == id)
                                .findFirst()
                                .orElse(null);
                        if (internship == null) {
                            System.out.println("Invalid internship ID.");
                        } else {
                            viewInternshipDetails(rep, internship);
                        }
                    }
                }
                case 2 -> updateFilters(filter);
                case 3 -> filter.reset();
                case 4 -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void viewInternshipDetails(CompanyRepresentative rep, Internship internship) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Internship Details ---");
            printInternship(internship);
            long acceptedCount = DB.countAcceptedForInternship(internship.getInternshipID());
            System.out.println("Confirmed placements: " + acceptedCount + "/" + internship.getSlots());

            System.out.println("1. Toggle visibility");
            System.out.println("2. Edit details");
            System.out.println("3. Delete internship");
            System.out.println("4. Manage applications");
            System.out.println("5. Back");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> toggleVisibility(internship);
                case 2 -> editInternship(internship);
                case 3 -> {
                    if (deleteInternship(internship)) {
                        back = true;
                    }
                }
                case 4 -> manageApplications(rep, internship);
                case 5 -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void toggleVisibility(Internship internship) {
        if (internship.getStatus() != InternshipStatus.APPROVED
                && internship.getStatus() != InternshipStatus.FILLED) {
            System.out.println("Internship must be approved before it can be made visible to students.");
            return;
        }
        internship.setVisible(!internship.isVisible());
        System.out.println("Visibility updated. Now " + (internship.isVisible() ? "Visible" : "Hidden"));
    }

    private static void editInternship(Internship internship) {
        if (internship.getStatus() == InternshipStatus.APPROVED
                || internship.getStatus() == InternshipStatus.FILLED) {
            System.out.println("Approved or filled internships cannot be edited.");
            return;
        }

        LocalDate originalOpening = internship.getOpeningDate();
        LocalDate originalClosing = internship.getClosingDate();

        System.out.print("Enter new title (leave blank to keep '" + internship.getInternshipTitle() + "'): ");
        String input = SCANNER.nextLine().trim();
        if (!input.isEmpty()) {
            internship.setInternshipTitle(input);
        }

        System.out.print("Enter new description (leave blank to keep current): ");
        input = SCANNER.nextLine().trim();
        if (!input.isEmpty()) {
            internship.setDescription(input);
        }

        InternshipLevel level = promptLevel("Enter new level (Basic/Intermediate/Advanced) or leave blank: ");
        if (level != null) {
            internship.setLevel(level);
        }

        System.out.print("Enter preferred major (leave blank to keep '" + internship.getPreferredMajor() + "'): ");
        input = SCANNER.nextLine().trim();
        if (!input.isEmpty()) {
            internship.setPreferredMajor(input);
        }

        LocalDate date = promptDate("Enter new opening date (YYYY-MM-DD) or leave blank: ");
        if (date != null) {
            internship.setOpeningDate(date);
        }
        date = promptDate("Enter new closing date (YYYY-MM-DD) or leave blank: ");
        if (date != null) {
            internship.setClosingDate(date);
        }

        int slots = promptSlots("Enter new number of slots (1-10) or 0 to keep current: ");
        if (slots > 0) {
            internship.setSlots(slots);
        }

        if (internship.getOpeningDate() != null && internship.getClosingDate() != null
                && internship.getClosingDate().isBefore(internship.getOpeningDate())) {
            System.out.println("Closing date cannot be earlier than opening date. Reverting date changes.");
            internship.setOpeningDate(originalOpening);
            internship.setClosingDate(originalClosing);
        }

        System.out.println("Internship updated. Please resubmit to staff if required.");
    }

    private static boolean deleteInternship(Internship internship) {
        if (internship.getStatus() == InternshipStatus.APPROVED
                || internship.getStatus() == InternshipStatus.FILLED) {
            System.out.println("You cannot delete an internship after it has been approved.");
            return false;
        }
        List<Application> applications = DB.getApplicationsByInternship(internship.getInternshipID());
        if (!applications.isEmpty()) {
            System.out.println("Cannot delete internship as there are existing applications.");
            return false;
        }
        DB.removeInternship(internship);
        System.out.println("Internship deleted successfully.");
        return true;
    }

    private static void manageApplications(CompanyRepresentative rep, Internship internship) {
        List<Application> applications = DB.getApplicationsByInternship(internship.getInternshipID());
        if (applications.isEmpty()) {
            System.out.println("No applications received yet.");
            return;
        }

        boolean back = false;
        while (!back) {
            applications.sort(Comparator.comparing(Application::getApplicationID));
            System.out.println("\n--- Applications for " + internship.getInternshipTitle() + " ---");
            applications.forEach(CompanyRepUI::printApplicationSummary);
            System.out.println("1. Update application status");
            System.out.println("2. View student profile");
            System.out.println("3. Back");
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> updateApplicationStatus(applications, internship);
                case 2 -> viewStudentProfile(applications);
                case 3 -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void updateApplicationStatus(List<Application> applications, Internship internship) {
        int applicationId = readInt("Enter application ID: ");
        Application application = applications.stream()
                .filter(app -> app.getApplicationID() == applicationId)
                .findFirst()
                .orElse(null);
        if (application == null) {
            System.out.println("Invalid application ID.");
            return;
        }
        if (application.getWithdrawalStatus() == WithdrawalStatus.APPROVED) {
            System.out.println("This application has been withdrawn and cannot be updated.");
            return;
        }

        System.out.print("Enter new status (Pending/Successful/Unsuccessful): ");
        String input = SCANNER.nextLine().trim();
        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown status. Update cancelled.");
            return;
        }

        if (newStatus == ApplicationStatus.SUCCESSFUL || newStatus == ApplicationStatus.PENDING) {
            application.setWithdrawalStatus(WithdrawalStatus.NONE);
        }
        if (newStatus != ApplicationStatus.SUCCESSFUL) {
            application.setAccepted(false);
        }

        application.setStatus(newStatus);
        System.out.println("Application status updated to " + newStatus + ".");
        DB.updateInternshipFilledStatus(internship);
    }

    private static void viewStudentProfile(List<Application> applications) {
        int applicationId = readInt("Enter application ID: ");
        Application application = applications.stream()
                .filter(app -> app.getApplicationID() == applicationId)
                .findFirst()
                .orElse(null);
        if (application == null) {
            System.out.println("Invalid application ID.");
            return;
        }
        Student student = DB.getStudents().stream()
                .filter(s -> s.getUserID().equals(application.getStudentID()))
                .findFirst()
                .orElse(null);
        if (student == null) {
            System.out.println("Student record not found (may have been removed).");
            return;
        }
        System.out.println("\n--- Student Profile ---");
        System.out.println("Student ID : " + student.getUserID());
        System.out.println("Name       : " + student.getName());
        System.out.println("Major      : " + student.getMajor());
        System.out.println("Year       : " + student.getYearOfStudy());
    }

    private static void updateFilters(RepFilter filter) {
        boolean done = false;
        while (!done) {
            System.out.println("\n--- Update Filters ---");
            System.out.println("1. Status filter (current: " + (filter.status == null ? "All" : filter.status) + ")");
            System.out.println("2. Level filter (current: " + (filter.level == null ? "All" : filter.level) + ")");
            System.out.println("3. Visibility filter (current: " + (filter.visibleOnly == null ? "All" : filter.visibleOnly) + ")");
            System.out.println("4. Back");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> filter.status = promptStatus();
                case 2 -> filter.level = promptLevel("Enter level (Basic/Intermediate/Advanced) or leave blank: ");
                case 3 -> {
                    System.out.print("Show only visible internships? (yes/no/blank for all): ");
                    String input = SCANNER.nextLine().trim().toLowerCase();
                    if (input.isEmpty()) {
                        filter.visibleOnly = null;
                    } else if (input.startsWith("y")) {
                        filter.visibleOnly = Boolean.TRUE;
                    } else if (input.startsWith("n")) {
                        filter.visibleOnly = Boolean.FALSE;
                    } else {
                        System.out.println("Invalid choice. Filter unchanged.");
                    }
                }
                case 4 -> done = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static InternshipLevel promptLevel(String message) {
        System.out.print(message);
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return InternshipLevel.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown level. Keeping existing value.");
            return null;
        }
    }

    private static InternshipStatus promptStatus() {
        System.out.print("Enter status (Pending/Approved/Rejected/Filled) or leave blank: ");
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return InternshipStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown status. Keeping previous value.");
            return null;
        }
    }

    private static LocalDate promptDate(String message) {
        System.out.print(message);
        String input = SCANNER.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Value unchanged.");
            return null;
        }
    }

    private static int promptSlots(String message) {
        while (true) {
            System.out.print(message);
            String input = SCANNER.nextLine().trim();
            if (input.isEmpty()) {
                return 0;
            }
            try {
                int value = Integer.parseInt(input);
                if (value == 0) {
                    return 0;
                }
                if (value < 1 || value > 10) {
                    System.out.println("Slots must be between 1 and 10.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static void printInternship(Internship internship) {
        System.out.println("[" + internship.getInternshipID() + "] " + internship.getInternshipTitle());
        System.out.println("    Status       : " + internship.getStatus());
        System.out.println("    Level        : " + internship.getLevel());
        System.out.println("    Preferred Major: " + internship.getPreferredMajor());
        System.out.println("    Opening Date : " + internship.getOpeningDate());
        System.out.println("    Closing Date : " + internship.getClosingDate());
        System.out.println("    Slots        : " + internship.getSlots());
        System.out.println("    Visible      : " + (internship.isVisible() ? "Yes" : "No"));
    }

    private static void printApplicationSummary(Application application) {
        Student student = DB.getStudents().stream()
                .filter(s -> s.getUserID().equals(application.getStudentID()))
                .findFirst()
                .orElse(null);
        String studentInfo = student == null
                ? application.getStudentID()
                : student.getName() + " (" + student.getUserID() + ", " + student.getMajor() + ", Yr " + student.getYearOfStudy() + ")";
        System.out.println("Application ID: " + application.getApplicationID());
        System.out.println("    Student    : " + studentInfo);
        System.out.println("    Status     : " + application.getStatus());
        System.out.println("    Accepted   : " + (application.isAccepted() ? "Yes" : "No"));
        System.out.println("    Withdrawal : " + application.getWithdrawalStatus());
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

    private static void createInternship(CompanyRepresentative rep) {
        List<Internship> myInternships = DB.getInternshipsByCompanyRep(rep.getUserID());
        if (myInternships.size() >= MAX_INTERNSHIPS) {
            System.out.println("You have reached the maximum of " + MAX_INTERNSHIPS + " internship postings.");
            return;
        }

        System.out.println("\n--- Create Internship ---");
        System.out.print("Title: ");
        String title = SCANNER.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }
        System.out.print("Description: ");
        String description = SCANNER.nextLine().trim();
        InternshipLevel level = promptLevel("Level (Basic/Intermediate/Advanced): ");
        if (level == null) {
            System.out.println("Invalid level provided. Creation cancelled.");
            return;
        }
        System.out.print("Preferred major: ");
        String major = SCANNER.nextLine().trim();
        if (major.isEmpty()) {
            System.out.println("Preferred major cannot be empty.");
            return;
        }
        LocalDate openingDate = promptDate("Opening date (YYYY-MM-DD): ");
        LocalDate closingDate = promptDate("Closing date (YYYY-MM-DD): ");
        if (openingDate == null || closingDate == null) {
            System.out.println("Opening and closing dates are required.");
            return;
        }
        if (openingDate != null && closingDate != null && closingDate.isBefore(openingDate)) {
            System.out.println("Closing date cannot be before opening date.");
            return;
        }
        int slots = promptSlots("Number of slots (1-10): ");
        if (slots <= 0) {
            System.out.println("Invalid slot count.");
            return;
        }

        Internship internship = new Internship(title, description, level, major, openingDate, closingDate,
                rep.getCompanyName(), rep.getUserID(), slots);
        DB.addInternship(internship);
        System.out.println("Internship created successfully and is pending approval by Career Center Staff.");
    }

    private static class RepFilter {
        private InternshipStatus status;
        private InternshipLevel level;
        private Boolean visibleOnly;

        List<Internship> apply(List<Internship> internships) {
            List<Internship> filtered = new ArrayList<>(internships);
            if (status != null) {
                filtered = filtered.stream()
                        .filter(internship -> internship.getStatus() == status)
                        .collect(Collectors.toList());
            }
            if (level != null) {
                filtered = filtered.stream()
                        .filter(internship -> internship.getLevel() == level)
                        .collect(Collectors.toList());
            }
            if (visibleOnly != null) {
                filtered = filtered.stream()
                        .filter(internship -> internship.isVisible() == visibleOnly)
                        .collect(Collectors.toList());
            }
            filtered.sort(Comparator.comparing(Internship::getInternshipTitle, String.CASE_INSENSITIVE_ORDER));
            return filtered;
        }

        void reset() {
            status = null;
            level = null;
            visibleOnly = null;
        }

        @Override
        public String toString() {
            return "Status=" + (status == null ? "All" : status)
                    + ", Level=" + (level == null ? "All" : level)
                    + ", VisibleOnly=" + (visibleOnly == null ? "All" : visibleOnly);
        }
    }
}
