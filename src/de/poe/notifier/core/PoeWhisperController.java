package de.poe.notifier.core;

import de.poe.notifier.gui.PoEWhisperNotifierGUI;

import java.io.*;
import java.util.Properties;

/**
 * @author Fabian Widmann on 11.04.2017.
 */
public class PoeWhisperController {
    private Properties properties;
    private PoEWhisperNotifierGUI gui;
    private PoEWhisperNotifierImpl whisperNotifier;

    private final String CLIENT_LOG_KEY = "Client Log Location";
    private final String MUTED_KEY = "Muted";
    private final String TRADE_ONLY_KEY = "Trade Only";
    private final String STAY_ON_TOP_KEY = "Stay on Top";

    private static final String PROPERTIES_FILE_NAME = "poe.notifier.properties";

    public PoeWhisperController(PoEWhisperNotifierGUI gui) {
        this.gui = gui;

        properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPERTIES_FILE_NAME));
        } catch (IOException ignored) {
        }
        String filename = (String) properties.get(CLIENT_LOG_KEY);
        File clientLog;
        if (filename != null)
            clientLog = new File(filename);
        else {
            clientLog = gui.openLogSearch();
        }

        gui.setTradeOnly(Boolean.valueOf(properties.getProperty(STAY_ON_TOP_KEY)));
        gui.setPlaySounds(Boolean.valueOf(properties.getProperty(MUTED_KEY)));
        gui.setTradeOnly(Boolean.valueOf(properties.getProperty(TRADE_ONLY_KEY)));


        whisperNotifier = new PoEWhisperNotifierImpl(clientLog, Boolean.valueOf(properties.getProperty(TRADE_ONLY_KEY)));
        whisperNotifier.subscribe(gui);
        System.out.println("PoEWhisperNotifierGUI constructor: " + filename);
        whisperNotifier.start(2);
    }


    public void restartNotifier(File log) {
        whisperNotifier.setLog(log);

        if (log != null) {
            whisperNotifier.cancel();
            whisperNotifier.start(2);
        }
    }

    public void playSounds(boolean playSounds) {
        properties.setProperty(MUTED_KEY, "" + playSounds);
        System.out.println("Saved mute=" + properties.getProperty(MUTED_KEY) + " - "
                + Boolean.valueOf(properties.getProperty(MUTED_KEY)));
        storeProperties();

        System.out.println(playSounds);
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

    public void storeLogLocationProperty(String logLocation) {
        properties.setProperty(CLIENT_LOG_KEY, logLocation);
        storeProperties();
    }

    /**
     * Wipes the currently specified log file.
     *
     * @return true if success or false if an error occured.
     */
    public boolean wipeLogs() {
        try {
            BufferedWriter out = new BufferedWriter(new PrintWriter((String) properties.get(CLIENT_LOG_KEY)));
            out.write("");
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void storeAlwaysOnTopProperty(boolean alwaysOnTop) {
        properties.setProperty(STAY_ON_TOP_KEY, "" + alwaysOnTop);
        System.out.println("Saved top=" + properties.getProperty(STAY_ON_TOP_KEY) + " - "
                + Boolean.valueOf(properties.getProperty(STAY_ON_TOP_KEY)));
        System.out.println();
        storeProperties();
    }

    public void setTradeOnlyProperty(boolean tradeOnly) {
        properties.setProperty(TRADE_ONLY_KEY, "" + tradeOnly);
        System.out.println("Saved trade=" + properties.getProperty(TRADE_ONLY_KEY) + " - "
                + Boolean.valueOf(properties.getProperty(TRADE_ONLY_KEY)));
        storeProperties();

        whisperNotifier.setTradeOnly(tradeOnly);
    }
}
