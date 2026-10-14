package com.campusfind.lostfound.config;

import java.awt.*;

public class AppTheme {

    // Brand Palette
    public static final Color PRIMARY_NAVY = new Color(0x1E, 0x3A, 0x8A);     // #1E3A8A Deep Navy
    public static final Color PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);    // #1D4ED8 Navy Blue Hover
    public static final Color SECONDARY_PURPLE = new Color(0x7C, 0x3A, 0xED); // #7C3AED Purple Accent
    public static final Color PURPLE_HOVER = new Color(0x6D, 0x28, 0xD9);     // #6D28D9 Dark Purple
    public static final Color PURPLE_LIGHT = new Color(0xEE, 0xF2, 0xFF);     // Soft Purple Background
    
    // Semantic Colors
    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81);          // #10B981 Emerald
    public static final Color SUCCESS_LIGHT = new Color(0xEC, 0xFD, 0xF5);
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);          // #F59E0B Amber
    public static final Color WARNING_LIGHT = new Color(0xFF, 0xFB, 0xEB);
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);           // #EF4444 Crimson
    public static final Color DANGER_LIGHT = new Color(0xFE, 0xF2, 0xF2);
    public static final Color INFO = new Color(0x3B, 0x82, 0xF6);             // #3B82F6 Sky
    public static final Color INFO_LIGHT = new Color(0xEF, 0xF6, 0xFF);

    // Neutral Grays & Backgrounds
    public static final Color BG_MAIN = new Color(0xF8, 0xFA, 0xFC);          // #F8FAFC Off-White / Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(0x0F, 0x17, 0x2A);       // Slate 900 Dark Sidebar
    public static final Color BG_SIDEBAR_HOVER = new Color(0x1E, 0x29, 0x3B); // Slate 800
    public static final Color BG_SIDEBAR_ACTIVE = new Color(0x25, 0x63, 0xEB);// Blue 600
    
    public static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);        // Slate 900
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);       // Slate 500
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);           // Slate 200

    // Fonts
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
}
