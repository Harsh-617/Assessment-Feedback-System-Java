import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AcademicLeaderGUI extends JFrame {
    private AcademicLeader logic;
    private String leaderName;

    public AcademicLeaderGUI(String id, String name) {
        this.leaderName = name;
        this.logic = new AcademicLeader(id);
        
        setTitle("AFS Academic Leader - " + name);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Show the main menu
        showMainMenu();
        setVisible(true);
    }

    // MAIN MENU / DASHBOARD
    private void showMainMenu() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        
        // Title
        JLabel title = new JLabel("Academic Leader Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);
        
        // Menu buttons
        JPanel menuPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 150, 50, 150));
        
        JButton btnProfile = new JButton("Update Profile");
        JButton btnModules = new JButton("Manage Modules");
        JButton btnAssign = new JButton("Assign Lecturers");
        JButton btnReports = new JButton("View Reports");
        JButton btnLogout = new JButton("Logout");
        
        // Add action listeners
        btnProfile.addActionListener(e -> showUpdateProfile());
        btnModules.addActionListener(e -> showManageModules());
        btnAssign.addActionListener(e -> showAssignLecturer());
        btnReports.addActionListener(e -> showReportsMenu());
        btnLogout.addActionListener(e -> logout());
        
        menuPanel.add(new JLabel("Welcome, " + leaderName, SwingConstants.CENTER));
        menuPanel.add(btnProfile);
        menuPanel.add(btnModules);
        menuPanel.add(btnAssign);
        menuPanel.add(btnReports);
        menuPanel.add(btnLogout);
        
        add(menuPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // UPDATE PROFILE
    private void showUpdateProfile() {
        JTextField txtName = new JTextField(logic.getName());
        JTextField txtEmail = new JTextField(logic.getEmail());
        JTextField txtPhone = new JTextField(logic.getPhone());
        JTextField txtAge = new JTextField(logic.getAge());
        
        Object[] message = {
            "Name:", txtName,
            "Email:", txtEmail,
            "Phone:", txtPhone,
            "Age:", txtAge
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Update Profile", 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            if (logic.updateProfile(txtName.getText(), txtEmail.getText(), 
                                   txtPhone.getText(), txtAge.getText())) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error: Invalid email!", 
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // MANAGE MODULES
    private void showManageModules() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        
        // Title
        JLabel title = new JLabel("Manage Modules (Max 3)", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);
        
        // Table to display modules
        String[] columns = {"Module ID", "Module Name"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        // Load data
        List<String[]> modules = logic.getMyModules();
        for (String[] module : modules) {
            model.addRow(new Object[]{module[0], module[1]});
        }
        
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Add Module");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnBack = new JButton("Back");
        
        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter Module Name:");
            if (name != null && !name.trim().isEmpty()) {
                String moduleID = logic.createModule(name);
                if (moduleID != null) {
                    JOptionPane.showMessageDialog(this, "Module created: " + moduleID);
                    showManageModules(); // Refresh
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot create! Maximum 3 modules reached.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a module!");
                return;
            }
            
            String moduleID = (String) model.getValueAt(row, 0);
            String currentName = (String) model.getValueAt(row, 1);
            String newName = JOptionPane.showInputDialog(this, "Enter new name:", currentName);
            
            if (newName != null && !newName.trim().isEmpty()) {
                if (logic.updateModule(moduleID, newName)) {
                    JOptionPane.showMessageDialog(this, "Module updated!");
                    showManageModules(); // Refresh
                }
            }
        });
        
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a module!");
                return;
            }
            
            String moduleID = (String) model.getValueAt(row, 0);
            String moduleName = (String) model.getValueAt(row, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Delete module: " + moduleName + "?", 
                "Confirm", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (logic.deleteModule(moduleID)) {
                    JOptionPane.showMessageDialog(this, "Module deleted!");
                    showManageModules(); // Refresh
                }
            }
        });
        
        btnBack.addActionListener(e -> showMainMenu());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnBack);
        add(buttonPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }

    // ASSIGN LECTURER
    private void showAssignLecturer() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        
        // Title
        JLabel title = new JLabel("Assign Lecturers to Modules", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Leader ID", "Lecturer ID", "Module ID"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        // Load data
        List<String[]> assignments = logic.getMyAssignments();
        for (String[] assignment : assignments) {
            model.addRow(assignment);
        }
        
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAssign = new JButton("Assign Lecturer");
        JButton btnBack = new JButton("Back");
        
        btnAssign.addActionListener(e -> {
            // Get modules
            List<String[]> modules = logic.getMyModules();
            if (modules.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No modules available!");
                return;
            }
            
            // Create module selection
            String[] moduleNames = new String[modules.size()];
            for (int i = 0; i < modules.size(); i++) {
                moduleNames[i] = modules.get(i)[0] + " - " + modules.get(i)[1];
            }
            
            String selectedModule = (String) JOptionPane.showInputDialog(this,
                "Select Module:", "Assign Lecturer",
                JOptionPane.QUESTION_MESSAGE, null, moduleNames, moduleNames[0]);
            
            if (selectedModule != null) {
                String moduleID = selectedModule.split(" - ")[0];
                String lecturerID = JOptionPane.showInputDialog(this, "Enter Lecturer ID:");
                
                if (lecturerID != null && !lecturerID.trim().isEmpty()) {
                    if (logic.assignLecturer(lecturerID, moduleID)) {
                        JOptionPane.showMessageDialog(this, "Lecturer assigned!");
                        showAssignLecturer(); // Refresh
                    } else {
                        JOptionPane.showMessageDialog(this, "Assignment failed!");
                    }
                }
            }
        });
        
        btnBack.addActionListener(e -> showMainMenu());
        
        buttonPanel.add(btnAssign);
        buttonPanel.add(btnBack);
        add(buttonPanel, BorderLayout.SOUTH);
        
        revalidate();
        repaint();
    }

    // REPORTS MENU
    private void showReportsMenu() {
        String[] options = {
            "1. Grade Distribution",
            "2. Pass/Fail Rate",
            "3. Lecturer Workload",
            "4. Student Feedback",
            "5. Average Marks"
        };
        
        String choice = (String) JOptionPane.showInputDialog(this,
            "Select Report:", "Analyzed Reports",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        if (choice != null) {
            if (choice.startsWith("1")) {
                showReport("Grade Distribution", logic.getGradeDistributionReport());
            } else if (choice.startsWith("2")) {
                showPassFailReport();
            } else if (choice.startsWith("3")) {
                showReport("Lecturer Workload", logic.getLecturerWorkloadReport());
            } else if (choice.startsWith("4")) {
                showReport("Student Feedback", logic.getFeedbackReport());
            } else if (choice.startsWith("5")) {
                showReport("Average Marks", logic.getAverageMarksReport());
            }
        }
    }
    
    // Show a simple text report
    private void showReport(String title, String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, title, 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Special handler for Pass/Fail report (needs module selection)
    private void showPassFailReport() {
        List<String[]> modules = logic.getMyModules();
        if (modules.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No modules available!");
            return;
        }
        
        String[] moduleNames = new String[modules.size()];
        for (int i = 0; i < modules.size(); i++) {
            moduleNames[i] = modules.get(i)[0] + " - " + modules.get(i)[1];
        }
        
        String selected = (String) JOptionPane.showInputDialog(this,
            "Select Module:", "Pass/Fail Rate",
            JOptionPane.QUESTION_MESSAGE, null, moduleNames, moduleNames[0]);
        
        if (selected != null) {
            String moduleID = selected.split(" - ")[0];
            String report = logic.getPassFailReport(moduleID);
            showReport("Pass/Fail Rate - " + selected, report);
        }
    }

    // LOGOUT
    private void logout() {
        new LoginGUI();
        this.dispose();
    }

    // MAIN (For Testing)
    public static void main(String[] args) {
        new AcademicLeaderGUI("AL01", "Test Leader");
    }
}