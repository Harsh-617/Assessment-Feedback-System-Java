import java.util.*;

/**
 * AdminStaff.java - Admin Role Logic
 * OOP Concept: Encapsulation — all fields private, accessed via public methods only
 */
public class AdminStaff {
    private String userID;
    private String name;
    private String email;
    private String phone;
    private String age;
    private String gender;
    private String password;

    public AdminStaff(String userID) {
        this.userID = userID;
        loadProfile();
    }

    private void loadProfile() {
        for (String line : Helpers.readFromFile("users.txt")) {
            String[] p = line.split("\\|");
            if (p.length >= 8 && p[0].equals(this.userID)) {
                this.password = p[2]; this.name = p[3]; this.gender = p[4];
                this.email = p[5];   this.phone = p[6]; this.age = p[7];
                break;
            }
        }
    }

    // ==========================================
    // 1. USER MANAGEMENT (CRUD)
    // ==========================================
    public String createUser(String role, String name, String gender,
                          String email, String phone, String age, String password) {
        if (!Helpers.validateEmail(email)) return null;
        if (name == null || name.trim().isEmpty()) return null;
        if (password == null || password.trim().isEmpty()) return null;

        // ADD THIS: Check for duplicate email
        List<String[]> existingUsers = getAllUsers();
        for (String[] user : existingUsers) {
            if (user[5].equals(email)) { // user[5] is email
                return null; // Email already exists
            }
        }

        String prefix;
        switch (role) {
            case "Lecturer":        prefix = "LEC"; break;
            case "Academic Leader": prefix = "AL";  break;
            default:                prefix = "STU"; break;
        }
        String newID = Helpers.generateUniqueID(prefix, "users.txt");
        Helpers.writeToFile("users.txt",
            newID + "|" + role + "|" + password + "|" + name
            + "|" + gender + "|" + email + "|" + phone + "|" + age);
        return newID;
    }

    public List<String[]> getAllUsers() {
        List<String[]> users = new ArrayList<>();
        for (String line : Helpers.readFromFile("users.txt")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 8) users.add(p);
        }
        return users;
    }

    public boolean updateUser(String targetID, String newName, String newGender,
                               String newEmail, String newPhone, String newAge) {
        if (!Helpers.validateEmail(newEmail) || newName == null || newName.trim().isEmpty()) return false;
        for (String line : Helpers.readFromFile("users.txt")) {
            String[] p = line.split("\\|");
            if (p.length >= 8 && p[0].equals(targetID)) {
                return Helpers.updateRecord("users.txt", targetID,
                    p[0] + "|" + p[1] + "|" + p[2] + "|" + newName
                    + "|" + newGender + "|" + newEmail + "|" + newPhone + "|" + newAge);
            }
        }
        return false;
    }

    public boolean deleteUser(String targetID) {
        if (targetID.equals(this.userID)) return false;
        return Helpers.deleteRecord("users.txt", targetID);
    }

    public boolean resetPassword(String targetID, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) return false;
        for (String line : Helpers.readFromFile("users.txt")) {
            String[] p = line.split("\\|");
            if (p.length >= 8 && p[0].equals(targetID)) {
                p[2] = newPassword;
                return Helpers.updateRecord("users.txt", targetID, String.join("|", p));
            }
        }
        return false;
    }

    // ==========================================
    // 2. ASSIGN LECTURER TO ACADEMIC LEADER (Admin requirement per spec)
    // File: leaderAssignments.txt  format: leaderID|lecturerID
    // ==========================================
    public boolean assignLecturerToLeader(String leaderID, String lecturerID) {
        if (leaderID == null || lecturerID == null) return false;
        boolean leaderOk = false, lecturerOk = false;
        for (String line : Helpers.readFromFile("users.txt")) {
            String[] p = line.split("\\|");
            if (p.length >= 2) {
                if (p[0].equals(leaderID)   && p[1].equals("Academic Leader")) leaderOk = true;
                if (p[0].equals(lecturerID) && p[1].equals("Lecturer"))        lecturerOk = true;
            }
        }
        if (!leaderOk || !lecturerOk) return false;

        // Prevent duplicates
        for (String line : Helpers.readFromFile("leaderAssignments.txt")) {
            String[] p = line.split("\\|");
            if (p.length >= 2 && p[0].equals(leaderID) && p[1].equals(lecturerID)) return false;
        }
        Helpers.writeToFile("leaderAssignments.txt", leaderID + "|" + lecturerID);
        return true;
    }

    public List<String[]> getAllLeaderAssignments() {
        List<String[]> result = new ArrayList<>();
        for (String line : Helpers.readFromFile("leaderAssignments.txt")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 2) result.add(p);
        }
        return result;
    }

    // ==========================================
    // 3. CREATE CLASS FOR A MODULE (Admin requirement per spec)
    // File: classes.txt  format: classID|className|moduleID
    // ==========================================
    public String createClass(String className, String moduleID) {
        if (className == null || className.trim().isEmpty()) return null;
        if (moduleID  == null || moduleID.trim().isEmpty())  return null;

        // Verify module exists
        boolean moduleExists = false;
        for (String line : Helpers.readFromFile("modules.txt")) {
            String[] p = line.split("\\|");
            if (p.length >= 1 && p[0].equals(moduleID)) { moduleExists = true; break; }
        }
        if (!moduleExists) return null;

        String classID = Helpers.generateUniqueID("CLS", "classes.txt");
        Helpers.writeToFile("classes.txt", classID + "|" + className + "|" + moduleID);
        return classID;
    }

    public List<String[]> getAllClasses() {
        List<String[]> result = new ArrayList<>();
        for (String line : Helpers.readFromFile("classes.txt")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 3) result.add(p);
        }
        return result;
    }

    public boolean deleteClass(String classID) {
        return Helpers.deleteRecord("classes.txt", classID);
    }

    // ==========================================
    // 4. GRADING SYSTEM
    // ==========================================
    public List<String[]> getGradingSystem() {
        List<String[]> grades = new ArrayList<>();
        for (String line : Helpers.readFromFile("grading.txt")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 3) grades.add(p);
        }
        return grades;
    }

    public boolean saveGradingSystem(List<String> gradingLines) {
        if (gradingLines == null || gradingLines.isEmpty()) return false;
        Helpers.overwriteFile("grading.txt", gradingLines);
        return true;
    }

    // ==========================================
    // 5. VIEW ALL MODULES
    // ==========================================
    public List<String[]> getAllModules() {
        List<String[]> modules = new ArrayList<>();
        for (String line : Helpers.readFromFile("modules.txt")) {
            if (line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 3) modules.add(p);
        }
        return modules;
    }

    // ==========================================
    // 6. UPDATE OWN PROFILE
    // ==========================================
    public boolean updateProfile(String newName, String newEmail, String newPhone, String newAge) {
        if (!Helpers.validateEmail(newEmail) || newName == null || newName.trim().isEmpty()) return false;
        String updated = userID + "|Admin|" + password + "|" + newName
                         + "|" + gender + "|" + newEmail + "|" + newPhone + "|" + newAge;
        boolean success = Helpers.updateRecord("users.txt", userID, updated);
        if (success) { this.name = newName; this.email = newEmail; this.phone = newPhone; this.age = newAge; }
        return success;
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public String getUserID() { return userID; }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
    public String getPhone()  { return phone; }
    public String getAge()    { return age; }
}