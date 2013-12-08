package fr.labri.shelly.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;

import fr.labri.shelly.Converter;

public class ConverterFactory implements fr.labri.shelly.ConverterFactory {
	public Converter<?> getConverter(Class<?> type, Object context) {
		Converter<?> converter = null;
		if (type.isArray())
			converter = getArrayConverter(type, context);
		else if (type.isPrimitive())
			converter = getPrimitiveConverter(type);
		else
			converter = getObjectConverter(type);

		if (converter == null)
			throw new RuntimeException(String.format("In %s: No converter for type %s", context, type.toString()));
		return converter;
	}

	public Converter<?> getArrayConverter(Class<?> type, Object context) {
		System.out.println("Array " + type+"of => " + type.getComponentType());
		return new ArrayConverter(getConverter(type.getComponentType(), context));
	}

	public Converter<?> getPrimitiveConverter(Class<?> type) {
		if (type.isAssignableFrom(int.class))
			return INT_CONVERTER;
		return null;
	}

	public Converter<?> getObjectConverter(Class<?> type) {
		if (type.isAssignableFrom(String.class))
			return STR_CONVERTER;
		if (type.isAssignableFrom(Integer.class))
			return INT_CONVERTER;
		return null;
	}

	static Converter<String> STR_CONVERTER = new SimpleConverter<String>() {
		public String convert(String cmd) {
			return cmd;
		}

		@Override
		public Class<String> convertedType() {
			return String.class;
		}
	};

	static Converter<Integer> INT_CONVERTER = new SimpleConverter<Integer>() {
		public Integer convert(String cmd) {
			return Integer.parseInt(cmd);
		}
		@Override
		public Class<Integer> convertedType() {
			return Integer.class;
		}
	};

	static class ArrayConverter implements Converter<Object> {
		final Converter<? extends Object> _converter;
		
		public ArrayConverter(Converter<?> converter) {
			_converter = converter;
		}

		final public Object convert(String cmd, PeekIterator<String> cmdLine) {
			ArrayList<Object> lst = new ArrayList<>();
			while(cmdLine.hasNext())
				lst.add(_converter.convert(cmd, cmdLine));
			return lst.toArray((Object[])Array.newInstance(_converter.convertedType(), lst.size()));
		}

		@Override
		public Class<?> convertedType() {
			return Array.class;
		}
	};

	public static abstract class SimpleConverter<T> implements Converter<T> {
		final public T convert(String cmd, PeekIterator<String> cmdLine) {
			return convert(cmdLine.next());
		}

		@Override
		public Class<?> convertedType() {
			return Object.class;
		}

		public abstract T convert(String value);
	}

	public static Object[] convertArray(Converter<?>[] converters, String cmd, PeekIterator<String> cmdLine) {
		Object[] args = new Object[converters.length];
		for (int i = 0; i < converters.length; i++)
			args[i] = converters[i].convert(cmd, cmdLine);
		return args;
	}
}
