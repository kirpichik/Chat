package org.polushin.chat.client;

import javax.swing.text.DefaultFormatter;
import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * Валидатор формата ввода использующий регулярные выражения.
 */
public class RegexFormatter extends DefaultFormatter {

	private final Pattern pattern;

	public RegexFormatter(Pattern pattern) {
		this.pattern = pattern;
	}

	public Object stringToValue(String text) throws ParseException {
		if (pattern != null) {
			if (pattern.matcher(text).matches())
				return super.stringToValue(text);
			throw new ParseException("Pattern did not match", 0);
		}
		return text;
	}
}

