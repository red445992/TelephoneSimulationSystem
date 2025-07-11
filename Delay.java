package com.mycompany.tsm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class Delay extends JFrame {
    
    // UI Components
    private JTextField fromField, toField, lengthField, arrivalField;
    private JTextField totalCallsField, answeredField, queueSizeField, blockedField, clockField;
    private JTextField linksMaxField, linksInUseField;
    private JTextField[] queueFields; // For displaying first queued call
    private JButton simulateBtn;
    
    // Simulation Data
    private int[] line = new int[8];
    private int[][] used = new int[3][3];
    private int from, to, arrival, length, end;
    private int processed = 0, completed = 0, blocke = 0, busy = 0, clock = 0;
    private int max = 3, inuse = 0, stat;
    private int[] delay = new int[3]; // For UI compatibility
    
    // Enhanced queue management for Erlang-C
    private ArrayList<QueuedCall> waitingQueue = new ArrayList<>();
    private PriorityQueue<Event> eventQueue = new PriorityQueue<>(Comparator.comparingInt(e -> e.time));
    
    // Enhanced statistics
    private double totalWaitTime = 0;
    private int callsDelayed = 0;
    private int totalCalls = 0;
    private int callsAnswered = 0;
    
    // Inner classes for proper simulation
    class QueuedCall {
        int from, to, length, arrivalTime;
        
        QueuedCall(int from, int to, int length, int arrivalTime) {
            this.from = from;
            this.to = to;
            this.length = length;
            this.arrivalTime = arrivalTime;
        }
    }
    
    class Event {
        int time;
        String type;
        int from, to, linkIndex;
        
        Event(int time, String type, int from, int to, int linkIndex) {
            this.time = time;
            this.type = type;
            this.from = from;
            this.to = to;
            this.linkIndex = linkIndex;
        }
    }

    public Delay() {
        initComponents();
        links();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Delay Call Simulation (Erlang-C)");
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
        JPanel statsPanel = new JPanel(new GridLayout(3, 5, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics & Queue Info"));
        
        // Row 1 - Main Statistics
        statsPanel.add(new JLabel("Total Calls:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Answered:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Queue Size:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Blocked:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Clock:", SwingConstants.CENTER));
        
        totalCallsField = new JTextField("0"); totalCallsField.setEditable(false);
        answeredField = new JTextField("0"); answeredField.setEditable(false);
        queueSizeField = new JTextField("0"); queueSizeField.setEditable(false);
        blockedField = new JTextField("0"); blockedField.setEditable(false);
        clockField = new JTextField("0"); clockField.setEditable(false);
        
        statsPanel.add(totalCallsField);
        statsPanel.add(answeredField);
        statsPanel.add(queueSizeField);
        statsPanel.add(blockedField);
        statsPanel.add(clockField);
        
        // Row 2 - Link Information
        statsPanel.add(new JLabel("Max Links:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Links Used:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Queue From:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Queue To:", SwingConstants.CENTER));
        statsPanel.add(new JLabel("Queue Length:", SwingConstants.CENTER));
        
        linksMaxField = new JTextField("3"); linksMaxField.setEditable(false);
        linksInUseField = new JTextField("0"); linksInUseField.setEditable(false);
        
        queueFields = new JTextField[3];
        for (int i = 0; i < 3; i++) {
            queueFields[i] = new JTextField("0");
            queueFields[i].setEditable(false);
        }
        
        statsPanel.add(linksMaxField);
        statsPanel.add(linksInUseField);
        statsPanel.add(queueFields[0]); // Queue From
        statsPanel.add(queueFields[1]); // Queue To  
        statsPanel.add(queueFields[2]); // Queue Length

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

        // Draw active connections with different colors per link
        Color[] linkColors = {Color.GREEN, Color.BLUE, Color.RED};
        for (int i = 0; i < inuse; i++) {
            int yFrom = 100 + used[i][0] * 25;
            int yTo = 100 + used[i][1] * 25;
            int linkX = 550 + i * 50;
            
            g2d.setColor(linkColors[i % linkColors.length]);
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(linkX, Math.min(yFrom, yTo), linkX, Math.max(yFrom, yTo));
            g2d.setStroke(new BasicStroke(1));
        }

        // Draw queue visualization
        g2d.setColor(Color.ORANGE);
        g2d.drawString("Queue: " + waitingQueue.size() + " calls waiting", 500, 320);
        if (!waitingQueue.isEmpty()) {
            QueuedCall first = waitingQueue.get(0);
            g2d.drawString("Next: Line " + first.from + " → Line " + first.to, 500, 340);
        }
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
        double avgWaitTime = callsDelayed > 0 ? totalWaitTime / callsDelayed : 0;
        double answerRate = totalCalls > 0 ? (double) callsAnswered / totalCalls * 100 : 0;
        double blockingRate = totalCalls > 0 ? (double) blocke / totalCalls * 100 : 0;
        double maxWaitTime = 0;
        
        // Calculate max wait time for calls currently in queue
        for (QueuedCall call : waitingQueue) {
            double waitTime = clock - call.arrivalTime;
            if (waitTime > maxWaitTime) maxWaitTime = waitTime;
        }
        
        String stats = String.format(
            "=== PROFESSIONAL STATISTICS ===\n\n" +
            "Answer Rate: %.2f%%\n" +
            "Average Wait Time: %.2f units\n" +
            "Maximum Wait Time: %.2f units\n" +
            "Current Queue Length: %d calls\n" +
            "Total Queued Calls: %d\n" +
            "Blocking Rate: %.2f%%\n" +
            "Total Calls: %d\n" +
            "Successfully Answered: %d\n" +
            "Current Time: %d\n" +
            "Links in Use: %d/%d",
            answerRate, avgWaitTime, maxWaitTime,
            waitingQueue.size(), callsDelayed, blockingRate,
            totalCalls, callsAnswered, clock, inuse, max
        );
        
        JOptionPane.showMessageDialog(this, stats, "Detailed Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    // Simulation methods
    public void links() {
        for (int i = 0; i < 3; i++) {
            delay[i] = 0;
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
        
        processNewCall();
        updateDisplay();
        repaint();
    }

    public void processNewCall() {
        totalCalls++;
        
        if (arrival > clock) {
            clock = arrival;
        }
        
        // Process any completed calls
        processEventQueue();
        
        // Check call status
        stat = callstat();
        
        if (stat == 0) {
            // Call can be connected immediately
            end = arrival + length;
            connection();
            used[inuse][0] = from;
            used[inuse][1] = to;
            used[inuse][2] = end;
            inuse++;
            completed++;
            processed++;
            callsAnswered++;
            
            // Schedule completion event
            eventQueue.offer(new Event(end, "COMPLETION", from, to, inuse - 1));
            
        } else if (stat == 1) {
            // Lines busy or no links available - add to queue (Erlang-C behavior)
            QueuedCall queuedCall = new QueuedCall(from, to, length, arrival);
            waitingQueue.add(queuedCall);
            callsDelayed++;
            
            // Update display for first call in queue
            updateQueueDisplay();
        }
    }

    public void processEventQueue() {
        while (!eventQueue.isEmpty() && eventQueue.peek().time <= clock) {
            Event event = eventQueue.poll();
            
            if (event.type.equals("COMPLETION")) {
                // Free the lines
                line[event.from - 1] = 0;
                line[event.to - 1] = 0;
                
                // Remove from used array
                for (int i = 0; i < inuse; i++) {
                    if (used[i][0] == event.from && used[i][1] == event.to) {
                        for (int j = i; j < inuse - 1; j++) {
                            used[j][0] = used[j + 1][0];
                            used[j][1] = used[j + 1][1];
                            used[j][2] = used[j + 1][2];
                        }
                        used[inuse - 1][0] = used[inuse - 1][1] = used[inuse - 1][2] = 0;
                        inuse--;
                        break;
                    }
                }
                
                // Try to serve next call in queue
                processWaitingQueue();
            }
        }
    }

    public void processWaitingQueue() {
        while (!waitingQueue.isEmpty() && inuse < max) {
            QueuedCall nextCall = waitingQueue.get(0);
            
            // Check if required lines are free
            if (line[nextCall.from - 1] == 0 && line[nextCall.to - 1] == 0) {
                // Serve the call
                waitingQueue.remove(0);
                
                // Calculate wait time
                int waitTime = clock - nextCall.arrivalTime;
                totalWaitTime += waitTime;
                
                // Set up connection
                line[nextCall.from - 1] = line[nextCall.to - 1] = 1;
                used[inuse][0] = nextCall.from;
                used[inuse][1] = nextCall.to;
                used[inuse][2] = clock + nextCall.length;
                inuse++;
                
                completed++;
                processed++;
                callsAnswered++;
                
                // Schedule completion
                eventQueue.offer(new Event(clock + nextCall.length, "COMPLETION", 
                                         nextCall.from, nextCall.to, inuse - 1));
            } else {
                break; // Can't serve this call yet
            }
        }
        
        updateQueueDisplay();
    }

    public int callstat() {
        // Check if either line is busy
        if (line[from - 1] == 1 || line[to - 1] == 1) {
            busy++;
            return 1;
        }
        
        // Check if all links are busy
        if (inuse >= max) {
            // In Erlang-C, this also goes to queue unless we want to implement blocking
            return 1;
        }
        
        return 0;
    }

    public void connection() {
        line[from - 1] = 1;
        line[to - 1] = 1;
    }

    private void updateQueueDisplay() {
        if (!waitingQueue.isEmpty()) {
            QueuedCall first = waitingQueue.get(0);
            delay[0] = first.from;
            delay[1] = first.to;
            delay[2] = first.length;
        } else {
            delay[0] = delay[1] = delay[2] = 0;
        }
    }

    private void updateDisplay() {
        totalCallsField.setText(String.valueOf(totalCalls));
        answeredField.setText(String.valueOf(callsAnswered));
        queueSizeField.setText(String.valueOf(waitingQueue.size()));
        blockedField.setText(String.valueOf(blocke));
        clockField.setText(String.valueOf(clock));
        linksInUseField.setText(String.valueOf(inuse));
        
        // Update queue info
        queueFields[0].setText(String.valueOf(delay[0]));
        queueFields[1].setText(String.valueOf(delay[1]));
        queueFields[2].setText(String.valueOf(delay[2]));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Delay().setVisible(true));
    }
}
