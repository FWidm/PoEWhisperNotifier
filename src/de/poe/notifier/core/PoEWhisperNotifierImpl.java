package de.poe.notifier.core;

import de.poe.notifier.core.interfaces.WhisperNotifier;
import de.poe.notifier.core.interfaces.WhisperSubscriber;
import de.poe.notifier.util.Utility;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PoEWhisperNotifierImpl implements WhisperNotifier {
    private File clientLog;
    private String lastLine = "";
    private List<String> whispers;
    private List<WhisperSubscriber> subscribers = new ArrayList<>();
    private boolean tradeOnly = true;

    // The timer for the executors
    private final ScheduledExecutorService timerExecutor = Executors.newSingleThreadScheduledExecutor();
    private String status = "";
    ScheduledFuture<?> scheduledFuture;

    /**
     * Starts the runnable inside a timerExecutor.
     *
     * @param pollRateInSeconds how often do you want to poll - 1 = every 1s
     */
    public void start(int pollRateInSeconds) {
        if (clientLog != null) {
            scheduledFuture = timerExecutor.scheduleAtFixedRate(new WhisperRunnable(), 0, pollRateInSeconds, TimeUnit.SECONDS);
            System.out.println("started future=" + scheduledFuture);

        } else {
            status = "Client log not found!";
            updateStatus(ColorStatus.ERROR);
        }

    }

    /**
     * Creates a new Subscription based notifier.
     *
     * @param clientLog
     * @param tradeOnly
     */
    public PoEWhisperNotifierImpl(File clientLog, boolean tradeOnly) {
        this.clientLog = clientLog;
        whispers = new ArrayList<String>();
        if (this.clientLog == null)
            return;
        lastLine = Utility.getLastLineFast(clientLog);
        this.tradeOnly = tradeOnly;
    }

    /**
     * Check whether the message is a trade Message.
     *
     * @param line
     * @return
     */
    public boolean isTradeMessage(String line) {
//		System.out.println("check line: " + line);
        if (!tradeOnly)
            return true;
        if (line.contains("wtb") || line.contains("buy") || line.contains("sell") || line.contains("interested")
                || line.contains("much") || line.contains("price") || line.contains("listed")) {
            return true;
        }
        return false;
    }


    /**
     * Cancel the existing task if it exists and is not cancelled or finished already.
     */
    public void cancel() {
        System.out.println("trying to cancel future=" + scheduledFuture);
        if (scheduledFuture == null)
            return;

        if (!scheduledFuture.isCancelled() || !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
            System.out.println("cancelled future=" + scheduledFuture);

        }

    }

    /**
     * This runnable is run within a timer to update the last message, whisperes
     * received and the status of the program.
     *
     * @author Fabian Widmann
     */
    public class WhisperRunnable implements Runnable {
        @Override
        public void run() {
            try {
                String line = Utility.getLastLineFast(clientLog);
                if (Utility.isProcessRunning("PathOfExile")) {
                    if (!Utility.returnWindowTitle(100).contains("Path of Exile")) {
                        status = "PoE in Background, running!";
                        updateStatus(ColorStatus.SUCCESS);
                        if (!line.equals(lastLine) && isTradeMessage(line) && line.contains("@") && line.toUpperCase().contains("FROM")) {
                            lastLine = line;
                            String msg = line.split("@")[1];
                            prepareMessage(msg);
                            System.out.println(">> " + msg);
                            notifySubscribers(whispers);
                        }
                    } else {
                        lastLine = Utility.getLastLineFast(clientLog);
                        status = "PoE in Foreground, do not notify!";
                        updateStatus(ColorStatus.DEFAULT);
                    }
                } else {
                    status = "PoE not running";
                    updateStatus(ColorStatus.ERROR);
                }
                System.out.println("runnable=" + String.valueOf(this));

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * Set the logfile
     *
     * @param clientLog
     */
    public void setLog(File clientLog) {
        //lastLine = Utility.getLastLineFast(clientLog);
        this.clientLog = clientLog;
    }

    /**
     * Add a timestamp and the @ to the splitted message.
     *
     * @param msg
     */
    public void prepareMessage(String msg) {
        // 00:00:00 - 23:59:59
        DateFormat dfmt = new SimpleDateFormat("HH:mm:ss");
        whispers.add(dfmt.format(new Date()) + " | @" + msg);
        status = "Last Update @ " + dfmt.format(new Date());
        updateStatus(ColorStatus.SUCCESS);
    }

    public List<String> getWhispers() {
        return whispers;
    }

    @Override
    public void subscribe(WhisperSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(WhisperSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Send all the whispers to all subscribers.
     *
     * @param whispers
     */
    private void notifySubscribers(List<String> whispers) {
        for (WhisperSubscriber sub : subscribers) {
            sub.receiveNotification(whispers);
        }
    }

    /**
     * Send the current status to all subscribers.
     */
    private void updateStatus(ColorStatus colorStatus) {
        for (WhisperSubscriber sub : subscribers) {
            sub.receiveStatus(status, colorStatus);
        }
    }


    public void setTradeOnly(boolean tradeOnly) {
        this.tradeOnly = tradeOnly;
    }

}
