package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ItemService;
import com.campusfind.lostfound.service.MatchingService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ReportItemDialog extends JDialog {

    private final AuthService authService = AuthService.getInstance();
    private final ItemService itemService = ItemService.getInstance();
    private final MatchingService matchingService = MatchingService.getInstance();

    private final JTabbedPane tabbedPane = new JTabbedPane();

    // Lost Item Form
    private final JTextField lostNameField = UIUtils.createTextField(25);
    private final JComboBox<Category> lostCategoryCombo = new JComboBox<>(Category.values());
    private final JTextField lostDateField = UIUtils.createTextField(15);
    private final JTextField lostLocationField = UIUtils.createTextField(25);
    private final JTextField lostColorField = UIUtils.createTextField(25);
    private final JTextField lostPhoneField = UIUtils.createTextField(20);
    private final JTextArea lostDescArea = UIUtils.createTextArea(4, 25);

    // Found Item Form
    private final JTextField foundNameField = UIUtils.createTextField(25);
    private final JComboBox<Category> foundCategoryCombo = new JComboBox<>(Category.values());
    private final JTextField foundDateField = UIUtils.createTextField(15);
    private final JTextField foundLocationField = UIUtils.createTextField(25);
    private final JTextField foundStorageField = UIUtils.createTextField(25);
    private final JTextField foundColorField = UIUtils.createTextField(25);
    private final JTextArea foundDescArea = UIUtils.createTextArea(4, 25);

    public ReportItemDialog(Frame parent, boolean startOnFoundTab) {
        super(parent, "Report Lost or Found Belonging", true);
        initComponents();
        if (startOnFoundTab) {
            tabbedPane.setSelectedIndex(1);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(560, 680);
        setLocationRelativeTo(getParent());

        // Prepopulate current user's phone if logged in
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            lostPhoneField.setText(currentUser.getPhoneNumber());
        }
        lostDateField.setText(LocalDate.now().toString());
        foundDateField.setText(LocalDate.now().toString());
        foundStorageField.setText("Campus Security Desk - Admin Block Room 102");

        tabbedPane.setFont(AppTheme.FONT_BOLD);
        tabbedPane.addTab("Report Lost Item", createLostTab());
        tabbedPane.addTab("Report Found Item", createFoundTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createLostTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AppTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;

        int row = 0;
        addFormRow(formCard, gbc, row++, "Item Name / Title *:", lostNameField);
        addFormRow(formCard, gbc, row++, "Category *:", lostCategoryCombo);
        addFormRow(formCard, gbc, row++, "Date Lost (YYYY-MM-DD) *:", lostDateField);
        addFormRow(formCard, gbc, row++, "Campus Location Lost *:", lostLocationField);
        addFormRow(formCard, gbc, row++, "Color & Distinctive Features *:", lostColorField);
        addFormRow(formCard, gbc, row++, "Contact Phone *:", lostPhoneField);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel descLabel = new JLabel("Detailed Description *:");
        descLabel.setFont(AppTheme.FONT_BOLD);
        formCard.add(descLabel, gbc);

        gbc.gridy = row++;
        formCard.add(new JScrollPane(lostDescArea), gbc);

        panel.add(new JScrollPane(formCard), BorderLayout.CENTER);

        JButton submitLostBtn = UIUtils.createButton("Submit Lost Item Report", AppTheme.PRIMARY_NAVY, Color.WHITE);
        submitLostBtn.setPreferredSize(new Dimension(200, 42));
        submitLostBtn.addActionListener(e -> submitLostItem());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(AppTheme.BG_MAIN);
        btnPanel.add(submitLostBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFoundTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AppTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;

        int row = 0;
        addFormRow(formCard, gbc, row++, "Discovered Item Name *:", foundNameField);
        addFormRow(formCard, gbc, row++, "Category *:", foundCategoryCombo);
        addFormRow(formCard, gbc, row++, "Date Found (YYYY-MM-DD) *:", foundDateField);
        addFormRow(formCard, gbc, row++, "Campus Location Found *:", foundLocationField);
        addFormRow(formCard, gbc, row++, "Safe Pickup Point / Storage *:", foundStorageField);
        addFormRow(formCard, gbc, row++, "Color & Features *:", foundColorField);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel descLabel = new JLabel("Description / Condition *:");
        descLabel.setFont(AppTheme.FONT_BOLD);
        formCard.add(descLabel, gbc);

        gbc.gridy = row++;
        formCard.add(new JScrollPane(foundDescArea), gbc);

        panel.add(new JScrollPane(formCard), BorderLayout.CENTER);

        JButton submitFoundBtn = UIUtils.createButton("Submit Discovered Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        submitFoundBtn.setPreferredSize(new Dimension(200, 42));
        submitFoundBtn.addActionListener(e -> submitFoundItem());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(AppTheme.BG_MAIN);
        btnPanel.add(submitFoundBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_BOLD);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void submitLostItem() {
        User user = authService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please sign in to report a lost item.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = lostNameField.getText().trim();
        Category cat = (Category) lostCategoryCombo.getSelectedItem();
        String dateStr = lostDateField.getText().trim();
        String loc = lostLocationField.getText().trim();
        String color = lostColorField.getText().trim();
        String phone = lostPhoneField.getText().trim();
        String desc = lostDescArea.getText().trim();

        if (name.isEmpty() || loc.isEmpty() || color.isEmpty() || phone.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please provide date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LostItem lost = itemService.reportLostItem(user, name, cat, desc, date, loc, color, phone, null);

        // Check for matches immediately
        List<MatchResult> matches = matchingService.scanMatchesForLostItem(lost);

        dispose();

        if (!matches.isEmpty()) {
            MatchResult topMatch = matches.get(0);
            int opt = JOptionPane.showConfirmDialog(getParent(),
                    "Your lost item was successfully reported!\n\n" +
                    "AI Smart Matching Engine detected a " + topMatch.getMatchScore() + "% match:\n" +
                    "Item: " + topMatch.getFoundItem().getItemName() + "\n" +
                    "Location: " + topMatch.getFoundItem().getLocationFound() + "\n\n" +
                    "Would you like to review this match and submit a claim now?",
                    "Potential Match Alert (" + topMatch.getMatchScore() + "% Confidence)",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (opt == JOptionPane.YES_OPTION) {
                new ClaimItemDialog((Frame) getParent(), topMatch.getFoundItem()).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(getParent(),
                    "Lost item reported successfully! We will automatically alert you when a matching item is cataloged.",
                    "Report Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void submitFoundItem() {
        User user = authService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please sign in to report a found item.", "Authentication Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = foundNameField.getText().trim();
        Category cat = (Category) foundCategoryCombo.getSelectedItem();
        String dateStr = foundDateField.getText().trim();
        String loc = foundLocationField.getText().trim();
        String storage = foundStorageField.getText().trim();
        String color = foundColorField.getText().trim();
        String desc = foundDescArea.getText().trim();

        if (name.isEmpty() || loc.isEmpty() || storage.isEmpty() || color.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please provide date in YYYY-MM-DD format.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FoundItem found = itemService.reportFoundItem(user, name, cat, desc, date, loc, color, storage, null);

        dispose();

        JOptionPane.showMessageDialog(getParent(),
                "Discovered item cataloged successfully!\nSafe Storage: " + storage + "\nOwners of matching lost reports have been notified.",
                "Found Item Cataloged",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
