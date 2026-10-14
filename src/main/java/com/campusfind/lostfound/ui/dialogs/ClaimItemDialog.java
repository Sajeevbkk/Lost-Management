package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ClaimService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ClaimItemDialog extends JDialog {

    private final FoundItem foundItem;
    private final AuthService authService = AuthService.getInstance();
    private final ClaimService claimService = ClaimService.getInstance();

    private final JTextArea descProofArea = UIUtils.createTextArea(3, 20);
    private final JTextArea uniqueFeaturesArea = UIUtils.createTextArea(3, 20);
    private final JTextField dateLostField = UIUtils.createTextField(15);
    private final JTextField locationLostField = UIUtils.createTextField(20);
    private final JTextArea supportingProofArea = UIUtils.createTextArea(3, 20);

    public ClaimItemDialog(Frame parent, FoundItem foundItem) {
        super(parent, "Claim Ownership - " + (foundItem != null ? foundItem.getItemName() : "Item"), true);
        this.foundItem = foundItem;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(560, 680);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JLabel title = new JLabel("Confidential Ownership Claim");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel info = new JLabel("<html>To prevent false claims, provide <b>private unlisted details</b> known only to the true owner (e.g. serial numbers, inner pocket contents, wallpaper description, passcodes).</html>");
        info.setFont(AppTheme.FONT_REGULAR);
        info.setForeground(AppTheme.TEXT_MUTED);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(info);
        mainPanel.add(Box.createVerticalStrut(14));

        // Found Item Summary Card
        JPanel itemCard = UIUtils.createCard();
        itemCard.setLayout(new GridLayout(3, 2, 8, 4));
        itemCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        itemCard.add(new JLabel("Claiming Item:"));
        JLabel nameLbl = new JLabel(foundItem.getItemName());
        nameLbl.setFont(AppTheme.FONT_BOLD);
        itemCard.add(nameLbl);

        itemCard.add(new JLabel("Category:"));
        itemCard.add(new JLabel(foundItem.getCategory().getDisplayName()));

        itemCard.add(new JLabel("Pickup Location:"));
        JLabel storageLbl = new JLabel(foundItem.getStorageLocation());
        storageLbl.setForeground(AppTheme.PRIMARY_NAVY);
        itemCard.add(storageLbl);

        mainPanel.add(itemCard);
        mainPanel.add(Box.createVerticalStrut(14));

        // Form Fields
        JPanel formCard = UIUtils.createCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        dateLostField.setText(foundItem.getDateFound() != null ? foundItem.getDateFound().toString() : LocalDate.now().toString());

        int row = 0;
        addFormRow(formCard, gbc, row++, "Approx. Date Lost (YYYY-MM-DD):", dateLostField);
        addFormRow(formCard, gbc, row++, "Approx. Location Lost:", locationLostField);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel p1 = new JLabel("Description of Item & Marks:");
        p1.setFont(AppTheme.FONT_BOLD);
        formCard.add(p1, gbc);

        gbc.gridy = row++;
        formCard.add(new JScrollPane(descProofArea), gbc);

        gbc.gridy = row++;
        JLabel p2 = new JLabel("Secret / Unlisted Identifying Features (Crucial for Admin Approval):");
        p2.setFont(AppTheme.FONT_BOLD);
        p2.setForeground(AppTheme.SECONDARY_PURPLE);
        formCard.add(p2, gbc);

        gbc.gridy = row++;
        formCard.add(new JScrollPane(uniqueFeaturesArea), gbc);

        gbc.gridy = row++;
        JLabel p3 = new JLabel("Supporting Proof Details (Purchase receipt, DigiLocker, etc.):");
        p3.setFont(AppTheme.FONT_BOLD);
        formCard.add(p3, gbc);

        gbc.gridy = row++;
        formCard.add(new JScrollPane(supportingProofArea), gbc);

        mainPanel.add(new JScrollPane(formCard));
        mainPanel.add(Box.createVerticalStrut(14));

        // Submit Button
        JButton submitBtn = UIUtils.createButton("Submit Confidential Claim", AppTheme.PRIMARY_NAVY, Color.WHITE);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        submitBtn.addActionListener(e -> submitClaim());
        mainPanel.add(submitBtn);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.FONT_BOLD);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(field, gbc);
    }

    private void submitClaim() {
        User user = authService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please log in before claiming an item.", "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String desc = descProofArea.getText().trim();
        String features = uniqueFeaturesArea.getText().trim();
        String dateStr = dateLostField.getText().trim();
        String loc = locationLostField.getText().trim();
        String supporting = supportingProofArea.getText().trim();

        if (desc.isEmpty() || features.isEmpty() || loc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide description proof, location lost, and unique identifying features.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Claim claim = claimService.submitClaim(foundItem, user, desc, features, date, loc, supporting);

        dispose();

        JOptionPane.showMessageDialog(getParent(),
                "Your ownership claim has been submitted to Campus Security!\n\n" +
                "Status: PENDING VERIFICATION\n" +
                "Our administrative staff will examine your confidential proof against physical records.\n" +
                "You can monitor progress in 'My Reports & Claims'.",
                "Claim Submitted Successfully",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
