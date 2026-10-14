package com.campusfind.lostfound.ui.dialogs;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ItemService;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ItemDetailDialog extends JDialog {

    private final LostItem lostItem;
    private final FoundItem foundItem;
    private final AuthService authService = AuthService.getInstance();
    private final ItemService itemService = ItemService.getInstance();

    public ItemDetailDialog(Frame parent, LostItem lostItem) {
        super(parent, "Lost Item Details - " + (lostItem != null ? lostItem.getItemName() : ""), true);
        this.lostItem = lostItem;
        this.foundItem = null;
        initComponents();
    }

    public ItemDetailDialog(Frame parent, FoundItem foundItem) {
        super(parent, "Discovered Item Details - " + (foundItem != null ? foundItem.getItemName() : ""), true);
        this.lostItem = null;
        this.foundItem = foundItem;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_MAIN);
        setSize(520, 560);
        setLocationRelativeTo(getParent());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BG_MAIN);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        boolean isLost = (lostItem != null);
        String name = isLost ? lostItem.getItemName() : foundItem.getItemName();
        Category cat = isLost ? lostItem.getCategory() : foundItem.getCategory();
        String status = isLost ? lostItem.getStatus().name() : foundItem.getStatus().name();
        User reporter = isLost ? lostItem.getUser() : foundItem.getUser();
        String dateStr = isLost ? UIUtils.formatDate(lostItem.getDateLost()) : UIUtils.formatDate(foundItem.getDateFound());
        String location = isLost ? lostItem.getLocationLost() : foundItem.getLocationFound();
        String color = isLost ? lostItem.getColorFeatures() : foundItem.getColorFeatures();
        String desc = isLost ? lostItem.getDescription() : foundItem.getDescription();
        String pointInfo = isLost ? ("Contact Phone: " + lostItem.getContactPhone()) : ("Storage Pickup Point: " + foundItem.getStorageLocation());

        // Header Title
        JLabel titleLbl = new JLabel(name);
        titleLbl.setFont(AppTheme.FONT_HEADER);
        titleLbl.setForeground(AppTheme.PRIMARY_NAVY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setBackground(AppTheme.BG_MAIN);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgeRow.add(UIUtils.createBadge(isLost ? "LOST ITEM" : "FOUND ITEM", AppTheme.PURPLE_LIGHT, AppTheme.SECONDARY_PURPLE));
        badgeRow.add(UIUtils.createBadge(cat.getDisplayName(), new Color(0xDB, 0xEA, 0xFE), AppTheme.PRIMARY_NAVY));
        badgeRow.add(UIUtils.createBadge(status, new Color(0xEC, 0xFD, 0xF5), AppTheme.SUCCESS));

        mainPanel.add(titleLbl);
        mainPanel.add(Box.createVerticalStrut(6));
        mainPanel.add(badgeRow);
        mainPanel.add(Box.createVerticalStrut(14));

        // Details Grid Card
        JPanel card = UIUtils.createCard();
        card.setLayout(new GridLayout(5, 2, 8, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(new JLabel("Reported By:"));
        card.add(new JLabel(reporter != null ? reporter.getFullName() + " (" + reporter.getCampusId() + ")" : "Campus Member"));

        card.add(new JLabel(isLost ? "Date Lost:" : "Date Discovered:"));
        card.add(new JLabel(dateStr));

        card.add(new JLabel("Campus Location:"));
        card.add(new JLabel(location));

        card.add(new JLabel("Color & Features:"));
        card.add(new JLabel(color));

        card.add(new JLabel(isLost ? "Contact:" : "Pickup Point:"));
        JLabel pLbl = new JLabel(pointInfo);
        pLbl.setFont(AppTheme.FONT_BOLD);
        pLbl.setForeground(AppTheme.PRIMARY_NAVY);
        card.add(pLbl);

        mainPanel.add(card);
        mainPanel.add(Box.createVerticalStrut(12));

        // Full Description Card
        JPanel descCard = UIUtils.createCard();
        descCard.setLayout(new BorderLayout(4, 4));
        descCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descHeader = new JLabel("Full Description & Specific Details:");
        descHeader.setFont(AppTheme.FONT_BOLD);

        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(AppTheme.FONT_REGULAR);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(0xF8, 0xFA, 0xFC));
        descArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        descCard.add(descHeader, BorderLayout.NORTH);
        descCard.add(new JScrollPane(descArea), BorderLayout.CENTER);

        mainPanel.add(new JScrollPane(descCard));
        mainPanel.add(Box.createVerticalStrut(16));

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(AppTheme.BG_MAIN);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (!isLost && foundItem != null && foundItem.getStatus() == FoundStatus.AVAILABLE) {
            JButton claimBtn = UIUtils.createButton("Claim Ownership of This Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
            claimBtn.addActionListener(e -> {
                dispose();
                new ClaimItemDialog((Frame) getParent(), foundItem).setVisible(true);
            });
            actionPanel.add(claimBtn);
        } else if (isLost && lostItem != null && lostItem.getStatus() == LostStatus.REPORTED) {
            User current = authService.getCurrentUser();
            boolean isOwner = (current != null && lostItem.getUser() != null && current.getUserId().equals(lostItem.getUser().getUserId()));
            boolean isAdmin = authService.isAdmin();
            if (isOwner || isAdmin) {
                JButton resolveBtn = UIUtils.createButton("Mark as Resolved / Found", AppTheme.SUCCESS, Color.WHITE);
                resolveBtn.addActionListener(e -> {
                    itemService.updateLostStatus(lostItem.getLostId(), LostStatus.RESOLVED);
                    JOptionPane.showMessageDialog(this, "Item marked as resolved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                });
                actionPanel.add(resolveBtn);
            }
        }

        JButton closeBtn = UIUtils.createOutlineButton("Close", AppTheme.TEXT_MUTED);
        closeBtn.addActionListener(e -> dispose());
        actionPanel.add(closeBtn);

        mainPanel.add(actionPanel);

        add(mainPanel, BorderLayout.CENTER);
    }
}
