package de.poe.notifier.gui;

import de.poe.notifier.core.ColorStatus;
import de.poe.notifier.core.PoeWhisperController;
import de.poe.notifier.core.interfaces.WhisperSubscriber;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class PoEWhisperNotifierGUI implements WhisperSubscriber {
    private PoeWhisperController controller;

    private JFrame frmPoewhispernotifier;

    private JTextPane txtWhisper;
    private JLabel lblLastUpdate;

    private boolean alwaysOnTop = false;
    private boolean tradeOnly = false;
    private boolean playSounds = false;

    private TrayIcon trayIcon;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PoEWhisperNotifierGUI window = new PoEWhisperNotifierGUI();
                PoeWhisperController controller = new PoeWhisperController(window);
                window.setPoEWhisperController(controller);
                window.frmPoewhispernotifier.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     */
    private PoEWhisperNotifierGUI() {

        // Get window decorations drawn by the look and feel.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        initialize();
        //stestTray();

    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    public void setTradeOnly(boolean tradeOnly) {
        this.tradeOnly = tradeOnly;
    }

    public void setPlaySounds(boolean playSounds) {
        this.playSounds = playSounds;
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        frmPoewhispernotifier = new JFrame();
        frmPoewhispernotifier.setTitle("PoE Whisper Notifier (beta)");
        frmPoewhispernotifier.setBounds(100, 100, 340, 300);
        frmPoewhispernotifier.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frmPoewhispernotifier.setAlwaysOnTop(alwaysOnTop);
        frmPoewhispernotifier.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                removeTrayIcon();
            }
        });
        // set Icon
        java.net.URL imageUrl = PoEWhisperNotifierGUI.class.getResource("/Icon.png");
        System.out.println(imageUrl);
        if (imageUrl != null) {
            frmPoewhispernotifier.setIconImage(new ImageIcon(imageUrl).getImage());
        }

        JMenuBar menuBar = new JMenuBar();
        frmPoewhispernotifier.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        mnNewMenu.setMnemonic('F');
        menuBar.add(mnNewMenu);

        JMenuItem mntmClose = new JMenuItem("Close");
        mntmClose.addActionListener(arg0 -> {
            frmPoewhispernotifier.dispose();
            System.exit(0);
        });

        JMenuItem mntmSetLogfile = new JMenuItem("Set Logfile");
        mntmSetLogfile.setIcon(new ImageIcon(
                PoEWhisperNotifierGUI.class.getResource("/com/sun/java/swing/plaf/windows/icons/TreeOpen.gif")));
        mnNewMenu.add(mntmSetLogfile);
        mntmSetLogfile.addActionListener(arg0 -> {
            File log = openLogSearch();

            controller.restartNotifier(log);
            System.out.println(log);

        });
        mnNewMenu.add(mntmClose);

        JMenu mnOptions = new JMenu("Options");
        mnOptions.setMnemonic('O');
        menuBar.add(mnOptions);

        JCheckBoxMenuItem chckbxmntmMuted = new JCheckBoxMenuItem("Play Sounds");
        chckbxmntmMuted.addActionListener(e -> {
            playSounds = !playSounds;
            controller.playSounds(playSounds);

        });
        mnOptions.add(chckbxmntmMuted);
        chckbxmntmMuted.setSelected(playSounds);

        JMenuItem mntmResetLog = new JMenuItem("Reset Log");
        mntmResetLog.setIcon(new ImageIcon(PoEWhisperNotifierGUI.class
                .getResource("/com/sun/deploy/uitoolkit/impl/fx/ui/resources/image/graybox_error.png")));
        mntmResetLog.addActionListener(arg0 -> pressResetButton());

        JCheckBoxMenuItem chckbxmntmAlwaysOnTop = new JCheckBoxMenuItem("Always on top");
        chckbxmntmAlwaysOnTop.setSelected(alwaysOnTop);
        chckbxmntmAlwaysOnTop.addActionListener(e -> {
            alwaysOnTop = !alwaysOnTop;
            controller.storeAlwaysOnTopProperty(alwaysOnTop);

            frmPoewhispernotifier.setAlwaysOnTop(alwaysOnTop);
        });

        JCheckBoxMenuItem chckbxmntmTradeOnly = new JCheckBoxMenuItem("Trade only");
        chckbxmntmTradeOnly.addActionListener(e -> {
            tradeOnly = !tradeOnly;
            controller.setTradeOnlyProperty(tradeOnly);
        });

        chckbxmntmTradeOnly.setSelected(tradeOnly);
        mnOptions.add(chckbxmntmTradeOnly);
        mnOptions.add(chckbxmntmAlwaysOnTop);

        JSeparator separator = new JSeparator();
        mnOptions.add(separator);
        mnOptions.add(mntmResetLog);
        frmPoewhispernotifier.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        frmPoewhispernotifier.getContentPane().add(panel, BorderLayout.SOUTH);

        JLabel lblLastUpdateTT = new JLabel("Last Status:");
        lblLastUpdateTT.setForeground(Color.GRAY);
        panel.add(lblLastUpdateTT);

        lblLastUpdate = new JLabel("Not running, select a log file first.");
        lblLastUpdate.setForeground(Color.GRAY);
        panel.add(lblLastUpdate);

        JScrollPane scrollPane = new JScrollPane();
        frmPoewhispernotifier.getContentPane().add(scrollPane, BorderLayout.CENTER);

        txtWhisper = new JTextPane();
        txtWhisper.setForeground(Color.BLACK);
        txtWhisper.setEditable(false);
        txtWhisper.setMargin(new Insets(3, 3, 3, 3));
        txtWhisper.setFont(new Font("Tahoma", Font.PLAIN, 13));
        scrollPane.setViewportView(txtWhisper);

        addTrayIcon();
    }

    private void addTrayIcon() {
        //If the icon is a file
        Image image = frmPoewhispernotifier.getToolkit().createImage("res/icon.png");
        System.out.println("Loading image... " + image.toString());
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();


            PopupMenu popup = new PopupMenu();

            MenuItem item = new MenuItem("Close");
            item.addActionListener(e -> {
                tray.remove(trayIcon);
                frmPoewhispernotifier.dispose();
                System.exit(0);
            });
            popup.add(item);

            trayIcon = new TrayIcon(image, "PoE Whispers", popup);
            //Let the system resizes the image if needed
            trayIcon.setImageAutoSize(true);
            //Set tooltip text for the tray icon
            trayIcon.setToolTip("PoE Whispers");

            trayIcon.addActionListener(event -> {
                frmPoewhispernotifier.toFront();
            });
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeTrayIcon() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }
    }

    private void pressResetButton() {
        int response = JOptionPane.showConfirmDialog(frmPoewhispernotifier,
                "Are you sure you want to clear the log contents?", "Reset Log", JOptionPane.OK_CANCEL_OPTION);
        if (response == JOptionPane.OK_OPTION) {
            System.out.println("Wipe Logs? " + controller.wipeLogs());
        }
    }

    public File openLogSearch() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Please select the client.log in /PoE/logs.");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Log file", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(frmPoewhispernotifier);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File clientLog = chooser.getSelectedFile();
            System.out.println("You chose to open this file: " + clientLog.getName() + " path=" + clientLog.getPath());

            controller.storeLogLocationProperty(clientLog.getPath());
            return clientLog;
        }
        return null;
    }


    @Override
    public void receiveNotification(List<String> whispers) {
        String text = "";
        System.out.println("settext=" + whispers);
        for (String s : whispers) {
            text += s + "\r\n";
        }

        txtWhisper.setText(text);
        txtWhisper.setCaretPosition(txtWhisper.getDocument().getLength());

        if (playSounds)
            Toolkit.getDefaultToolkit().beep();

        trayIcon.displayMessage("PoE Whisper Notfier", text, TrayIcon.MessageType.INFO);

    }

    @Override
    public void receiveStatus(String status, ColorStatus colorStatus) {
        if (lblLastUpdate != null) {
            lblLastUpdate.setText(status);
            lblLastUpdate.setForeground(ColorStatus.getColor(colorStatus));
        }
    }

    public void setPoEWhisperController(PoeWhisperController poEWhisperController) {
        this.controller = poEWhisperController;
    }
}
