import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * LecturerGUI.java - Lecturer Dashboard
 * Covers all spec requirements:
 *  - Edit personal profile
 *  - Design assessment types
 *  - Key-in assessment marks
 *  - Provide feedback
 */
public class LecturerGUI extends JFrame {
    private Lecturer logic;
    private String lecturerID;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public LecturerGUI(String id, String name) {
        this.lecturerID = id;
        this.logic = new Lecturer(id);

        setTitle("AFS Lecturer System - " + name);
        setSize(850, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);

        mainPanel.add(createDashboard(name),  "Dashboard");
        mainPanel.add(createProfilePanel(),   "Profile");
        mainPanel.add(createDesignPanel(),    "Design");
        mainPanel.add(createMarksPanel(),     "Marks");
        mainPanel.add(createCommentsPanel(),  "Comments");

        add(mainPanel);
    }

    // ==========================================
    // DASHBOARD
    // ==========================================
    private JPanel createDashboard(String name) {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JButton btnProfile  = new JButton("Update Profile");
        JButton btnDesign   = new JButton("Design Assessment");
        JButton btnMarks    = new JButton("Input Marks & Feedback");
        JButton btnComments = new JButton("View Student Comments");
        JButton btnLogout   = new JButton("Logout");

        btnProfile.addActionListener(e -> {
            refreshProfilePanel();
            cardLayout.show(mainPanel, "Profile");
        });
        btnDesign.addActionListener(e -> cardLayout.show(mainPanel, "Design"));
        btnMarks.addActionListener(e -> cardLayout.show(mainPanel, "Marks"));
        btnComments.addActionListener(e -> {
            refreshCommentsTable();
            cardLayout.show(mainPanel, "Comments");
        });
        btnLogout.addActionListener(e -> logout());

        panel.add(new JLabel("Welcome, " + name, SwingConstants.CENTER));
        panel.add(btnProfile);
        panel.add(btnDesign);
        panel.add(btnMarks);
        panel.add(btnComments);
        panel.add(btnLogout);
        return panel;
    }

    // ==========================================
    // UPDATE PROFILE PANEL
    // ==========================================
    private JTextField pfName, pfEmail, pfPhone, pfAge;

    private JPanel createProfilePanel() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        pfName  = new JTextField();
        pfEmail = new JTextField();
        pfPhone = new JTextField();
        pfAge   = new JTextField();

        p.add(new JLabel("Name:"));  p.add(pfName);
        p.add(new JLabel("Email:")); p.add(pfEmail);
        p.add(new JLabel("Phone:")); p.add(pfPhone);
        p.add(new JLabel("Age:"));   p.add(pfAge);

        JButton btnSave = new JButton("Save");
        JButton btnBack = new JButton("Back");

