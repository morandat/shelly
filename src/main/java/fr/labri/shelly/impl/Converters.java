package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.labri.shelly.Converter;
import fr.labri.shelly.annotations.Param;

public class Converters {

	final static public fr.labri.shelly.ConverterFactory DEFAULT = new fr.labri.shelly.impl.Converters.BasicConverter();
	
	public static class CompositeFactory implements fr.labri.shelly.ConverterFactory {
		final fr.labri.shelly.ConverterFactory[] _factories;
		final fr.labri.shelly.ConverterFactory _parent;
		CompositeFactory(fr.labri.shelly.ConverterFactory[] factories, fr.labri.shelly.ConverterFactory parent) {
			_factories = factories;
			_parent = parent;
		}
		@Override
		public Converter<?> getConverter(Class<?> type, boolean isOption ,Object context) {
			for(fr.labri.shelly.ConverterFactory factory : _factories) {
				Converter<?> converter = factory.getConverter(type, isOption, context);
				if(converter != null) {
					return converter;
				}
			}
			if(_parent != null)
				return _parent.getConverter(type, isOption, context);
			return null;
		}
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
	
	static Converter<Integer> PINT_CONVERTER = new SimpleConverter<Integer>() {
		public Integer convert(String cmd) {
			return Integer.parseInt(cmd);
		}

		@Override
		public Class<Integer> convertedType() {
			return int.class;
		}
	};
	
	public static final Converter<?>[] DEFAULTS = new Converter<?>[] {
		INT_CONVERTER,
		STR_CONVERTER
	};
	public static final Converter<?>[] DEFAULTS_PRIM = new Converter<?>[] {
		PINT_CONVERTER,
	};
	
	public static class BasicConverter implements fr.labri.shelly.ConverterFactory {
		
		public Converter<?> getConverter(Class<?> type, boolean isOption, Object context) {
			Converter<?> converter = null;
			if (type.isArray())
				converter = getArrayConverter(type, isOption, context);
			else if (type.isPrimitive())
				converter = getPrimitiveConverter(type, isOption);
			else
				converter = getObjectConverter(type, isOption);

			if (converter == null)
				throw new RuntimeException(String.format("In %s: No converter for type %s", context, type.toString()));
			return converter;
		}
		public Converter<?> getArrayConverter(Class<?> type, boolean isOption, Object context) {
			return new ArrayConverter(getConverter(type.getComponentType(), isOption, context));
		}
		
		public Converter<?> getPrimitiveConverter(Class<?> type, boolean isOption) {
			for(Converter<?> c: DEFAULTS_PRIM)
				if(type.isAssignableFrom(c.convertedType()))
					return c;
			return null;
		}
		public Converter<?> getObjectConverter(Class<?> type, boolean isOption) {
			for(Converter<?> c: DEFAULTS)
				if(type.isAssignableFrom(c.convertedType()))
					return c;
			return null;
		}
	}

	static class ArrayConverter implements Converter<Object[]> {
		final Converter<?> _converter;

		public ArrayConverter(Converter<?> converter) {
			_converter = converter;
		}

		final public Object[] convert(String cmd, PeekIterator<String> cmdLine) {
			ArrayList<Object> lst = new ArrayList<>();
			while (cmdLine.hasNext())
				lst.add(_converter.convert(cmd, cmdLine));
			return lst.toArray((Object[]) Array.newInstance(_converter.convertedType(), lst.size()));
		}

		@Override
		public Class<Array> convertedType() {
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

	static abstract class Cache<E> {
		Map<Class<E>, E> _objects = new HashMap<Class<E>, E>();

		@SuppressWarnings("unchecked")
		public E newFactory(Class<? extends E> clazz) {
			if(_objects.containsKey(clazz))
				return _objects.get(clazz);
			try {
				E o = newItem((Class<E>) clazz);
				_objects.put( (Class<E>) clazz, o);
				return o; 
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(String.format("Cannot instantiate factory: %s", clazz));
			}
		}
		abstract E newItem(Class<E> clazz) throws InstantiationException, IllegalAccessException; // clazz.newInstance()
	}
	
	public static Converter<?>[] getConverters(final fr.labri.shelly.ConverterFactory factory, Class<?> param) {
		return new Converter[] { new fr.labri.shelly.impl.Converters() {
			public Converter<?> getConverter(Class<?> type, Object context) {
				Converter<?> converter = factory.getConverter(type, false, context);
				return new ArrayConverter(converter);
			}
		}.getConverter(param, param) };
	}

	static Converter<?>[] getConverters(fr.labri.shelly.ConverterFactory factory, Class<?>[] params) {
		int i = 0;
		Converter<?>[] converters = new Converter<?>[params.length];
		for (Class<?> a : params)
			converters[i++] = factory.getConverter(a, false, params);
		return converters;
	}

	static Converter<?>[] getConverters(fr.labri.shelly.ConverterFactory factory, Class<?>[] params, Annotation[][] annotations) {
		int i = 0;
		Converter<?>[] converters = new Converter<?>[params.length];
		Class<? extends fr.labri.shelly.ConverterFactory> c;
		for (Class<?> a : params) {
			fr.labri.shelly.ConverterFactory f = factory;
			Param pa = AnnotationUtils.getAnnotation(annotations[i], Param.class);
			if(pa != null && !fr.labri.shelly.ConverterFactory.class.equals(c = pa.converter()))
					f = cache.newFactory(c);
			Converter<?> converter = null;
			if (f != null)
				converter = f.getConverter(a, false, params);
			if(converter == null)
				converter = factory.getConverter(a, false, params);
			
			converters[i++] =  converter;
		}
		return converters;
	}
	
	public static Object[] convertArray(Converter<?>[] converters, String cmd, PeekIterator<String> cmdLine) {
		Object[] args = new Object[converters.length];
		for (int i = 0; i < converters.length; i++)
			args[i] = converters[i].convert(cmd, cmdLine);
		return args;
	}

	static private Cache<fr.labri.shelly.ConverterFactory> cache = new Cache<fr.labri.shelly.ConverterFactory>(){
		@Override
		fr.labri.shelly.ConverterFactory newItem(Class<fr.labri.shelly.ConverterFactory> clazz) throws InstantiationException, IllegalAccessException {
			return clazz.newInstance();
		}
	};
	
	public static fr.labri.shelly.ConverterFactory getComposite(fr.labri.shelly.ConverterFactory parent, Class<? extends fr.labri.shelly.ConverterFactory>[] newFactory) {
		fr.labri.shelly.ConverterFactory[] factories = new fr.labri.shelly.ConverterFactory[newFactory.length];
		for(int i = 0; i < newFactory.length; i ++)
			factories[i] = cache.newFactory(newFactory[i]);
		return new CompositeFactory(factories, parent);
	}
}
