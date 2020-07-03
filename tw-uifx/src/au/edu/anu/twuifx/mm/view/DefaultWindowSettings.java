package au.edu.anu.twuifx.mm.view;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class DefaultWindowSettings {
	private static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
	private static double w = 1220;
	private static double h =840;
	private static double x = (screenBounds.getWidth() - w) / 2;
	private static double y = (screenBounds.getHeight() - h) / 3;
	private static double splitter1 = 0.3;
	private static double splitter2 = 0.5;
	private static String defaultMMName = "3Worlds Model Maker";
	public static double getWidth() {
		return w;
	}
	public static double getHeight() {
		return h;
	}
	public static double getX() {
		return x;
	}
	public static double getY() {
		return y;
	}
	public static double splitter1() {
		return splitter1;
	}
	public static double splitter2() {
		return splitter2;
	}
	public static String defaultName() {
		return defaultMMName;
	}
	


}
