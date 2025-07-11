package com.mycompany.tsm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainTSM extends JFrame {
    
    // UI Components
    private JTextField fromField, toField, lengthField, arrivalField;
    private JTextField processedField, completedField, blockedField, busyField, clockField;
    private JTextField linksMaxField, linksInUseField;
    private JButton simulateBtn;
    
    // Simulation Data (from original)
    private int[] line = new int[8];
    private int[][] used = new int[3][3];
    private int from, to, arrival, length, end;
    private int processed = 0, completed = 0, blocke = 0, busy = 0, clock = 0;
    private int max = 3, inuse = 0, stat;
    private int totalCalls = 0;

    public MainTSM() {
        initComponents();
        links();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Lost Call Simulation (Erlang-B)");
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Call Parameters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("From Line:"), gbc);
        gbc.gridx = 1;
        fromField = new JTextField(5);
        inputPanel.add(fromField, gbc);
        
        gbc.gridx = 2;
        inputPanel.add(new JLabel("To Line:"), gbc);
        gbc.gridx = 3;
        toField = new JTextField(5);
        inputPanel.add(toField, gbc);
        
        gbc.gridx = 4;
        inputPanel.add(new JLabel("Length:"), gbc);
        gbc.gridx = 5;
        lengthField = new JTextField(5);
        inputPanel.add(lengthField, gbc);
        
        gbc.gridx = 6;
        inputPanel.add(new JLabel("Arrival Time:"), gbc);
        gbc.gridx = 7;
        arrivalField = new JTextField(5);
        inputPanel.add(arrivalField, gbc);
        
        gbc.gridx = 8;
        simulateBtn = new JButton("Simulate");
        simulateBtn.addActionListener(this::buttonAction);
        inputPanel.add(simulateBtn, gbc);

        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 7, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        
        // Labels
        statsPanel.add(new JLabel("Processed:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Completed:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Blocked:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Busy:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Clock:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Max Links:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Links Used:", SwingConstants.CENTER));
        
        // Fields
        processedField = new JTextField("0"); processedField.setEditable(false);
        completedField = new JTextField("0"); completedField.setEditable(false);
        blockedField = new JTextField("0"); blockedField.setEditable(false);
        busyField = new JTextField("0"); busyField.setEditable(false);
        clockField = new JTextField("0"); clockField.setEditable(false);
        linksMaxField = new JTextField("3"); linksMaxField.setEditable(false);
        linksInUseField = new JTextField("0"); linksInUseField.setEditable(false);
        
        statsPanel.add(processedField);
        statsPanel.add(completedField);
        statsPanel.add(blockedField);
        statsPanel.add(busyField);
        statsPanel.add(clockField);
        statsPanel.add(linksMaxField);
        statsPanel.add(linksInUseField);

        // Visual Panel
        JPanel visualPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintSimulation(g);
            }
        };
        visualPanel.setPreferredSize(new Dimension(700, 350));
        visualPanel.setBorder(BorderFactory.createTitledBorder("Visual Simulation"));
        visualPanel.setBackground(Color.WHITE);

        add(inputPanel, BorderLayout.NORTH);
        add(visualPanel, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void paintSimulation(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw link labels and lines
        g2d.setColor(Color.BLACK);
        g2d.drawString("Link 1", 540, 90);
        g2d.drawString("Link 2", 590, 90);
        g2d.drawString("Link 3", 640, 90);
        
        g2d.drawLine(550, 100, 550, 300);
        g2d.drawLine(600, 100, 600, 300);
        g2d.drawLine(650, 100, 650, 300);

        // Draw lines and their labels
        for (int i = 1; i <= 8; i++) {
            g2d.drawString("Line " + i, 500, 100 + i * 25);
            g2d.drawLine(525, 100 + i * 25, 650, 100 + i * 25);
        }

        // Draw active connections
        for (int i = 0; i < inuse; i++) {
            int yFrom = 100 + used[i][0] * 25;
            int yTo = 100 + used[i][1] * 25;
            
            switch (i) {
                case 0:
                    g2d.setColor(Color.GREEN);
                    g2d.fillRect(548, Math.min(yFrom, yTo), 4, Math.abs(yTo - yFrom));
                    break;
                case 1:
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(598, Math.min(yFrom, yTo), 4, Math.abs(yTo - yFrom));
                    break;
                case 2:
                    g2d.setColor(Color.RED);
                    g2d.fillRect(648, Math.min(yFrom, yTo), 4, Math.abs(yTo - yFrom));
                    break;
            }
        }

        // Status info
        g2d.setColor(Color.BLACK);
        g2d.drawString("Active connections: " + inuse, 500, 320);
        g2d.drawString("Current time: " + clock, 500, 340);
    }

    private void buttonAction(ActionEvent e) {
        // Check for Ctrl+Click for detailed statistics
        if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0) {
            showDetailedStatistics();
            return;
        }
        
        // Normal simulation
        inite();
    }

    private void showDetailedStatistics() {
        double blockingRate = totalCalls > 0 ? (double) blocke / totalCalls * 100 : 0;
        double busyRate = totalCalls > 0 ? (double) busy / totalCalls * 100 : 0;
        double completionRate = totalCalls > 0 ? (double) completed / totalCalls * 100 : 0;
        
        String stats = String.format(
            "=== PROFESSIONAL STATISTICS ===\n\n" +
            "Blocking Probability: %.2f%%\n" +
            "Call Completion Rate: %.2f%%\n" +
            "Busy Signal Rate: %.2f%%\n" +
            "Total Calls: %d\n" +
            "Completed Calls: %d\n" +
            "Blocked Calls: %d\n" +
            "Busy Signals: %d\n" +
            "Current Time: %d\n" +
            "Links in Use: %d/%d",
            blockingRate, completionRate, busyRate,
            totalCalls, completed, blocke, busy, clock, inuse, max
        );
        
        JOptionPane.showMessageDialog(this, stats, "Detailed Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    // Original simulation methods (cleaned up)
    public void links() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                used[i][j] = 0;
            }
        }
        for (int i = 0; i < 8; i++) {
            line[i] = 0;
        }
    }

    public void inite() {
        try {
            String fromt = fromField.getText().trim();
            String tot = toField.getText().trim();
            String lengtht = lengthField.getText().trim();
            String arrivalt = arrivalField.getText().trim();
            
            if (fromt.isEmpty() || tot.isEmpty() || lengtht.isEmpty() || arrivalt.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill in all fields", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            from = Integer.parseInt(fromt);
            to = Integer.parseInt(tot);
            length = Integer.parseInt(lengtht);
            arrival = Integer.parseInt(arrivalt);
            
            if (from < 1 || from > 8 || to < 1 || to > 8) {
                JOptionPane.showMessageDialog(this, 
                    "Line numbers must be between 1 and 8", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (length <= 0 || arrival < 0) {
                JOptionPane.showMessageDialog(this, 
                    "Length must be positive and arrival time non-negative", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid integers", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        totalCalls++;
        clock = arrival;
        callcompletecheck();
        stat = callstat();
        
        if (stat == 0) {
            end = arrival + length;
            connection();
            used[inuse][0] = from;
            used[inuse][1] = to;
            used[inuse][2] = end;
            inuse++;
            completed++;
        }
        
        processed++;
        updateDisplay();
        repaint();
    }

    public void callcompletecheck() {
        for (int i = 0; i < inuse; i++) {
            if (used[i][2] <= arrival && used[i][2] > 0) {
                clock = used[i][2];
                int fromLine = used[i][0] - 1;
                int toLine = used[i][1] - 1;
                line[fromLine] = 0;
                line[toLine] = 0;
                
                for (int j = i; j < inuse - 1; j++) {
                    used[j][0] = used[j + 1][0];
                    used[j][1] = used[j + 1][1];
                    used[j][2] = used[j + 1][2];
                }
                used[inuse - 1][0] = used[inuse - 1][1] = used[inuse - 1][2] = 0;
                inuse--;
                i--;
            } else {
                clock = arrival;
            }
        }
    }

    public int callstat() {
        if (line[from - 1] == 1 || line[to - 1] == 1) {
            busy++;
            clock = arrival;
            return 1;
        }
        if (inuse == 3) {
            blocke++;
            clock = arrival;
            return 2;
        }
        return 0;
    }

    public void connection() {
        line[from - 1] = 1;
        line[to - 1] = 1;
    }

    private void updateDisplay() {
        processedField.setText(String.valueOf(processed));
        completedField.setText(String.valueOf(completed));
        blockedField.setText(String.valueOf(blocke));
        busyField.setText(String.valueOf(busy));
        clockField.setText(String.valueOf(clock));
        linksInUseField.setText(String.valueOf(inuse));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainTSM().setVisible(true));
    }
}