        btnSave.addActionListener(e -> {
            if (pfName.getText().trim().isEmpty() || pfEmail.getText().trim().isEmpty()
                    || pfPhone.getText().trim().isEmpty() || pfAge.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (logic.updateProfile(pfName.getText().trim(), pfEmail.getText().trim(),
                                    pfPhone.getText().trim(), pfAge.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Update failed. Check email format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        p.add(btnBack); p.add(btnSave);
        return p;
    }

    private void refreshProfilePanel() {
        pfName.setText(logic.getName());
        pfEmail.setText(logic.getEmail());
        pfPhone.setText(logic.getPhone());
        pfAge.setText(logic.getAge());
    }

    // ==========================================
    // DESIGN ASSESSMENT PANEL
    // ==========================================
    private JPanel createDesignPanel() {
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));

        JTextField tName       = new JTextField();
        JTextField tWeightage  = new JTextField();
        // Module selector — populated dynamically
        JComboBox<String> cmbModule = new JComboBox<>();

        // Populate module combo on panel creation (will be refreshed on show)
        refreshModuleCombo(cmbModule);

        p.add(new JLabel("Module:"));    p.add(cmbModule);
        p.add(new JLabel("Assessment Name:")); p.add(tName);
        p.add(new JLabel("Weightage (%):")); p.add(tWeightage);

        JButton btnSave = new JButton("Save Assessment");
        JButton btnBack = new JButton("Back");

        btnSave.addActionListener(e -> {
            if (cmbModule.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No modules assigned to you yet.");
                return;
            }
            if (tName.getText().trim().isEmpty() || tWeightage.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedModule = ((String) cmbModule.getSelectedItem()).split(" - ")[0];
            String asID = logic.designAssessment(selectedModule, tName.getText().trim(), tWeightage.getText().trim());
            if (asID != null) {
                tName.setText(""); tWeightage.setText("");
                JOptionPane.showMessageDialog(this, "Assessment saved! ID: " + asID);
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed. Ensure weightage is a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> {
            refreshModuleCombo(cmbModule);
            cardLayout.show(mainPanel, "Dashboard");
        });

        p.add(btnBack); p.add(btnSave);
        return p;
    }

    private void refreshModuleCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for (String[] m : logic.getMyAssignedModuleDetails()) {
            combo.addItem(m[0] + " - " + (m.length > 1 ? m[1] : ""));
        }
    }

    // ==========================================
    // INPUT MARKS & FEEDBACK PANEL
    // ==========================================
    private JPanel createMarksPanel() {
        JPanel p = new JPanel(new GridLayout(8, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        JTextField  tStuID    = new JTextField();
        JComboBox<String> cmbModule = new JComboBox<>();
        JComboBox<String> cmbAssess = new JComboBox<>();
        JTextField  tMarks    = new JTextField();
        JTextArea   tFeedback = new JTextArea(3, 20);

        refreshModuleCombo(cmbModule);

        // When module changes, reload assessments
        cmbModule.addActionListener(e -> {
            cmbAssess.removeAllItems();
            if (cmbModule.getItemCount() > 0 && cmbModule.getSelectedItem() != null) {
                String modID = ((String) cmbModule.getSelectedItem()).split(" - ")[0];
                for (String[] a : logic.getAssessmentsForModule(modID)) {
                    cmbAssess.addItem(a[0] + " - " + a[2]); // asID - name
                }
            }
        });

        p.add(new JLabel("Student ID:"));    p.add(tStuID);
        p.add(new JLabel("Module:"));        p.add(cmbModule);
        p.add(new JLabel("Assessment:"));    p.add(cmbAssess);
        p.add(new JLabel("Marks (0-100):")); p.add(tMarks);
        p.add(new JLabel("Feedback:"));      p.add(new JScrollPane(tFeedback));

        JButton btnSubmit = new JButton("Submit");
        JButton btnBack   = new JButton("Back");

        btnSubmit.addActionListener(e -> {
            if (cmbModule.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No modules assigned to you."); return;
            }
            if (cmbAssess.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No assessments for this module yet. Design one first."); return;
            }
            if (tStuID.getText().trim().isEmpty() || tMarks.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Student ID and Marks are required.",
                    "Error", JOptionPane.WARNING_MESSAGE); return;
            }
            try {
                double m = Double.parseDouble(tMarks.getText().trim());
                if (m < 0 || m > 100) { JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100."); return; }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Marks must be a valid number.", "Error", JOptionPane.WARNING_MESSAGE); return;
            }

            String modID  = ((String) cmbModule.getSelectedItem()).split(" - ")[0];
            String asID   = ((String) cmbAssess.getSelectedItem()).split(" - ")[0];

            if (logic.inputMarks(tStuID.getText().trim(), modID, asID,
                                 tMarks.getText().trim(), tFeedback.getText().trim())) {
                tStuID.setText(""); tMarks.setText(""); tFeedback.setText("");
                JOptionPane.showMessageDialog(this, "Marks submitted!");
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Submission failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> {
            refreshModuleCombo(cmbModule);
            cardLayout.show(mainPanel, "Dashboard");
        });

        p.add(btnBack); p.add(btnSubmit);
        return p;
    }

    // ==========================================
    // VIEW COMMENTS PANEL
    // ==========================================
    private JTable commentsTable;

    private JPanel createCommentsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        commentsTable = new JTable(new DefaultTableModel(new String[]{"Student ID", "Comment"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        p.add(new JScrollPane(commentsTable), BorderLayout.CENTER);

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        JPanel south = new JPanel(new FlowLayout()); south.add(btnBack);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshCommentsTable() {
        DefaultTableModel m = (DefaultTableModel) commentsTable.getModel();
        m.setRowCount(0);
        for (String[] row : logic.getLecturerComments()) m.addRow(row);
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    public void logout() {
        try {
            new LoginGUI().setVisible(true);
            this.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Logout failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LecturerGUI("LEC01", "Professor").setVisible(true));
    }
}