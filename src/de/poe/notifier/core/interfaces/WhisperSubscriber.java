package de.poe.notifier.core.interfaces;

import de.poe.notifier.core.ColorStatus;

import java.util.List;

public interface WhisperSubscriber {
	/**
	 * Receive whispers
	 * @param whispers
	 */
	public void receiveNotification(List<String> whispers);

	/**
	 * Indicates whether the game is running in bg or fg and add color information
	 * @param status
	 * @param colorStatus
	 */
	public void receiveStatus(String status,ColorStatus colorStatus);
}
