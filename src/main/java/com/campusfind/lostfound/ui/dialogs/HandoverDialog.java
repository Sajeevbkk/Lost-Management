package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ItemReturnService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HandoverDialog extends JDialog {

    private final Claim claim;
    private final ItemReturnService returnService = ItemReturnService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final JTextArea notesArea = UIUtils.createTextArea(4, 25);
    private final JCheckBox verifyIdCheck = new JCheckBox("I have physically verified the student / staff member's official college ID card.");

    public HandoverDialog(Frame parent, Claim claim) {
        super(parent, "Physical Handover & Audit Log", true);
        this.claim = claim;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(520, 520);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Official Physical Item Handover");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel info = new JLabel("<html>Record the physical transfer of the property to the verified claimant. This creates an <b>immutable audit log entry</b>.</html>");
        info.setFont(AppTheme.FONT_REGULAR);
        info.setForeground(AppTheme.TEXT_MUTED);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(info);
        mainPanel.add(Box.createVerticalStrut(14));

        // Handover Details Card
        JPanel card = UIUtils.createCard();
        card.setLayout(new GridLayout(4, 2, 8, 6));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(new JLabel("Claim ID:"));
        card.add(new JLabel("#" + claim.getClaimId()));

        card.add(new JLabel("Item Being Returned:"));
        JLabel itemLbl = new JLabel(claim.getFoundItem().getItemName());
        itemLbl.setFont(AppTheme.FONT_BOLD);
        card.add(itemLbl);

        card.add(new JLabel("Claimant Member:"));
        JLabel claimantLbl = new JLabel(claim.getClaimant().getFullName() + " (" + claim.getClaimant().getCampusId() + ")");
        claimantLbl.setFont(AppTheme.FONT_BOLD);
        claimantLbl.setForeground(AppTheme.PRIMARY_NAVY);
        card.add(claimantLbl);

        card.add(new JLabel("Handover Admin:"));
        User admin = authService.getCurrentUser();
        card.add(new JLabel(admin != null ? admin.getFullName() : "Admin"));

        mainPanel.add(card);
        mainPanel.add(Box.createVerticalStrut(12));

        // Verification Checkbox
        verifyIdCheck.setFont(AppTheme.FONT_BOLD);
        verifyIdCheck.setForeground(AppTheme.PRIMARY_NAVY);
        verifyIdCheck.setBackground(AppTheme.BG_MAIN);
        verifyIdCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(verifyIdCheck);
        mainPanel.add(Box.createVerticalStrut(10));

        // Notes Area
        JPanel notesCard = UIUtils.createCard();
        notesCard.setLayout(new BorderLayout(4, 4));
        notesCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel notesLbl = new JLabel("Handover Verification Notes & Physical Serial Signoff:");
        notesLbl.setFont(AppTheme.FONT_BOLD);
        notesArea.setText("Verified student smart ID card " + claim.getClaimant().getCampusId() + ". Physical item inspected and accepted by owner.");

        notesCard.add(notesLbl, BorderLayout.NORTH);
        notesCard.add(new JScrollPane(notesArea), BorderLayout.CENTER);

        mainPanel.add(notesCard);
        mainPanel.add(Box.createVerticalStrut(16));

        // Confirm Button
        JButton confirmBtn = UIUtils.createButton("Confirm & Log Physical Return", AppTheme.SUCCESS, Color.WHITE);
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        confirmBtn.addActionListener(e -> completeHandover());
        mainPanel.add(confirmBtn);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void completeHandover() {
        if (!verifyIdCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please verify the member's official college ID card and check the verification box.", "ID Verification Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String notes = notesArea.getText().trim();
        if (notes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter handover audit notes.", "Notes Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User adminUser = authService.getCurrentUser();
        returnService.executeHandover(claim, notes, adminUser);

        dispose();

        JOptionPane.showMessageDialog(getParent(),
                "Physical handover has been logged successfully in the immutable audit trail!\nItem marked as RETURNED.",
                "Handover Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
