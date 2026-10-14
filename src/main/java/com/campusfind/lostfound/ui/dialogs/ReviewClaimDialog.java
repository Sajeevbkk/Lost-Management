package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.ClaimStatus;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ClaimService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReviewClaimDialog extends JDialog {

    private final Claim claim;
    private final ClaimService claimService = ClaimService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final JTextArea remarksArea = UIUtils.createTextArea(3, 20);

    public ReviewClaimDialog(Frame parent, Claim claim) {
        super(parent, "Administrative Review: Claim #" + (claim != null ? claim.getClaimId() : ""), true);
        this.claim = claim;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(580, 700);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Confidential Claim Verification");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(12));

        // Claimant and Found Item Info
        JPanel infoCard = UIUtils.createCard();
        infoCard.setLayout(new GridLayout(4, 2, 8, 4));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoCard.add(new JLabel("Claimant:"));
        JLabel claimantLbl = new JLabel(claim.getClaimant().getFullName() + " (" + claim.getClaimant().getCampusId() + ")");
        claimantLbl.setFont(AppTheme.FONT_BOLD);
        infoCard.add(claimantLbl);

        infoCard.add(new JLabel("Department & Phone:"));
        infoCard.add(new JLabel(claim.getClaimant().getDepartment() + " | " + claim.getClaimant().getPhoneNumber()));

        infoCard.add(new JLabel("Target Found Item:"));
        JLabel itemLbl = new JLabel(claim.getFoundItem().getItemName() + " (" + claim.getFoundItem().getCategory().getDisplayName() + ")");
        itemLbl.setFont(AppTheme.FONT_BOLD);
        infoCard.add(itemLbl);

        infoCard.add(new JLabel("Safe Storage Location:"));
        JLabel locLbl = new JLabel(claim.getFoundItem().getStorageLocation());
        locLbl.setForeground(AppTheme.PRIMARY_NAVY);
        infoCard.add(locLbl);

        mainPanel.add(infoCard);
        mainPanel.add(Box.createVerticalStrut(12));

        // Secret Proof Details Card
        JPanel proofCard = UIUtils.createCard();
        proofCard.setLayout(new BoxLayout(proofCard, BoxLayout.Y_AXIS));
        proofCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel proofTitle = new JLabel("Confidential Identifying Details:");
        proofTitle.setFont(AppTheme.FONT_BOLD);
        proofTitle.setForeground(AppTheme.SECONDARY_PURPLE);

        JTextArea secretText = new JTextArea(claim.getUniqueIdentifyingFeatures());
        secretText.setFont(AppTheme.FONT_REGULAR);
        secretText.setEditable(false);
        secretText.setLineWrap(true);
        secretText.setWrapStyleWord(true);
        secretText.setBackground(new Color(0xF8, 0xFA, 0xFC));
        secretText.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel descProofTitle = new JLabel("Claimant's Item Description:");
        descProofTitle.setFont(AppTheme.FONT_BOLD);

        JTextArea descProofText = new JTextArea(claim.getItemDescriptionProof());
        descProofText.setFont(AppTheme.FONT_REGULAR);
        descProofText.setEditable(false);
        descProofText.setLineWrap(true);
        descProofText.setWrapStyleWord(true);
        descProofText.setBackground(new Color(0xF8, 0xFA, 0xFC));
        descProofText.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel supportingTitle = new JLabel("Supporting Proof / Notes:");
        supportingTitle.setFont(AppTheme.FONT_BOLD);

        JTextArea supportingText = new JTextArea(claim.getSupportingProofDetails() != null ? claim.getSupportingProofDetails() : "None provided");
        supportingText.setFont(AppTheme.FONT_REGULAR);
        supportingText.setEditable(false);
        supportingText.setLineWrap(true);
        supportingText.setWrapStyleWord(true);
        supportingText.setBackground(new Color(0xF8, 0xFA, 0xFC));
        supportingText.setBorder(new EmptyBorder(8, 8, 8, 8));

        proofCard.add(proofTitle);
        proofCard.add(Box.createVerticalStrut(4));
        proofCard.add(new JScrollPane(secretText));
        proofCard.add(Box.createVerticalStrut(8));
        proofCard.add(descProofTitle);
        proofCard.add(Box.createVerticalStrut(4));
        proofCard.add(new JScrollPane(descProofText));
        proofCard.add(Box.createVerticalStrut(8));
        proofCard.add(supportingTitle);
        proofCard.add(Box.createVerticalStrut(4));
        proofCard.add(new JScrollPane(supportingText));

        mainPanel.add(new JScrollPane(proofCard));
        mainPanel.add(Box.createVerticalStrut(10));

        // Admin Remarks Field
        JPanel remarksPanel = UIUtils.createCard();
        remarksPanel.setLayout(new BorderLayout(4, 4));
        remarksPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel remarksLabel = new JLabel("Administrator Remarks & Audit Notes:");
        remarksLabel.setFont(AppTheme.FONT_BOLD);
        remarksArea.setText(claim.getAdminRemarks() != null ? claim.getAdminRemarks() : "");

        remarksPanel.add(remarksLabel, BorderLayout.NORTH);
        remarksPanel.add(new JScrollPane(remarksArea), BorderLayout.CENTER);

        mainPanel.add(remarksPanel);
        mainPanel.add(Box.createVerticalStrut(14));

        // Decision Action Buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        actionPanel.setBackground(AppTheme.BG_MAIN);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton approveBtn = UIUtils.createButton("Approve Claim", AppTheme.SUCCESS, Color.WHITE);
        approveBtn.addActionListener(e -> updateClaim(ClaimStatus.APPROVED));

        JButton verifyBtn = UIUtils.createButton("More Proof Needed", AppTheme.WARNING, Color.WHITE);
        verifyBtn.addActionListener(e -> updateClaim(ClaimStatus.UNDER_VERIFICATION));

        JButton rejectBtn = UIUtils.createButton("Reject Claim", AppTheme.DANGER, Color.WHITE);
        rejectBtn.addActionListener(e -> updateClaim(ClaimStatus.REJECTED));

        actionPanel.add(approveBtn);
        actionPanel.add(verifyBtn);
        actionPanel.add(rejectBtn);

        mainPanel.add(actionPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void updateClaim(ClaimStatus newStatus) {
        String remarks = remarksArea.getText().trim();
        if (remarks.isEmpty()) {
            if (newStatus == ClaimStatus.APPROVED) {
                remarks = "Confidential evidence verified. Approved for collection at pickup desk.";
            } else if (newStatus == ClaimStatus.UNDER_VERIFICATION) {
                remarks = "Additional identifying details or physical ID requested before handover.";
            } else if (newStatus == ClaimStatus.REJECTED) {
                remarks = "Submitted proof features did not match cataloged item attributes.";
            }
        }

        User adminUser = authService.getCurrentUser();
        claimService.reviewClaim(claim.getClaimId(), newStatus, remarks, adminUser);

        dispose();

        JOptionPane.showMessageDialog(getParent(),
                "Claim #" + claim.getClaimId() + " status updated to " + newStatus.name() + "!\nClaimant notified.",
                "Review Completed",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
