package fr.labri.shelly.impl;

import java.util.Arrays;
import java.util.Iterator;

public class Tokenizer {

	static Iterable<String> getTokenizer(final String[] args) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return Arrays.asList(args).iterator();
			}
		};
	}
	
}
