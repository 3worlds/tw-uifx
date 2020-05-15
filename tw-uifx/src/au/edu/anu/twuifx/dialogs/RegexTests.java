package au.edu.anu.twuifx.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTests {
	
	private static final String email = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile(email);
		List<String> names = new ArrayList<String>();
		names.add("IAN.DAVI_ES%@ANU.EDU.AU");
		names.add("Ian.%Davies%@anu.edu.au");

		for (String name : names) {
			Matcher matcher = pattern.matcher(name);
			System.out.println((names.indexOf(name) + 1) + ") " + matcher.matches());
		}


	}

}
