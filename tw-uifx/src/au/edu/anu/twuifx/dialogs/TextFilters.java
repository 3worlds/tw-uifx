package au.edu.anu.twuifx.dialogs;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

/**
 * @author Ian Davies
 *
 * @date 15 Sept 2021
 * 
 *       https://stackoverflow.com/questions/45977390/how-to-force-a-double-input-in-a-textfield-in-javafx
 */
public class TextFilters {
	private static Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");

	private TextFilters() {
	};

	public static TextFormatter<Double> getDoubleFormatter() {
		return new TextFormatter<>(converter, 0.0, getDoubleFilter());
	}

	private static UnaryOperator<TextFormatter.Change> getDoubleFilter() {
		UnaryOperator<TextFormatter.Change> result = c -> {
			String text = c.getControlNewText();
			if (validEditingState.matcher(text).matches()) {
				return c;
			} else {
				return null;
			}
		};
		return result;

	}

	private static StringConverter<Double> converter = new StringConverter<Double>() {

		@Override
		public Double fromString(String s) {
			if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
				return 0.0;
			} else {
				return Double.valueOf(s);
			}
		}

		@Override
		public String toString(Double d) {
			return d.toString();
		}
	};
}
