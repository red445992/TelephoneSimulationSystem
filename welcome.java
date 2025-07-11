package com.mycompany.tsm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class welcome extends JFrame {
    
    public welcome() {
        initComponents();
    }
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Telephone Simulation System");
        setResizable(false);
        
        // Create components
        JLabel titleLabel = new JLabel("Welcome to Telephone Simulation System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel logoLabel = createLogo();
        
        JLabel authorLabel = new JLabel("Submitted by: Aditya Malla Thakuri", SwingConstants.CENTER);
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel crnLabel = new JLabel("CRN: 022-307", SwingConstants.CENTER);
        crnLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton lostCallBtn = new JButton("Lost Call");
        lostCallBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lostCallBtn.setPreferredSize(new Dimension(120, 40));
        lostCallBtn.addActionListener(e -> openLostCall());
        
        JButton delayCallBtn = new JButton("Delay Call");
        delayCallBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        delayCallBtn.setPreferredSize(new Dimension(120, 40));
        delayCallBtn.addActionListener(e -> openDelayCall());
        
        // Layout
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(20, 0, 10, 0);
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1; gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(logoLabel, gbc);
        
        gbc.gridy = 2;
        mainPanel.add(authorLabel, gbc);
        
        gbc.gridy = 3;
        mainPanel.add(crnLabel, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.add(lostCallBtn);
        buttonPanel.add(delayCallBtn);
        
        gbc.gridy = 4; gbc.insets = new Insets(30, 0, 20, 0);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private JLabel createLogo() {
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        
        // Try to load logo files
        String[] logoFiles = {"logo.jpg", "logo.png", "logo.gif"};
        for (String fileName : logoFiles) {
            try {
                ImageIcon icon = new ImageIcon(fileName);
                if (icon.getIconWidth() > 0) {
                    Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    logoLabel.setIcon(new ImageIcon(img));
                    return logoLabel;
                }
            } catch (Exception ignored) {}
        }
        
        // Create simple logo if no image found
        logoLabel.setText("📞 TSS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        logoLabel.setPreferredSize(new Dimension(120, 120));
        return logoLabel;
    }
    
    private void openLostCall() {
        new MainTSM().setVisible(true);
        dispose();
    }
    
    private void openDelayCall() {
        new Delay().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> new welcome().setVisible(true));
    }
}
