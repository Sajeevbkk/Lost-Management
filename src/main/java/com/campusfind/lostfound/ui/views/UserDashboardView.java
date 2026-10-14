package com.campusfind.lostfound.ui.views;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ClaimService;
import com.campusfind.lostfound.service.ItemService;
import com.campusfind.lostfound.ui.MainFrame;
import com.campusfind.lostfound.ui.dialogs.ItemDetailDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserDashboardView extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService = AuthService.getInstance();
    private final ItemService itemService = ItemService.getInstance();
    private final ClaimService claimService = ClaimService.getInstance();

    private final JLabel lblMyLost = new JLabel("0");
    private final JLabel lblMyFound = new JLabel("0");
    private final JLabel lblMyClaims = new JLabel("0");

    private final DefaultTableModel lostModel = new DefaultTableModel(
            new String[]{"ID", "Item Name", "Category", "Date Lost", "Location", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable lostTable = new JTable(lostModel);
    private List<LostItem> myLostList;

    private final DefaultTableModel foundModel = new DefaultTableModel(
            new String[]{"ID", "Item Name", "Category", "Date Found", "Location", "Storage Pickup Point", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable foundTable = new JTable(foundModel);
    private List<FoundItem> myFoundList;

    private final JPanel claimsContainer = new JPanel();

    public UserDashboardView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_MAIN);
        initComponents();

        itemService.addChangeListener(this::refreshData);
        claimService.addChangeListener(this::refreshData);
        authService.addAuthListener(this::refreshData);
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(AppTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Header Title
        JLabel title = new JLabel("My Campus Recovery Activity");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        scrollContent.add(title);
        scrollContent.add(Box.createVerticalStrut(14));

        // Badges Row
        JPanel badgesRow = new JPanel(new GridLayout(1, 3, 14, 0));
        badgesRow.setBackground(AppTheme.BG_MAIN);
        badgesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        badgesRow.add(createSummaryBadge("My Lost Belongings", lblMyLost, AppTheme.PRIMARY_NAVY));
        badgesRow.add(createSummaryBadge("My Discovered Belongings", lblMyFound, AppTheme.SECONDARY_PURPLE));
        badgesRow.add(createSummaryBadge("My Ownership Claims", lblMyClaims, AppTheme.SUCCESS));

        scrollContent.add(badgesRow);
        scrollContent.add(Box.createVerticalStrut(18));

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BOLD);
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);

        tabs.addTab("My Lost Items", createMyLostPanel());
        tabs.addTab("My Found Items", createMyFoundPanel());
        tabs.addTab("My Ownership Claims & Progress Stepper", createMyClaimsPanel());

        scrollContent.add(tabs);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createSummaryBadge(String label, JLabel countLabel, Color color) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(6, 4));

        JLabel titleLbl = new JLabel(label);
        titleLbl.setFont(AppTheme.FONT_SMALL);
        titleLbl.setForeground(AppTheme.TEXT_MUTED);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        countLabel.setForeground(color);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMyLostPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        UIUtils.styleTable(lostTable);
        p.add(new JScrollPane(lostTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton resolveBtn = UIUtils.createButton("Mark Selected as Resolved", AppTheme.SUCCESS, Color.WHITE);
        resolveBtn.addActionListener(e -> {
            int row = lostTable.getSelectedRow();
            if (row >= 0 && myLostList != null && row < myLostList.size()) {
                LostItem item = myLostList.get(row);
                itemService.updateLostStatus(item.getLostId(), LostStatus.RESOLVED);
                JOptionPane.showMessageDialog(this, "Item marked as resolved!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton viewBtn = UIUtils.createOutlineButton("View Details", AppTheme.PRIMARY_NAVY);
        viewBtn.addActionListener(e -> {
            int row = lostTable.getSelectedRow();
            if (row >= 0 && myLostList != null && row < myLostList.size()) {
                new ItemDetailDialog(mainFrame, myLostList.get(row)).setVisible(true);
            }
        });

        btnRow.add(resolveBtn);
        btnRow.add(viewBtn);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createMyFoundPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(AppTheme.BG_CARD);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        UIUtils.styleTable(foundTable);
        p.add(new JScrollPane(foundTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(AppTheme.BG_CARD);

        JButton viewBtn = UIUtils.createOutlineButton("View Details", AppTheme.PRIMARY_NAVY);
        viewBtn.addActionListener(e -> {
            int row = foundTable.getSelectedRow();
            if (row >= 0 && myFoundList != null && row < myFoundList.size()) {
                new ItemDetailDialog(mainFrame, myFoundList.get(row)).setVisible(true);
            }
        });

        btnRow.add(viewBtn);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createMyClaimsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.BG_MAIN);

        claimsContainer.setLayout(new BoxLayout(claimsContainer, BoxLayout.Y_AXIS));
        claimsContainer.setBackground(AppTheme.BG_MAIN);
        claimsContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane sp = new JScrollPane(claimsContainer);
        sp.setBorder(null);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    public void refreshData() {
        User user = authService.getCurrentUser();
        if (user == null) {
            lblMyLost.setText("0");
            lblMyFound.setText("0");
            lblMyClaims.setText("0");
            lostModel.setRowCount(0);
            foundModel.setRowCount(0);
            claimsContainer.removeAll();
            claimsContainer.revalidate();
            claimsContainer.repaint();
            return;
        }

        myLostList = itemService.getLostItemsByUser(user.getUserId());
        myFoundList = itemService.getFoundItemsByUser(user.getUserId());
        List<Claim> myClaims = claimService.getClaimsByClaimant(user.getUserId());

        lblMyLost.setText(String.valueOf(myLostList.size()));
        lblMyFound.setText(String.valueOf(myFoundList.size()));
        lblMyClaims.setText(String.valueOf(myClaims.size()));

        // Populate Lost Table
        lostModel.setRowCount(0);
        for (LostItem item : myLostList) {
            lostModel.addRow(new Object[]{
                    "#" + item.getLostId(),
                    item.getItemName(),
                    item.getCategory().getDisplayName(),
                    UIUtils.formatDate(item.getDateLost()),
                    item.getLocationLost(),
                    item.getStatus().name()
            });
        }

        // Populate Found Table
        foundModel.setRowCount(0);
        for (FoundItem item : myFoundList) {
            foundModel.addRow(new Object[]{
                    "#" + item.getFoundId(),
                    item.getItemName(),
                    item.getCategory().getDisplayName(),
                    UIUtils.formatDate(item.getDateFound()),
                    item.getLocationFound(),
                    item.getStorageLocation(),
                    item.getStatus().name()
            });
        }

        // Populate Claims with visual stepper
        claimsContainer.removeAll();
        if (myClaims.isEmpty()) {
            JPanel empty = UIUtils.createCard();
            empty.add(new JLabel("You have not submitted any ownership claims yet."));
            claimsContainer.add(empty);
        } else {
            for (Claim claim : myClaims) {
                claimsContainer.add(createClaimStepperCard(claim));
                claimsContainer.add(Box.createVerticalStrut(12));
            }
        }

        claimsContainer.revalidate();
        claimsContainer.repaint();
    }

    private JPanel createClaimStepperCard(Claim claim) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(10, 10));

        // Top info
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppTheme.BG_CARD);

        JLabel itemLbl = new JLabel("Claim #" + claim.getClaimId() + ": " + claim.getFoundItem().getItemName());
        itemLbl.setFont(AppTheme.FONT_BOLD);
        itemLbl.setForeground(AppTheme.PRIMARY_NAVY);

        JLabel statusBadge = UIUtils.createBadge(claim.getStatus().name(), AppTheme.PURPLE_LIGHT, AppTheme.SECONDARY_PURPLE);

        top.add(itemLbl, BorderLayout.WEST);
        top.add(statusBadge, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Stepper Visual Row
        JPanel stepperPanel = new JPanel(new GridLayout(1, 4, 8, 0));
        stepperPanel.setBackground(AppTheme.BG_CARD);

        int step = getStepIndex(claim.getStatus());
        stepperPanel.add(createStepBox(1, "Submitted", step >= 1));
        stepperPanel.add(createStepBox(2, "Under Verification", step >= 2));
        stepperPanel.add(createStepBox(3, "Approved for Pickup", step >= 3));
        stepperPanel.add(createStepBox(4, "Handover Complete", step >= 4));

        card.add(stepperPanel, BorderLayout.CENTER);

        // Bottom Remarks & Safe Storage
        JPanel bottom = new JPanel(new GridLayout(2, 1, 4, 4));
        bottom.setBackground(AppTheme.BG_CARD);

        JLabel deskLbl = new JLabel("Collection Desk: " + claim.getFoundItem().getStorageLocation());
        deskLbl.setFont(AppTheme.FONT_SMALL);
        deskLbl.setForeground(AppTheme.TEXT_MUTED);

        JLabel remarksLbl = new JLabel("Admin Remarks: " + (claim.getAdminRemarks() != null ? claim.getAdminRemarks() : "Under evaluation"));
        remarksLbl.setFont(AppTheme.FONT_SMALL);
        remarksLbl.setForeground(AppTheme.PRIMARY_NAVY);

        bottom.add(deskLbl);
        bottom.add(remarksLbl);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private int getStepIndex(ClaimStatus status) {
        if (status == null) return 1;
        return switch (status) {
            case PENDING -> 1;
            case UNDER_VERIFICATION -> 2;
            case APPROVED -> 3;
            case RETURNED -> 4;
            case REJECTED -> 1;
        };
    }

    private JPanel createStepBox(int num, String label, boolean completed) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(completed ? new Color(0xEC, 0xFD, 0xF5) : new Color(0xF8, 0xFA, 0xFC));
        box.setBorder(new CompoundBorder(
                new LineBorder(completed ? AppTheme.SUCCESS : AppTheme.BORDER, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel numLbl = new JLabel("Step " + num + (completed ? " [✓]" : ""));
        numLbl.setFont(AppTheme.FONT_BADGE);
        numLbl.setForeground(completed ? AppTheme.SUCCESS : AppTheme.TEXT_MUTED);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(AppTheme.FONT_SMALL);
        nameLbl.setForeground(completed ? AppTheme.TEXT_DARK : AppTheme.TEXT_MUTED);

        box.add(numLbl);
        box.add(Box.createVerticalStrut(2));
        box.add(nameLbl);
        return box;
    }
}
