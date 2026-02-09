/**
 * Main.java - Entry Point & Gateway
 * Handles system initialization, login, and role-based navigation
 */
public class Main {
    
    public static void main(String[] args) {
        // Step 1: System Initialization (Bootstrap)
        systemInitialization();
        
        // Step 2: Launch Login GUI
        new LoginGUI();
    }
    
    // ==========================================
    // SYSTEM INITIALIZATION (BOOTSTRAP)
    // ==========================================
    /**
     * Ensures the system always has a default Admin account
     * Runs before login screen appears
     * Creates default admin if users.txt is missing or empty
     */
    private static void systemInitialization() {
        // Check if users.txt exists and has content
        if (!Helpers.fileExistsAndNotEmpty("users.txt")) {
            System.out.println("Initializing system with default admin account...");
            
            // Create default admin account
            // Format: userID|role|password|name|gender|email|phone|age
            String defaultAdmin = "ADM01|Admin|admin123|System Admin|Male|admin@apu.my|000|99";
            Helpers.writeToFile("users.txt", defaultAdmin);
            
            System.out.println("Default Admin created: Email=admin@apu.my, Password=admin123");
        }
        
        // Initialize other files if they don't exist (prevents errors)
        String[] dataFiles = {
            "modules.txt",
            "classes.txt", 
            "assignments.txt",
            "enrollments.txt",
            "grading.txt",
            "assessments.txt",
            "results.txt",
            "comments.txt"
        };
        
        for (String file : dataFiles) {
            if (!Helpers.fileExistsAndNotEmpty(file)) {
                // Create empty file
                Helpers.writeToFile(file, "");
                // Remove the empty line we just wrote
                java.util.List<String> lines = Helpers.readFromFile(file);
                lines.clear();
                Helpers.overwriteFile(file, lines);
            }
        }
        
        System.out.println("System initialization complete.");
    }
    
    // ==========================================
    // LOGIN USER (Called from LoginGUI)
    // ==========================================
    /**
     * Authenticates user and opens appropriate dashboard
     * 
     * @param email User's email address
     * @param password User's password
     * @return true if login successful, false otherwise
     */
    public static boolean loginUser(String email, String password) {
        java.util.List<String> users = Helpers.readFromFile("users.txt");
        
        for (String line : users) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            // Format: userID|role|password|name|gender|email|phone|age
            if (parts.length >= 8) {
                String storedEmail = parts[5];
                String storedPassword = parts[2];
                
                if (storedEmail.equals(email) && storedPassword.equals(password)) {
                    // Login successful - dispatch to appropriate dashboard
                    String userID = parts[0];
                    String role = parts[1];
                    String name = parts[3];
                    
                    dispatchUserByRole(userID, role, name);
                    return true;
                }
            }
        }
        
        return false; // Login failed
    }
    
    // ==========================================
    // DISPATCH USER BY ROLE
    // ==========================================
    /**
     * Opens the appropriate dashboard based on user role
     * 
     * @param userID The user's unique ID
     * @param role The user's role (Admin, Student, Lecturer, Academic Leader)
     * @param name The user's name
     */
    private static void dispatchUserByRole(String userID, String role, String name) {
        switch (role) {
            case "Admin":
                // Open Admin Dashboard
                new AdminGUI(userID, name).setVisible(true);
                break;
                
            case "Student":
                // Create Student object and open Student Dashboard
                Student student = getUserAsStudent(userID);
                if (student != null) {
                    new StudentGUI(student);
                }
                break;
                
            case "Lecturer":
                // Open Lecturer Dashboard
                new LecturerGUI(userID, name).setVisible(true);
                break;
                
            case "Academic Leader":
                // Open Academic Leader Dashboard
                new AcademicLeaderGUI(userID, name).setVisible(true);
                break;
                
            default:
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Unknown role: " + role, 
                    "Error", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ==========================================
    // HELPER: GET USER AS STUDENT OBJECT
    // ==========================================
    /**
     * Creates a Student object from users.txt data
     * 
     * @param userID The student's ID
     * @return Student object or null if not found
     */
    private static Student getUserAsStudent(String userID) {
        java.util.List<String> users = Helpers.readFromFile("users.txt");
        
        for (String line : users) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 8 && parts[0].equals(userID)) {
                // Format: userID|role|password|name|gender|email|phone|age
                return new Student(
                    parts[0], // id
                    parts[3], // name
                    parts[2], // password
                    parts[4], // gender
                    parts[5], // email
                    parts[6], // phone
                    parts[7]  // age
                );
            }
        }
        
        return null;
    }
}