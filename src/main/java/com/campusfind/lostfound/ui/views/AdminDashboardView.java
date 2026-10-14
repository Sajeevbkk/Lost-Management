package com.campusfind.lostfound.ui.views;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.Claim;
import com.campusfind.lostfound.model.ItemReturn;
import com.campusfind.lostfound.model.User;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ClaimService;
import com.campusfind.lostfound.service.ItemReturnService;
import com.campusfind.lostfound.ui.MainFrame;
import com.campusfind.lostfound.ui.dialogs.HandoverDialog;
import com.campusfind.lostfound.ui.dialogs.ReviewClaimDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardView extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService = AuthService.getInstance();
    private final ClaimService claimService = ClaimService.getInstance();
    private final ItemReturnService returnService = ItemReturnService.getInstance();

    private final JPanel contentPanel = new JPanel(new BorderLayout());

    // Tab 1: Pending Claims
    private final DefaultTableModel pendingClaimsModel = new DefaultTableModel(
            new String[]{"Claim ID", "Item Name", "Claimant Name", "Campus ID", "Approx Date Lost", "Status", "Submitted At"}, 0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable pendingClaimsTable = new JTable(pendingClaimsModel);
    private List<Claim> pendingClaimsList;

    // Tab 2: Approved Claims
    private final DefaultTableModel approvedClaimsModel = new DefaultTableModel(
            new String[]{"Claim ID", "Item Name", "Claimant Name", "Campus ID", "Pickup Desk", "Admin Remarks"}, 0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable approvedClaimsTable = new JTable(approvedClaimsModel);
    private List<Claim> approvedClaimsList;

    // Tab 3: Handover Audit Trail
    private final DefaultTableModel auditModel = new DefaultTableModel(
            new String[]{"Return ID", "Claim ID", "Item Name", "Claimant Member", "Verified By Admin", "Handover Time", "Audit Notes"}, 0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable auditTable = new JTable(auditModel);

    // Tab 4: Campus Users Directory
    private final DefaultTableModel usersModel = new DefaultTableModel(
            new String[]{"User ID", "Full Name", "College Email", "Campus ID", "Department", "Phone", "Role"}, 0
    ) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable usersTable = new JTable(usersModel);

    public AdminDashboardView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_MAIN);

        add(contentPanel, BorderLayout.CENTER);

        authService.addAuthListener(this::refreshView);
        claimService.addChangeListener(this::refreshData);
        returnService.addChangeListener(this::refreshData);

        refreshView();
    }

    public void refreshView() {
        contentPanel.removeAll();

        if (!authService.isAdmin()) {
            contentPanel.add(createRestrictedPanel(), BorderLayout.CENTER);
        } else {
            contentPanel.add(createAdminPanel(), BorderLayout.CENTER);
            refreshData();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createRestrictedPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(AppTheme.BG_MAIN);
        p.setBorder(new EmptyBorder(60, 24, 24, 24));

        JPanel card = UIUtils.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(540, 260));

        JLabel icon = new JLabel("🔒 Campus Administrator Access Restricted");
        icon.setFont(AppTheme.FONT_HEADER);
        icon.setForeground(AppTheme.DANGER);

        JLabel msg = new JLabel("<html>This administrative console is reserved for authorized Campus Security & Student Affairs personnel to review confidential proof and conduct physical item releases.</html>");
        msg.setFont(AppTheme.FONT_REGULAR);
        msg.setForeground(AppTheme.TEXT_MUTED);

        JButton adminLoginBtn = UIUtils.createButton("1-Click Sign In as Campus Admin (Arvind Swaminathan)", AppTheme.PRIMARY_NAVY, Color.WHITE);
        adminLoginBtn.addActionListener(e -> authService.quickSwitchUser("admin@college.edu"));

        card.add(icon);
        card.add(Box.createVerticalStrut(10));
        card.add(msg);
        card.add(Box.createVerticalStrut(20));
        card.add(adminLoginBtn);

        p.add(card);
        return p;
    }

    private JPanel createAdminPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(AppTheme.BG_MAIN);
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        // Header Title
        JPanel headRow = new JPanel(new BorderLayout());
        headRow.setBackground(AppTheme.BG_MAIN);

        JLabel title = new JLabel("Campus Security Administration Console");
        title.setFont(AppTheme.FONT_HEADER);
        title.setForeground(AppTheme.PRIMARY_NAVY);

        JLabel sub = new JLabel("Confidential claim moderation, physical handover audit logging & member directory");
        sub.setFont(AppTheme.FONT_SMALL);
        sub.setForeground(AppTheme.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(AppTheme.BG_MAIN);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(sub);

        headRow.add(titleBlock, BorderLayout.WEST);
        p.add(headRow, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_BOLD);

        // Tab 1: Pending Claims Queue
        JPanel t1 = new JPanel(new BorderLayout(8, 8));
        t1.setBackground(AppTheme.BG_CARD);
        t1.setBorder(new EmptyBorder(12, 12, 12, 12));
        UIUtils.styleTable(pendingClaimsTable);
        t1.add(new JScrollPane(pendingClaimsTable), BorderLayout.CENTER);

        JPanel t1Btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        t1Btns.setBackground(AppTheme.BG_CARD);
        JButton reviewBtn = UIUtils.createButton("Inspect Secret Evidence & Review Claim", AppTheme.PRIMARY_NAVY, Color.WHITE);
        reviewBtn.addActionListener(e -> reviewSelectedClaim());
        t1Btns.add(reviewBtn);
        t1.add(t1Btns, BorderLayout.SOUTH);

        // Tab 2: Approved Claims (Handover Ready)
        JPanel t2 = new JPanel(new BorderLayout(8, 8));
        t2.setBackground(AppTheme.BG_CARD);
        t2.setBorder(new EmptyBorder(12, 12, 12, 12));
        UIUtils.styleTable(approvedClaimsTable);
        t2.add(new JScrollPane(approvedClaimsTable), BorderLayout.CENTER);

        JPanel t2Btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        t2Btns.setBackground(AppTheme.BG_CARD);
        JButton handoverBtn = UIUtils.createButton("Execute Official Physical Handover", AppTheme.SUCCESS, Color.WHITE);
        handoverBtn.addActionListener(e -> executeSelectedHandover());
        t2Btns.add(handoverBtn);
        t2.add(t2Btns, BorderLayout.SOUTH);

        // Tab 3: Handover Audit Trail
        JPanel t3 = new JPanel(new BorderLayout(8, 8));
        t3.setBackground(AppTheme.BG_CARD);
        t3.setBorder(new EmptyBorder(12, 12, 12, 12));
        UIUtils.styleTable(auditTable);
        t3.add(new JScrollPane(auditTable), BorderLayout.CENTER);

        // Tab 4: Campus User Directory
        JPanel t4 = new JPanel(new BorderLayout(8, 8));
        t4.setBackground(AppTheme.BG_CARD);
        t4.setBorder(new EmptyBorder(12, 12, 12, 12));
        UIUtils.styleTable(usersTable);
        t4.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        tabs.addTab("Pending Claims Queue", t1);
        tabs.addTab("Approved Claims & Physical Handover", t2);
        tabs.addTab("Handover Audit Trail (Immutable)", t3);
        tabs.addTab("Campus Members Directory", t4);

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    private void refreshData() {
        if (!authService.isAdmin()) return;

        // 1. Pending Claims
        pendingClaimsList = claimService.getPendingClaims();
        pendingClaimsModel.setRowCount(0);
        for (Claim c : pendingClaimsList) {
            pendingClaimsModel.addRow(new Object[]{
                    "#" + c.getClaimId(),
                    c.getFoundItem().getItemName(),
                    c.getClaimant().getFullName(),
                    c.getClaimant().getCampusId(),
                    UIUtils.formatDate(c.getApproxDateLost()),
                    c.getStatus().name(),
                    UIUtils.formatDateTime(c.getSubmittedAt())
            });
        }

        // 2. Approved Claims
        approvedClaimsList = claimService.getApprovedClaims();
        approvedClaimsModel.setRowCount(0);
        for (Claim c : approvedClaimsList) {
            approvedClaimsModel.addRow(new Object[]{
                    "#" + c.getClaimId(),
                    c.getFoundItem().getItemName(),
                    c.getClaimant().getFullName(),
                    c.getClaimant().getCampusId(),
                    c.getFoundItem().getStorageLocation(),
                    c.getAdminRemarks()
            });
        }

        // 3. Handover Audit Trail
        List<ItemReturn> returns = returnService.getAllReturns();
        auditModel.setRowCount(0);
        for (ItemReturn r : returns) {
            auditModel.addRow(new Object[]{
                    "RET-" + r.getReturnId(),
                    "#" + (r.getClaim() != null ? r.getClaim().getClaimId() : ""),
                    r.getFoundItem() != null ? r.getFoundItem().getItemName() : "",
                    r.getReturnedToUser() != null ? r.getReturnedToUser().getFullName() + " (" + r.getReturnedToUser().getCampusId() + ")" : "",
                    r.getProcessedByAdmin() != null ? r.getProcessedByAdmin().getFullName() : "Admin",
                    UIUtils.formatDateTime(r.getHandoverTimestamp()),
                    r.getHandoverNotes()
            });
        }

        // 4. Campus Members Directory
        List<User> users = authService.getAllUsers();
        usersModel.setRowCount(0);
        for (User u : users) {
            usersModel.addRow(new Object[]{
                    "#" + u.getUserId(),
                    u.getFullName(),
                    u.getCollegeEmail(),
                    u.getCampusId(),
                    u.getDepartment(),
                    u.getPhoneNumber(),
                    u.getRole().getLabel()
            });
        }
    }

    private void reviewSelectedClaim() {
        int row = pendingClaimsTable.getSelectedRow();
        if (row < 0 || pendingClaimsList == null || row >= pendingClaimsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a claim from the queue to review.", "Select Claim", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Claim c = pendingClaimsList.get(row);
        new ReviewClaimDialog(mainFrame, c).setVisible(true);
    }

    private void executeSelectedHandover() {
        int row = approvedClaimsTable.getSelectedRow();
        if (row < 0 || approvedClaimsList == null || row >= approvedClaimsList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an approved claim to conduct handover.", "Select Claim", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Claim c = approvedClaimsList.get(row);
        new HandoverDialog(mainFrame, c).setVisible(true);
    }
}
