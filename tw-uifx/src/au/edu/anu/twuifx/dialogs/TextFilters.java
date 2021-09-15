package au.edu.anu.twuifx.dialogs;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;

/**
 * @author Ian Davies
 *
 * @date 15 Sept 2021
 * 
 * https://gist.github.com/karimsqualli96/f8d4c2995da8e11496ed
 */
public class TextFilters {
	private TextFilters() {
	};

	public static UnaryOperator<TextFormatter.Change> getDoubleFilter() {
		UnaryOperator<TextFormatter.Change> result = new UnaryOperator<TextFormatter.Change>() {

			@Override
			public TextFormatter.Change apply(TextFormatter.Change t) {

				if (t.isReplaced())
					if (t.getText().matches("[^0-9]"))
						t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));

				if (t.isAdded()) {
					if (t.getControlText().contains(".")) {
						if (t.getText().matches("[^0-9]")) {
							t.setText("");
						}
					} else if (t.getText().matches("[^0-9.]")) {
						t.setText("");
					}
				}

				return t;
			}
		};
		return result;

	}

}
