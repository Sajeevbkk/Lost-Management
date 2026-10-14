package com.campusfind.lostfound.ui;

import com.campusfind.lostfound.config.AppTheme;
import com.campusfind.lostfound.ui.views.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel viewsPanel = new JPanel(cardLayout);

    private SidebarPanel sidebarPanel;
    private TopBarPanel topBarPanel;

    private HomeView homeView;
    private LostItemsView lostItemsView;
    private FoundItemsView foundItemsView;
    private SmartMatchesView smartMatchesView;
    private UserDashboardView userDashboardView;
    private AdminDashboardView adminDashboardView;
    private AboutView aboutView;

    public MainFrame() {
        super("CampusFind — Smart Lost & Found Management System");
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);

        // Sidebar and Top Bar
        sidebarPanel = new SidebarPanel(this);
        topBarPanel = new TopBarPanel(this);

        // Views
        homeView = new HomeView(this);
        lostItemsView = new LostItemsView(this);
        foundItemsView = new FoundItemsView(this);
        smartMatchesView = new SmartMatchesView(this);
        userDashboardView = new UserDashboardView(this);
        adminDashboardView = new AdminDashboardView(this);
        aboutView = new AboutView();

        viewsPanel.add(homeView, "home");
        viewsPanel.add(lostItemsView, "lost");
        viewsPanel.add(foundItemsView, "found");
        viewsPanel.add(smartMatchesView, "matches");
        viewsPanel.add(userDashboardView, "user-dashboard");
        viewsPanel.add(adminDashboardView, "admin-dashboard");
        viewsPanel.add(aboutView, "about");

        // Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebarPanel, BorderLayout.WEST);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(topBarPanel, BorderLayout.NORTH);
        rightContainer.add(viewsPanel, BorderLayout.CENTER);

        getContentPane().add(rightContainer, BorderLayout.CENTER);

        showView("home");
    }

    public void showView(String viewKey) {
        cardLayout.show(viewsPanel, viewKey);
        if (sidebarPanel != null) {
            sidebarPanel.setActiveView(viewKey);
        }

        // Refresh dynamic view contents
        if ("home".equalsIgnoreCase(viewKey) && homeView != null) {
            homeView.refreshData();
        } else if ("lost".equalsIgnoreCase(viewKey) && lostItemsView != null) {
            lostItemsView.loadItems();
        } else if ("found".equalsIgnoreCase(viewKey) && foundItemsView != null) {
            foundItemsView.loadItems();
        } else if ("matches".equalsIgnoreCase(viewKey) && smartMatchesView != null) {
            smartMatchesView.loadMatches();
        } else if ("user-dashboard".equalsIgnoreCase(viewKey) && userDashboardView != null) {
            userDashboardView.refreshData();
        } else if ("admin-dashboard".equalsIgnoreCase(viewKey) && adminDashboardView != null) {
            adminDashboardView.refreshView();
        }
    }
}
