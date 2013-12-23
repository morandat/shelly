package fr.labri.shelly;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import fr.labri.shelly.impl.PeekIterator;

public interface Converter<T> {
	public abstract T convert(Executor executor);

	public abstract Class<? super T> convertedType();
	
	public class MapEntryConverter implements Converter<Map.Entry<String, String>> {
		String getMapSeparator() {
			return "=";
		}
		@Override
		public Entry<String, String> convert(Executor executor) {
			String text = executor.getCommandLine().next();
			int idx = text.indexOf(getMapSeparator());
			if (idx == -1)
				throw new ShellyException("Parse error, key"+getMapSeparator()+"val expected");
			String key = text.substring(0, idx);
			return new AbstractMap.SimpleImmutableEntry<>(key, text.substring(idx+1));
		}

		@Override
		public Class<? super Entry<String, String>> convertedType() {
			return Map.Entry.class;
		}
	}

	public class ArrayConverter<E> implements Converter<E[]> {
		final Converter<? extends E> _converter;
		
		public ArrayConverter(Converter<E> converter) {
			_converter = converter;
		}

		@SuppressWarnings("unchecked")
		final public E[] convert(Executor executor) {
			ArrayList<E> lst = new ArrayList<>();
			PeekIterator<String> line = executor.getCommandLine();
			while (line.hasNext())
				lst.add(_converter.convert(executor));
			return lst.toArray((E[]) Array.newInstance(_converter.convertedType(), lst.size()));
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<? super E[]> convertedType() {
			return (Class<? super E[]>) Array.newInstance(_converter.convertedType(), 0).getClass();
		}
	};
	
	public abstract class SimpleConverter<T> implements Converter<T> {
		final public T convert(Executor executor) {
			try {
				return convert(executor.getCommandLine().next());
			} catch (NumberFormatException e) {
				throw new ShellyException(String.format("Bad format arguments, expected '%s'", convertedType()));
			} catch (NoSuchElementException e) {
				throw new ShellyException(String.format("Not enought arguments, expected '%s'", convertedType()));
			}
		}

		@Override
		public Class<? super T> convertedType() {
			return Object.class;
		}
		public abstract T convert(String value);
	}
}
