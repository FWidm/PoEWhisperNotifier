package de.poe.notifier.core.interfaces;

public interface WhisperNotifier {
	public void subscribe(WhisperSubscriber subscriber);
	public void unsubscribe(WhisperSubscriber subscriber);
}
