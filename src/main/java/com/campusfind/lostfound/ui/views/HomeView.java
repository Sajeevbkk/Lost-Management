package com.campusfind.lostfound.ui.views;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.model.FoundItem;
import com.campusfind.lostfound.model.FoundStatus;
import com.campusfind.lostfound.model.LostItem;
import com.campusfind.lostfound.model.LostStatus;
import com.campusfind.lostfound.service.AuthService;
import com.campusfind.lostfound.service.ItemService;
import com.campusfind.lostfound.service.StatsService;
import com.campusfind.lostfound.ui.MainFrame;
import com.campusfind.lostfound.ui.dialogs.ItemDetailDialog;
import com.campusfind.lostfound.ui.dialogs.ReportItemDialog;
import com.campusfind.lostfound.util.UIUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class HomeView extends JPanel {

    private final MainFrame mainFrame;
    private final StatsService statsService = StatsService.getInstance();
    private final ItemService itemService = ItemService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    private final JLabel lblLostCount = new JLabel("0");
    private final JLabel lblFoundCount = new JLabel("0");
    private final JLabel lblReturnedCount = new JLabel("0");
    private final JLabel lblClaimsCount = new JLabel("0");
    private final JLabel lblRecoveryRate = new JLabel("0.0%");

    private final JPanel recentFoundPanel = new JPanel();
    private final JPanel recentLostPanel = new JPanel();

    public HomeView(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_MAIN);
        initComponents();

        itemService.addChangeListener(this::refreshData);
        authService.addAuthListener(this::refreshData);
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(AppTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Hero Banner Card
        scrollContent.add(createHeroCard());
        scrollContent.add(Box.createVerticalStrut(18));

        // 2. Statistics Grid
        scrollContent.add(createStatsGrid());
        scrollContent.add(Box.createVerticalStrut(20));

        // 3. Two-Column Recent Activity Section
        JPanel recentActivitySection = new JPanel(new GridLayout(1, 2, 16, 0));
        recentActivitySection.setBackground(AppTheme.BG_MAIN);
        recentActivitySection.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Column 1: Recent Discovered Items
        JPanel foundCol = UIUtils.createCard();
        foundCol.setLayout(new BorderLayout(8, 8));

        JPanel foundHead = new JPanel(new BorderLayout());
        foundHead.setBackground(AppTheme.BG_CARD);
        JLabel foundTitle = new JLabel("Recently Discovered Belongings");
        foundTitle.setFont(AppTheme.FONT_SUBTITLE);
        foundTitle.setForeground(AppTheme.PRIMARY_NAVY);

        JButton viewAllFound = new JButton("View All ->");
        viewAllFound.setFont(AppTheme.FONT_SMALL);
        viewAllFound.setBorderPainted(false);
        viewAllFound.setContentAreaFilled(false);
        viewAllFound.setForeground(AppTheme.SECONDARY_PURPLE);
        viewAllFound.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllFound.addActionListener(e -> mainFrame.showView("found"));

        foundHead.add(foundTitle, BorderLayout.WEST);
        foundHead.add(viewAllFound, BorderLayout.EAST);
        foundCol.add(foundHead, BorderLayout.NORTH);

        recentFoundPanel.setLayout(new BoxLayout(recentFoundPanel, BoxLayout.Y_AXIS));
        recentFoundPanel.setBackground(AppTheme.BG_CARD);
        foundCol.add(recentFoundPanel, BorderLayout.CENTER);

        // Column 2: Recent Lost Reports
        JPanel lostCol = UIUtils.createCard();
        lostCol.setLayout(new BorderLayout(8, 8));

        JPanel lostHead = new JPanel(new BorderLayout());
        lostHead.setBackground(AppTheme.BG_CARD);
        JLabel lostTitle = new JLabel("Recent Missing Item Reports");
        lostTitle.setFont(AppTheme.FONT_SUBTITLE);
        lostTitle.setForeground(AppTheme.PRIMARY_NAVY);

        JButton viewAllLost = new JButton("View All ->");
        viewAllLost.setFont(AppTheme.FONT_SMALL);
        viewAllLost.setBorderPainted(false);
        viewAllLost.setContentAreaFilled(false);
        viewAllLost.setForeground(AppTheme.SECONDARY_PURPLE);
        viewAllLost.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllLost.addActionListener(e -> mainFrame.showView("lost"));

        lostHead.add(lostTitle, BorderLayout.WEST);
        lostHead.add(viewAllLost, BorderLayout.EAST);
        lostCol.add(lostHead, BorderLayout.NORTH);

        recentLostPanel.setLayout(new BoxLayout(recentLostPanel, BoxLayout.Y_AXIS));
        recentLostPanel.setBackground(AppTheme.BG_CARD);
        lostCol.add(recentLostPanel, BorderLayout.CENTER);

        recentActivitySection.add(foundCol);
        recentActivitySection.add(lostCol);

        scrollContent.add(recentActivitySection);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeroCard() {
        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBackground(AppTheme.PRIMARY_NAVY);
        hero.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.PRIMARY_NAVY.darker(), 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tag = new JLabel("CAMPUS SECURITY & RECOVERY NETWORK");
        tag.setFont(AppTheme.FONT_BADGE);
        tag.setForeground(new Color(0x93, 0xC5, 0xFD)); // Light blue tag
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Lost Something? Let's Find It Together!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html>A centralized campus management system connecting students, faculty, and campus security.<br>Report missing belongings, catalog discovered items, and explore automated AI similarity matching.</html>");
        desc.setFont(AppTheme.FONT_REGULAR);
        desc.setForeground(new Color(0xEB, 0xF2, 0xFE));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(AppTheme.PRIMARY_NAVY);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton reportLostBtn = UIUtils.createButton("+ Report Lost Item", Color.WHITE, AppTheme.PRIMARY_NAVY);
        reportLostBtn.addActionListener(e -> new ReportItemDialog(mainFrame, false).setVisible(true));

        JButton reportFoundBtn = UIUtils.createButton("+ Report Found Item", AppTheme.SECONDARY_PURPLE, Color.WHITE);
        reportFoundBtn.addActionListener(e -> new ReportItemDialog(mainFrame, true).setVisible(true));

        JButton smartMatchesBtn = UIUtils.createOutlineButton("Explore Smart Matches", Color.WHITE);
        smartMatchesBtn.addActionListener(e -> mainFrame.showView("matches"));

        btnRow.add(reportLostBtn);
        btnRow.add(reportFoundBtn);
        btnRow.add(smartMatchesBtn);

        hero.add(tag);
        hero.add(Box.createVerticalStrut(6));
        hero.add(title);
        hero.add(Box.createVerticalStrut(6));
        hero.add(desc);
        hero.add(Box.createVerticalStrut(18));
        hero.add(btnRow);

        return hero;
    }

    private JPanel createStatsGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
        grid.setBackground(AppTheme.BG_MAIN);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        grid.add(createStatCard("Reported Lost", lblLostCount, AppTheme.WARNING));
        grid.add(createStatCard("Discovered Found", lblFoundCount, AppTheme.INFO));
        grid.add(createStatCard("Successful Returns", lblReturnedCount, AppTheme.SUCCESS));
        grid.add(createStatCard("Active Claims", lblClaimsCount, AppTheme.SECONDARY_PURPLE));
        grid.add(createStatCard("Recovery Rate", lblRecoveryRate, AppTheme.PRIMARY_NAVY));

        return grid;
    }

    private JPanel createStatCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = UIUtils.createCard();
        card.setLayout(new BorderLayout(6, 6));

        JLabel titleLbl = new JLabel(label);
        titleLbl.setFont(AppTheme.FONT_SMALL);
        titleLbl.setForeground(AppTheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        lblLostCount.setText(String.valueOf(statsService.getTotalLost()));
        lblFoundCount.setText(String.valueOf(statsService.getTotalFound()));
        lblReturnedCount.setText(String.valueOf(statsService.getTotalReturned()));
        lblClaimsCount.setText(String.valueOf(statsService.getActiveClaims()));
        lblRecoveryRate.setText(String.format("%.1f%%", statsService.getRecoveryRate()));

        // Populate recent found items
        recentFoundPanel.removeAll();
        List<FoundItem> foundList = itemService.getAllFoundItems();
        int count = 0;
        for (FoundItem item : foundList) {
            if (count++ >= 4) break;
            recentFoundPanel.add(createRecentFoundRow(item));
            recentFoundPanel.add(Box.createVerticalStrut(6));
        }
        if (foundList.isEmpty()) {
            recentFoundPanel.add(new JLabel("No discovered items cataloged yet."));
        }

        // Populate recent lost items
        recentLostPanel.removeAll();
        List<LostItem> lostList = itemService.getAllLostItems();
        count = 0;
        for (LostItem item : lostList) {
            if (count++ >= 4) break;
            recentLostPanel.add(createRecentLostRow(item));
            recentLostPanel.add(Box.createVerticalStrut(6));
        }
        if (lostList.isEmpty()) {
            recentLostPanel.add(new JLabel("No missing item reports."));
        }

        recentFoundPanel.revalidate();
        recentFoundPanel.repaint();
        recentLostPanel.revalidate();
        recentLostPanel.repaint();
    }

    private JPanel createRecentFoundRow(FoundItem item) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(0xF8, 0xFA, 0xFC));
        row.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel nameLbl = new JLabel(item.getItemName());
        nameLbl.setFont(AppTheme.FONT_BOLD);
        nameLbl.setForeground(AppTheme.TEXT_DARK);

        JLabel subLbl = new JLabel(item.getCategory().getDisplayName() + " • " + item.getLocationFound());
        subLbl.setFont(AppTheme.FONT_SMALL);
        subLbl.setForeground(AppTheme.TEXT_MUTED);

        left.add(nameLbl);
        left.add(subLbl);

        JButton viewBtn = new JButton("Details");
        viewBtn.setFont(AppTheme.FONT_SMALL);
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> new ItemDetailDialog(mainFrame, item).setVisible(true));

        row.add(left, BorderLayout.CENTER);
        row.add(viewBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel createRecentLostRow(LostItem item) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(0xF8, 0xFA, 0xFC));
        row.setBorder(new CompoundBorder(
                new LineBorder(AppTheme.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel nameLbl = new JLabel(item.getItemName());
        nameLbl.setFont(AppTheme.FONT_BOLD);
        nameLbl.setForeground(AppTheme.TEXT_DARK);

        JLabel subLbl = new JLabel(item.getCategory().getDisplayName() + " • " + item.getLocationLost());
        subLbl.setFont(AppTheme.FONT_SMALL);
        subLbl.setForeground(AppTheme.TEXT_MUTED);

        left.add(nameLbl);
        left.add(subLbl);

        JButton viewBtn = new JButton("Details");
        viewBtn.setFont(AppTheme.FONT_SMALL);
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> new ItemDetailDialog(mainFrame, item).setVisible(true));

        row.add(left, BorderLayout.CENTER);
        row.add(viewBtn, BorderLayout.EAST);
        return row;
    }
}
