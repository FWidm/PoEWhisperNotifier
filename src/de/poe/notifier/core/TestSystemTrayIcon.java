package de.poe.notifier.core;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.*;
import java.net.URI;

import javax.swing.*;

public class TestSystemTrayIcon {
    private final static SystemTray tray = SystemTray.getSystemTray();
    private static TrayIcon trayIcon;
    static int i = 1;

    public static void main(String[] args) {
        trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("icon.png"), "App");
        JButton testBtn = new JButton("Test System Tray Message");
        JFrame frame = new JFrame("Test System Tray");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("Successfully clicking the icon will launch google.com");
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(testBtn, BorderLayout.NORTH);
        panel.add(label, BorderLayout.CENTER);
        frame.setSize(new Dimension(500, 100));
        frame.add(panel);
        frame.setVisible(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println(e.getStackTrace());
        }
        trayIcon.getActionCommand();
        trayIcon.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("http://www.google.com"));
            } catch (Exception e1) {
                System.out.println(e1.getStackTrace());
            }
        });
        testBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trayIcon.displayMessage("Tray Icon Title", "This is a test message for the tray", MessageType.INFO);
            }
        });
    }
}
