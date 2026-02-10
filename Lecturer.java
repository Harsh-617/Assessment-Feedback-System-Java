import java.util.*;

/**
 * Lecturer.java - Lecturer Role Logic
 * OOP Concept: Encapsulation — private fields, public accessor methods
 */
public class Lecturer {
    private String userID;
    private String name;
    private String email;
    private String phone;
    private String age;
    private String gender;
    private String password;

    public Lecturer(String userID) {
        this.userID = userID;
        loadCurrentProfile();
    }

    // ==========================================
    // LOAD PROFILE
    // ==========================================
    private void loadCurrentProfile() {
        for (String line : Helpers.readFromFile("users.txt")) {
            String[] d = line.split("\\|");
            if (d.length >= 8 && d[0].equals(this.userID)) {
                this.password = d[2]; this.name   = d[3];
                this.gender   = d[4]; this.email  = d[5];
                this.phone    = d[6]; this.age    = d[7];
                break;
            }
        }
    }

    // ==========================================
    // 1. UPDATE PROFILE
    // ==========================================
    public boolean updateProfile(String newName, String newEmail, String newPhone, String newAge) {
        if (newName  == null || newName.trim().isEmpty())  return false;
        if (newPhone == null || newPhone.trim().isEmpty()) return false;
        if (newAge   == null || newAge.trim().isEmpty())   return false;
        if (!Helpers.validateEmail(newEmail))              return false;

        List<String> users = Helpers.readFromFile("users.txt");
        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            String[] d = users.get(i).split("\\|");
            if (d.length >= 8 && d[0].equals(this.userID)) {
                d[3] = newName; d[5] = newEmail; d[6] = newPhone; d[7] = newAge;
                users.set(i, String.join("|", d));
                updated = true;
                break;
            }
        }
        if (updated) {
            Helpers.overwriteFile("users.txt", users);
            this.name = newName; this.email = newEmail;
            this.phone = newPhone; this.age = newAge;
            return true;
        }
        return false;
    }

    // ==========================================
    // 2. DESIGN ASSESSMENT
    // ==========================================
    public String designAssessment(String moduleID, String asName, String weightage) {
        if (moduleID  == null || moduleID.trim().isEmpty())  return null;
        if (asName    == null || asName.trim().isEmpty())    return null;
        if (weightage == null || weightage.trim().isEmpty()) return null;

        // Validate weightage is numeric
        try { Double.parseDouble(weightage); }
        catch (NumberFormatException e) { return null; }

        String asID = Helpers.generateUniqueID("AS", "assessments.txt");
        // Format: assessmentID|moduleID|name|weightage
        Helpers.writeToFile("assessments.txt", asID + "|" + moduleID + "|" + asName + "|" + weightage);
        return asID;
    }

    // ==========================================
    // 3. GET ASSIGNED MODULES
    // ==========================================
    public List<String> getMyAssignedModules() {
        List<String> assigned = new ArrayList<>();
        for (String line : Helpers.readFromFile("assignments.txt")) {
            String[] d = line.split("\\|");
            // Format: leaderID|lecturerID|moduleID
            if (d.length >= 3 && d[1].equals(this.userID)) {
                assigned.add(d[2]);
            }
        }
        return assigned;
    }

    // Get full module details for assigned modules
    public List<String[]> getMyAssignedModuleDetails() {
        List<String> myModuleIDs = getMyAssignedModules();
        List<String[]> details = new ArrayList<>();
        for (String line : Helpers.readFromFile("modules.txt")) {
            String[] d = line.split("\\|");
            if (d.length >= 2 && myModuleIDs.contains(d[0])) {
                details.add(d);
            }
        }
        return details;
    }

    // ==========================================
    // 4. INPUT MARKS & FEEDBACK
    // ==========================================
    public boolean inputMarks(String stuID, String modID, String asID, String marks, String feedback) {
        if (stuID == null || stuID.trim().isEmpty()) return false;
        if (modID == null || modID.trim().isEmpty()) return false;
        if (asID  == null || asID.trim().isEmpty())  return false;
        if (marks == null || marks.trim().isEmpty()) return false;
        if (feedback == null) feedback = "";

        // Validate marks is numeric
        try { Double.parseDouble(marks); }
        catch (NumberFormatException e) { return false; }

        // Check if record already exists — update if so
        List<String> results = Helpers.readFromFile("results.txt");
        for (int i = 0; i < results.size(); i++) {
            String[] d = results.get(i).split("\\|");
            if (d.length >= 3 && d[0].equals(stuID) && d[1].equals(modID) && d[2].equals(asID)) {
                results.set(i, stuID + "|" + modID + "|" + asID + "|" + marks + "|" + feedback);
                Helpers.overwriteFile("results.txt", results);
                return true;
            }
        }
        // New record
        Helpers.writeToFile("results.txt", stuID + "|" + modID + "|" + asID + "|" + marks + "|" + feedback);
        return true;
    }

    // ==========================================
    // 5. VIEW STUDENT COMMENTS
    // ==========================================
    public List<String[]> getLecturerComments() {
        List<String[]> myComments = new ArrayList<>();
        for (String line : Helpers.readFromFile("comments.txt")) {
            String[] d = line.split("\\|");
            // Format: studentID|lecturerID|comment
            if (d.length >= 3 && d[1].equals(this.userID)) {
                myComments.add(new String[]{d[0], d[2]});
            }
        }
        return myComments;
    }

    // ==========================================
    // 6. GET ASSESSMENTS FOR A MODULE
    // ==========================================
    public List<String[]> getAssessmentsForModule(String moduleID) {
        List<String[]> list = new ArrayList<>();
        for (String line : Helpers.readFromFile("assessments.txt")) {
            String[] d = line.split("\\|");
            // Format: assessmentID|moduleID|name|weightage
            if (d.length >= 4 && d[1].equals(moduleID)) {
                list.add(d);
            }
        }
        return list;
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public String getUserID() { return userID; }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
    public String getPhone()  { return phone; }
    public String getAge()    { return age; }
    public String getGender() { return gender; }
}