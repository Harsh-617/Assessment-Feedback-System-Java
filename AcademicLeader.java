import java.util.*;

public class AcademicLeader {
    private String userID;
    private String name;
    private String email;
    private String password;
    private String gender;
    private String phone;
    private String age;

    // Constructor - loads user data when object is created
    public AcademicLeader(String userID) {
        this.userID = userID;
        loadProfile();
    }

    // LOAD PROFILE FROM FILE
    private void loadProfile() {
        List<String> users = Helpers.readFromFile("users.txt");
        for (String line : users) {
            String[] parts = line.split("\\|");
            if (parts.length >= 8 && parts[0].equals(this.userID)) {
                this.password = parts[2];
                this.name = parts[3];
                this.gender = parts[4];
                this.email = parts[5];
                this.phone = parts[6];
                this.age = parts[7];
                break;
            }
        }
    }

    // UPDATE PROFILE
    public boolean updateProfile(String newName, String newEmail, String newPhone, String newAge) {
        // Validate email first
        if (!Helpers.validateEmail(newEmail)) {
            return false;
        }
        
        // Create updated record string
        String updatedRecord = userID + "|Academic Leader|" + password + "|" + 
                               newName + "|" + gender + "|" + newEmail + "|" + 
                               newPhone + "|" + newAge;
        
        // Update in file
        boolean success = Helpers.updateRecord("users.txt", userID, updatedRecord);
        
        if (success) {
            // Update local variables too
            this.name = newName;
            this.email = newEmail;
            this.phone = newPhone;
            this.age = newAge;
        }
        
        return success;
    }

    // MODULE MANAGEMENT

    // CREATE MODULE (Max 3 per leader)
    public String createModule(String moduleName) {
        // Check if already have 3 modules
        if (getMyModules().size() >= 3) {
            return null;
        }
        
        // Generate new ID
        String moduleID = Helpers.generateUniqueID("MOD", "modules.txt");
        
        // Save to file: moduleID|moduleName|leaderID
        String record = moduleID + "|" + moduleName + "|" + userID;
        Helpers.writeToFile("modules.txt", record);
        
        return moduleID;
    }
    
