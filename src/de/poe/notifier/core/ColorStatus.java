package de.poe.notifier.core;

import java.awt.Color;

public enum ColorStatus {
	DEFAULT, ERROR, SUCCESS;

	public static Color getColor(ColorStatus status) {
		switch (status) {
		case ERROR:
			return new Color(170,20,20);
		case SUCCESS:
			return new Color(70,170,20);
		default:
			return Color.BLACK;
		}
	}

}
