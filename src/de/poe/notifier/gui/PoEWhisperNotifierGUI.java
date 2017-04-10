package de.poe.notifier.gui;

import de.poe.notifier.core.ColorStatus;
import de.poe.notifier.core.PoEWhisperNotifierImpl;
import de.poe.notifier.core.WhisperSubscriber;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

public class PoEWhisperNotifierGUI implements WhisperSubscriber {
    private final String CLIENT_LOG_KEY = "Client Log Location";
    private final String MUTED_KEY = "Muted";
    private final String TRADE_ONLY_KEY = "Trade Only";
    private final String STAY_ON_TOP_KEY = "Stay on Top";

    private JFrame frmPoewhispernotifier;

    private PoEWhisperNotifierImpl notifier;
    private Properties properties;
    private JTextPane txtWhisper;
    private JLabel lblLastUpdate;

    private boolean alwaysOnTop = false;
    private boolean tradeOnly = true;
    private boolean playSounds = true;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PoEWhisperNotifierGUI window = new PoEWhisperNotifierGUI();
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

        properties = new Properties();
        try {
            properties.load(new FileInputStream("poe.notifier.properties"));
        } catch (IOException ignored) {
        }
        String filename = (String) properties.get(CLIENT_LOG_KEY);
        File clientLog;
        if (filename != null)
            clientLog = new File(filename);
        else {
            clientLog = openLogSearch();
        }
        alwaysOnTop = Boolean.valueOf(properties.getProperty(STAY_ON_TOP_KEY));
        System.out.println("TOP=" + properties.getProperty(STAY_ON_TOP_KEY) + " - "
                + Boolean.valueOf(properties.getProperty(STAY_ON_TOP_KEY)));
        playSounds = Boolean.valueOf(properties.getProperty(MUTED_KEY));
        System.out.println("MUTE=" + properties.getProperty(MUTED_KEY) + " - "
                + Boolean.valueOf(properties.getProperty(MUTED_KEY)));
        tradeOnly = Boolean.valueOf(properties.getProperty(TRADE_ONLY_KEY));
        System.out.println("TRADE=" + properties.getProperty(TRADE_ONLY_KEY) + " - "
                + Boolean.valueOf(properties.getProperty(TRADE_ONLY_KEY)));
        notifier = new PoEWhisperNotifierImpl(clientLog,tradeOnly);
        notifier.subscribe(this);
        System.out.println("constructor: " + filename);
        notifier.start();

        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        // Get window decorations drawn by the look and feel.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        frmPoewhispernotifier = new JFrame();
        frmPoewhispernotifier.setTitle("PoE Whisper Notifier (beta)");
        frmPoewhispernotifier.setBounds(100, 100, 340, 300);
        frmPoewhispernotifier.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frmPoewhispernotifier.setAlwaysOnTop(alwaysOnTop);
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
            notifier.setLog(openLogSearch());
            notifier.start();
        });
        mnNewMenu.add(mntmClose);

        JMenu mnOptions = new JMenu("Options");
        mnOptions.setMnemonic('O');
        menuBar.add(mnOptions);

        JCheckBoxMenuItem chckbxmntmMuted = new JCheckBoxMenuItem("Play Sounds");
        chckbxmntmMuted.addActionListener(e -> {
            playSounds = !playSounds;
            properties.setProperty(MUTED_KEY, "" + playSounds);
            System.out.println("Saved mute=" + properties.getProperty(MUTED_KEY) + " - "
                    + Boolean.valueOf(properties.getProperty(MUTED_KEY)));
            storeProperties();

            System.out.println(playSounds);
            notifier.setPlaySounds(playSounds);
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
            properties.setProperty(STAY_ON_TOP_KEY, "" + playSounds);
            System.out.println("Saved top=" + properties.getProperty(STAY_ON_TOP_KEY) + " - "
                    + Boolean.valueOf(properties.getProperty(STAY_ON_TOP_KEY)));
            System.out.println();
            storeProperties();

            frmPoewhispernotifier.setAlwaysOnTop(alwaysOnTop);
        });

        JCheckBoxMenuItem chckbxmntmTradeOnly = new JCheckBoxMenuItem("Trade only");
        chckbxmntmTradeOnly.addActionListener(e -> {
            tradeOnly = !tradeOnly;
            properties.setProperty(TRADE_ONLY_KEY, "" + tradeOnly);
            System.out.println("Saved trade=" + properties.getProperty(TRADE_ONLY_KEY) + " - "
                    + Boolean.valueOf(properties.getProperty(TRADE_ONLY_KEY)));
            storeProperties();

            notifier.setTradeOnly(tradeOnly);
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

        lblLastUpdate = new JLabel("<UpdateWithError>");
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
    }

    private void pressResetButton() {
        int response = JOptionPane.showConfirmDialog(frmPoewhispernotifier,
                "Are you sure you want to clear the log contents?", "Reset Log", JOptionPane.OK_CANCEL_OPTION);
        if (response == JOptionPane.OK_OPTION) {
            System.out.println("Wipe Logs? " + notifier.wipeLogs());
        }
    }

    private File openLogSearch() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Log file", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(frmPoewhispernotifier);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File clientLog = chooser.getSelectedFile();
            System.out.println("You chose to open this file: " + clientLog.getName() + " path=" + clientLog.getPath());
            properties.setProperty(CLIENT_LOG_KEY, clientLog.getPath());
            storeProperties();
            return clientLog;
        }
        return null;
    }

    private void storeProperties() {
        File file = new File("poe.notifier.properties");
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(file);
            properties.store(fileOut, "PoE Whisper Notifier Properties");
            System.out.println(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        try {
            displayTray("PoE Whisper Notfier",text);
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveStatus(String status, ColorStatus colorStatus) {
        if (lblLastUpdate != null) {
            lblLastUpdate.setText(status);
            lblLastUpdate.setForeground(ColorStatus.getColor(colorStatus));
        }
    }

    public void displayTray(String title, String message) throws AWTException, java.net.MalformedURLException {
        //Obtain only one instance of the SystemTray object

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            //If the icon is a file
            Image image = frmPoewhispernotifier.getToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "PoE Whispers");
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    frmPoewhispernotifier.toFront();
                    System.out.println("HAI");

                    if (e.getButton() == MouseEvent.BUTTON1) {
                    }
                }
            });
            //Let the system resizes the image if needed
            trayIcon.setImageAutoSize(true);
            //Set tooltip text for the tray icon
            trayIcon.setToolTip("PoE Whispers");

            tray.add(trayIcon);
            System.out.println("listeners=" + trayIcon.getMouseListeners().length);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

        }
    }
}
