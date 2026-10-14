package com.campusfind.lostfound;

import com.campusfind.lostfound.config.DatabaseManager;
import com.campusfind.lostfound.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class CampusFindApp {

    public static void main(String[] args) {
        // 1. Setup Modern Flat Look and Feel
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // 2. Initialize Database & Seed Demo Records
        System.out.println("Starting CampusFind Desktop Application...");
        DatabaseManager.initialize();

        // 3. Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