    // GET MY MODULES
    public List<String[]> getMyModules() {
        List<String[]> myModules = new ArrayList<>();
        List<String> allModules = Helpers.readFromFile("modules.txt");
        
        for (String line : allModules) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 3 && parts[2].equals(userID)) {
                myModules.add(parts);
            }
        }
        
        return myModules;
    }
    
    // UPDATE MODULE
    public boolean updateModule(String moduleID, String newName) {
        // Check if this module belongs to me
        boolean isMyModule = false;
        for (String[] module : getMyModules()) {
            if (module[0].equals(moduleID)) {
                isMyModule = true;
                break;
            }
        }
        
        if (!isMyModule) return false;
        
        // Update the record
        String updatedRecord = moduleID + "|" + newName + "|" + userID;
        return Helpers.updateRecord("modules.txt", moduleID, updatedRecord);
    }
    
    // DELETE MODULE
    public boolean deleteModule(String moduleID) {
        // Check if this module belongs to me
        boolean isMyModule = false;
        for (String[] module : getMyModules()) {
            if (module[0].equals(moduleID)) {
                isMyModule = true;
                break;
            }
        }
        
        if (!isMyModule) return false;
        
        // Delete from modules.txt
        return Helpers.deleteRecord("modules.txt", moduleID);
    }

    // ASSIGN LECTURER TO MODULE
    public boolean assignLecturer(String lecturerID, String moduleID) {
        // Check if module is mine
        boolean isMyModule = false;
        for (String[] module : getMyModules()) {
            if (module[0].equals(moduleID)) {
                isMyModule = true;
                break;
            }
        }
        
        if (!isMyModule) return false;
        
        // Check if assignment already exists for this module
        List<String> assignments = Helpers.readFromFile("assignments.txt");
        boolean exists = false;
        
        for (String line : assignments) {
            String[] parts = line.split("\\|");
            if (parts.length >= 3 && parts[2].equals(moduleID)) {
                exists = true;
                break;
            }
        }
        
        if (exists) {
            // Update existing assignment
            List<String> updated = new ArrayList<>();
            for (String line : assignments) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3 && parts[2].equals(moduleID)) {
                    updated.add(userID + "|" + lecturerID + "|" + moduleID);
                } else {
                    updated.add(line);
                }
            }
            Helpers.overwriteFile("assignments.txt", updated);
        } else {
            // Create new assignment: leaderID|lecturerID|moduleID
            String record = userID + "|" + lecturerID + "|" + moduleID;
            Helpers.writeToFile("assignments.txt", record);
        }
        
        return true;
    }
    
    // GET MY ASSIGNMENTS
    public List<String[]> getMyAssignments() {
        List<String[]> myAssignments = new ArrayList<>();
        List<String> assignments = Helpers.readFromFile("assignments.txt");
        
        for (String line : assignments) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 3 && parts[0].equals(userID)) {
                myAssignments.add(parts);
            }
        }
        
        return myAssignments;
    }

    // REPORT 1: GRADE DISTRIBUTION
    public String getGradeDistributionReport() {
        Map<String, Integer> gradeCount = new LinkedHashMap<>();
        
        // Load grading system
        List<String> grading = Helpers.readFromFile("grading.txt");
        for (String line : grading) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 1) {
                gradeCount.put(parts[0], 0);
            }
        }
        
        // Get my module IDs
        Set<String> myModuleIDs = new HashSet<>();
        for (String[] module : getMyModules()) {
            myModuleIDs.add(module[0]);
        }
        
        // Count grades from results
        List<String> results = Helpers.readFromFile("results.txt");
        for (String line : results) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 4 && myModuleIDs.contains(parts[1])) {
                try {
                    double marks = Double.parseDouble(parts[3]);
                    String grade = getGradeFromMarks(marks);
                    if (gradeCount.containsKey(grade)) {
                        gradeCount.put(grade, gradeCount.get(grade) + 1);
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        // Build report string
        StringBuilder report = new StringBuilder();
        for (Map.Entry<String, Integer> entry : gradeCount.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append(" students\n");
        }
        
        return report.toString();
    }
    
    // Helper method to convert marks to grade
    private String getGradeFromMarks(double marks) {
        List<String> grading = Helpers.readFromFile("grading.txt");
        for (String line : grading) {
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                try {
                    double min = Double.parseDouble(parts[1]);
                    double max = Double.parseDouble(parts[2]);
                    if (marks >= min && marks <= max) {
                        return parts[0];
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return "F";
    }

    // REPORT 2: PASS/FAIL RATE
    public String getPassFailReport(String moduleID) {
        int passCount = 0;
        int failCount = 0;
        
        List<String> results = Helpers.readFromFile("results.txt");
        for (String line : results) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 4 && parts[1].equals(moduleID)) {
                try {
                    double marks = Double.parseDouble(parts[3]);
                    if (marks >= 50) {
                        passCount++;
                    } else {
                        failCount++;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        int total = passCount + failCount;
        String passRate = total > 0 ? String.format("%.2f%%", passCount * 100.0 / total) : "0%";
        String failRate = total > 0 ? String.format("%.2f%%", failCount * 100.0 / total) : "0%";
        
        return "Pass: " + passCount + " (" + passRate + ")\n" +
               "Fail: " + failCount + " (" + failRate + ")";
    }

    // REPORT 3: LECTURER WORKLOAD
    public String getLecturerWorkloadReport() {
        Map<String, Integer> lecturerModules = new HashMap<>();
        
        // Count modules per lecturer
        for (String[] assignment : getMyAssignments()) {
            String lecID = assignment[1];
            lecturerModules.put(lecID, lecturerModules.getOrDefault(lecID, 0) + 1);
        }
        
        // Build report
        StringBuilder report = new StringBuilder();
        for (Map.Entry<String, Integer> entry : lecturerModules.entrySet()) {
            report.append(entry.getKey()).append(": ").append(entry.getValue()).append(" modules\n");
        }
        
        return report.toString();
    }

    // REPORT 4: STUDENT FEEDBACK
    public String getFeedbackReport() {
        // Get my lecturers
        Set<String> myLecturers = new HashSet<>();
        for (String[] assignment : getMyAssignments()) {
            myLecturers.add(assignment[1]);
        }
        
        // Collect comments
        Map<String, List<String>> lecturerComments = new HashMap<>();
        List<String> comments = Helpers.readFromFile("comments.txt");
        
        for (String line : comments) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 3 && myLecturers.contains(parts[1])) {
                String lecID = parts[1];
                String comment = parts[2];
                
                if (!lecturerComments.containsKey(lecID)) {
                    lecturerComments.put(lecID, new ArrayList<>());
                }
                lecturerComments.get(lecID).add(comment);
            }
        }
        
        // Build report
        StringBuilder report = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : lecturerComments.entrySet()) {
            report.append(entry.getKey()).append(":\n");
            for (String comment : entry.getValue()) {
                report.append("  - ").append(comment).append("\n");
            }
            report.append("\n");
        }
        
        return report.toString();
    }

    // REPORT 5: AVERAGE MARKS
    public String getAverageMarksReport() {
        // Get my modules
        Set<String> myModuleIDs = new HashSet<>();
        for (String[] module : getMyModules()) {
            myModuleIDs.add(module[0]);
        }
        
        // Calculate average per module
        Map<String, Double> moduleTotals = new HashMap<>();
        Map<String, Integer> moduleCounts = new HashMap<>();
        
        List<String> results = Helpers.readFromFile("results.txt");
        for (String line : results) {
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split("\\|");
            if (parts.length >= 4 && myModuleIDs.contains(parts[1])) {
                String modID = parts[1];
                try {
                    double marks = Double.parseDouble(parts[3]);
                    moduleTotals.put(modID, moduleTotals.getOrDefault(modID, 0.0) + marks);
                    moduleCounts.put(modID, moduleCounts.getOrDefault(modID, 0) + 1);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        
        // Build report
        StringBuilder report = new StringBuilder();
        for (String modID : myModuleIDs) {
            if (moduleCounts.containsKey(modID) && moduleCounts.get(modID) > 0) {
                double avg = moduleTotals.get(modID) / moduleCounts.get(modID);
                report.append(modID).append(": ").append(String.format("%.2f", avg)).append("\n");
            }
        }
        
        return report.toString();
    }

    // GETTERS
    public String getUserID() { return userID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAge() { return age; }
}