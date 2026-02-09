import java.util.ArrayList;
import java.util.List;

public class Student {
    private String id;
    private String name;
    private String password;
    private String gender;
    private String email;
    private String phone;
    private String age;

    // Constructor to hold logged-in user details
    public Student(String id, String name, String password, String gender, String email, String phone, String age) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.age = age;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    // ==========================================
    // 1. REGISTER STUDENT (Self-Registration)
    // ==========================================
    public static boolean registerStudent(String pass, String name, String gender, String email, String phone, String age) {
        // Validation (Page 8 Requirement)
        if (!Helpers.validateEmail(email)) {
            return false; // Invalid email
        }

        // Generate ID (Page 7 Requirement: "STU", "users.txt")
        String newID = Helpers.generateUniqueID("STU", "users.txt");

        // Format: userID|role|password|name|gender|email|phone|age
        String record = newID + "|Student|" + pass + "|" + name + "|" + gender + "|" + email + "|" + phone + "|" + age;
        
        // Save
        Helpers.writeToFile("users.txt", record);
        return true;
    }

    // ==========================================
    // 2. REGISTER FOR CLASSES
    // ==========================================
    public boolean registerForClasses(String classID) {
        // Prevent duplicate enrollment
        List<String> enrollments = Helpers.readFromFile("enrollments.txt");
        for (String line : enrollments) {
            String[] parts = line.split("\\|");
            // Format: studentID|classID
            if (parts.length >= 2 && parts[0].equals(this.id) && parts[1].equals(classID)) {
                return false; // Already enrolled
            }
        }

        // Save to enrollments.txt
        String record = this.id + "|" + classID;
        Helpers.writeToFile("enrollments.txt", record);
        return true;
    }

    // ==========================================
    // 3. SUBMIT COMMENTS
    // ==========================================
    public void submitComments(String lecturerID, String comment) {
        // Format: studentID|lecturerID|comment (Page 12)
        String record = this.id + "|" + lecturerID + "|" + comment;
        Helpers.writeToFile("comments.txt", record);
    }

    // ==========================================
    // 4. UPDATE PROFILE
    // ==========================================
    public void updateProfile(String newPass, String newName, String newGender, String newEmail, String newPhone, String newAge) {
        List<String> users = Helpers.readFromFile("users.txt");
        List<String> updatedUsers = new ArrayList<>();

        for (String line : users) {
            String[] parts = line.split("\\|");
            if (parts[0].equals(this.id)) {
                // Replace with new data, keeping ID and Role (index 1) same
                String updatedLine = this.id + "|Student|" + newPass + "|" + newName + "|" + newGender + "|" + newEmail + "|" + newPhone + "|" + newAge;
                updatedUsers.add(updatedLine);
                
                // Update local object
                this.password = newPass;
                this.name = newName;
                this.gender = newGender;
                this.email = newEmail;
                this.phone = newPhone;
                this.age = newAge;
            } else {
                updatedUsers.add(line);
            }
        }

        // Re-write the file (This implies Member 1 needs a "writeAllToFile" or we overwrite users.txt manually)
        // For this assignment using append-only helpers, we technically usually append, 
        // but for updates, we must overwrite.
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter("users.txt"))) {
            for (String line : updatedUsers) {
                bw.write(line);
                bw.newLine();
            }
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    // ==========================================
    // 5. VIEW RESULTS 
    // ==========================================
    public List<String[]> viewResults() {
        List<String[]> myResults = new ArrayList<>();
        
        // Step A: Get Class IDs from enrollments.txt
        List<String> myClassIDs = new ArrayList<>();
        List<String> enrollments = Helpers.readFromFile("enrollments.txt");
        for (String line : enrollments) {
            String[] parts = line.split("\\|");
            if (parts[0].equals(this.id)) {
                myClassIDs.add(parts[1]); // Found classID
            }
        }

        // Step B: Get Module IDs from classes.txt using Class IDs
        List<String> myModuleIDs = new ArrayList<>();
        List<String> classes = Helpers.readFromFile("classes.txt");
        for (String line : classes) {
            String[] parts = line.split("\\|");
            // Format: classID|className|moduleID
            if (parts.length >= 3 && myClassIDs.contains(parts[0])) {
                myModuleIDs.add(parts[2]); // Found moduleID
            }
        }

        // Step C: Filter results.txt
        List<String> results = Helpers.readFromFile("results.txt");
        for (String line : results) {
            String[] parts = line.split("\\|");
            // Format: studentID|moduleID|assessmentID|marks|feedback
            if (parts.length >= 5) {
                String rStuID = parts[0];
                String rModID = parts[1];
                
                if (rStuID.equals(this.id) && myModuleIDs.contains(rModID)) {
                    // Add to list for display
                    myResults.add(parts);
                }
            }
        }
        return myResults;
    }
}