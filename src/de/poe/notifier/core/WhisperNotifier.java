package de.poe.notifier.core;

public interface WhisperNotifier {
	public void subscribe(WhisperSubscriber subscriber);
	public void unsubscribe(WhisperSubscriber subscriber);
}
