package fr.labri.shelly;

import fr.labri.shelly.impl.PeekIterator;


public interface Converter<T> {
	T convert(String cmd, PeekIterator<String> cmdLine);
	public abstract Class<?> convertedType();
}
