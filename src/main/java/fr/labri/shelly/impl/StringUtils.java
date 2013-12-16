package fr.labri.shelly.impl;

public class StringUtils {

	static public int startWith(String str, String prefix, int offset) {
		return str.startsWith(prefix, offset) ? prefix.length() + offset : -1;
	}

	static public boolean endsWith(String str, String suffix, int offset) {
		return (offset + suffix.length() == str.length()) ? str.endsWith(suffix) : false;
	}

}
