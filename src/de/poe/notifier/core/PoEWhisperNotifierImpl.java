package de.poe.notifier.core;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.poe.notifier.util.Utility;

public class PoEWhisperNotifierImpl implements WhisperNotifier {
	private File clientLog;
	private String lastLine = "";
	private List<String> whispers;
	private List<WhisperSubscriber> subscribers = new ArrayList<>();
	private boolean playSounds = true;
	private boolean tradeOnly=true;
	// Check every second.
	public final int TIMER_PERIOD = 1;

	// The timer for the executors
	private final ScheduledExecutorService timerExecutor = Executors.newSingleThreadScheduledExecutor();
	private String status = "";
	

	/**
	 * Starts the Timer with a period that is given from the Runnable we use.
	 */
	public void start() {

		if (clientLog != null)
			timerExecutor.scheduleAtFixedRate(new WhisperRunnable(), 0, TIMER_PERIOD, TimeUnit.SECONDS);
		else{
			status = "Client log not found!";
			updateStatus(ColorStatus.ERROR);
		}

	}

	/**
	 * Creates a new Subscription based notifier.
	 * 
	 * @param clientLog
	 */
	public PoEWhisperNotifierImpl(File clientLog) {
		this.clientLog = clientLog;
		whispers = new ArrayList<String>();
		if (this.clientLog == null)
			return;
		lastLine = Utility.getLastLineFast(clientLog);
	}

	/**
	 * Check whether the message is a trade Message.
	 * 
	 * @param line
	 * @return
	 */
	public boolean isBuyer(String line) {
//		System.out.println("check line: " + line);
		if(!tradeOnly)
			return true;
		if (line.contains("wtb") || line.contains("buy") || line.contains("sell") || line.contains("interested")
				|| line.contains("much") || line.contains("price") || line.contains("listed")) {
			return true;
		}
		return false;
	}

	/**
	 * Wipes the currently specified log file.
	 * 
	 * @return
	 */
	public boolean wipeLogs() {
		try {
			BufferedWriter out = new BufferedWriter(new PrintWriter(clientLog));
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

	/**
	 * This runnable is run within a timer to update the last message, whisperes
	 * received and the status of the program.
	 * 
	 * @author Fabian Widmann
	 *
	 */
	public class WhisperRunnable implements Runnable {
		@Override
		public void run() {
			try {
				String line = Utility.getLastLineFast(clientLog);

				if (!Utility.returnWindowTitle(100).contains("Path of Exile")) {
					status = "PoE in Background, running!";
					updateStatus(ColorStatus.SUCCESS);
					if (!line.equals(lastLine) && isBuyer(line) && line.contains("@")) {
						lastLine = line;
						String msg = line.split("@")[1];
						prepareMessage(msg);
						System.out.println(">> " + msg);
						notifySubscribers(whispers);
						if (playSounds)
							Toolkit.getDefaultToolkit().beep();
					}
				} else {
					lastLine = Utility.getLastLineFast(clientLog);
					status = "PoE in Foreground, do not notify!";
					updateStatus(ColorStatus.DEFAULT);
				}

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
		lastLine = Utility.getLastLineFast(clientLog);
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

	/**
	 * Set the boolean to either mute the notifications or play a sound.
	 * 
	 * @param playSounds
	 */
	public void setPlaySounds(boolean playSounds) {
		this.playSounds = playSounds;
	}

	public void setTradeOnly(boolean tradeOnly) {
		this.tradeOnly=tradeOnly;
	}

	// public static void main(String[] args) {
	// String filePath = "";
	// if (args.length > 0) {
	// filePath = args[0];
	// System.out.println("Loading File: '" + filePath + "'");
	// }
	// File clientLog = new File(filePath);
	// PoEWhisperNotifierImpl notifier = new PoEWhisperNotifierImpl(clientLog);
	// notifier.start();
	// }
}
