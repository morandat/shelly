package fr.labri.shelly.impl;

import fr.labri.shelly.Converter;

public class ConverterFactory implements fr.labri.shelly.ConverterFactory {
	public Converter<?> getConverter(Class<?> type, Object context) {
		Converter<?> converter = null;
		if(type.isArray())
			converter = getArrayConverter(type);
		else if(type.isPrimitive())
			converter = getPrimitiveConverter(type);
		else
			converter = getObjectConverter(type);
		
		if(converter == null)
			throw new RuntimeException(String.format("In %s: No converter for type %s", context, type.toString()));
		return converter;
	}

	public Converter<?> getArrayConverter(Class<?> type) {
		return null; // TODO
	}

	public Converter<?> getPrimitiveConverter(Class<?> type) {
		 if(type.isAssignableFrom(String.class))
				return INT_CONVERTER;
		 return null;
	}
	
	public Converter<?> getObjectConverter(Class<?> type) {
		 if(type.isAssignableFrom(String.class))
				return STR_CONVERTER;
		return null;
	}
	
	static Converter<String> STR_CONVERTER = new SimpleConverter<String>() {
		public String convert(String cmd) {
			return cmd;
		}
	};
	static Converter<Integer> INT_CONVERTER = new SimpleConverter<Integer>() {
		public Integer convert(String cmd) {
			return Integer.parseInt(cmd);
		}
	};
	
	public static abstract class SimpleConverter<T> implements Converter<T>  {
		final public T convert(String cmd, PeekIterator cmdLine) {
			return convert(cmdLine.next());
		}
		public abstract T convert(String value);
	}

	public static Object[] convertArray(Converter<?>[] converters, String cmd, PeekIterator cmdLine) {
		Object[] args = new Object[converters.length];
		for(int i = 0; i < converters.length; i ++)
			args[i] = converters[i].convert(cmd, cmdLine);
		return args;
	}
}
