package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.labri.shelly.Converter;
import fr.labri.shelly.Executor;
import fr.labri.shelly.Option;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.ShellyException;
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
		public Converter<?> getConverter(Class<?> type, boolean isOption) {
			for(fr.labri.shelly.ConverterFactory factory : _factories) {
				Converter<?> converter = factory.getConverter(type, isOption);
				if(converter != null) {
					return converter;
				}
			}
			if(_parent != null)
				return _parent.getConverter(type, isOption);
			return null;
		}
	}
	
	public static Object[] convertArray(String cmd, Converter<?>[] converters, Executor executor) {
		Object[] res = new Object[converters.length];
		int i = 0;
		for(Converter<?> converter : converters)
			res[i++] = converter.convert(cmd, executor);
		return res;
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
	
	static abstract class PrimitiveConverterFactory<T> {
		Class<T> type, primitiveType;

		PrimitiveConverterFactory(Class<T> type, Class<T> primitiveType) {
			this.type = type; 
			this.primitiveType = primitiveType;
		}
		Converter<T> getPrimitiveConverter() {
			return new SimpleConverter<T>() {
				@Override
				public T convert(String value) {
					return PrimitiveConverterFactory.this.convert(value);
				}
				@Override
				public Class<T> convertedType() {
					return primitiveType;
				}
			}; 
		}
		Converter<T> getOjectConverter() {
			return new SimpleConverter<T>() {
				@Override
				public T convert(String value) {
					return PrimitiveConverterFactory.this.convert(value);
				}
				@Override
				public Class<T> convertedType() {
					return type;
				}
			}; 
		}
		abstract public T convert(String cmd);
	};
	static class BooleanFactory extends PrimitiveConverterFactory<Boolean>{
		BooleanFactory() {
			super(Boolean.class, boolean.class);
		}

		@Override
		public Boolean convert(String cmd) {
			return Boolean.parseBoolean(cmd);
		}
	}
	static class IntFactory extends PrimitiveConverterFactory<Integer>{
		IntFactory() {
			super(Integer.class, int.class);
		}

		@Override
		public Integer convert(String cmd) {
			return Integer.parseInt(cmd);
		}
	}
	static class ShortFactory extends PrimitiveConverterFactory<Short>{
		ShortFactory() {
			super(Short.class, short.class);
		}

		@Override
		public Short convert(String cmd) {
			return Short.parseShort(cmd);
		}
	}
	static class LongFactory extends PrimitiveConverterFactory<Long>{
		LongFactory() {
			super(Long.class, long.class);
		}

		@Override
		public Long convert(String cmd) {
			return Long.parseLong(cmd);
		}
	}
	
	static abstract class BooleanOptionConverter implements Converter<Boolean>{
		@Override
		public Boolean convert(String cmd, Executor executor) {
			return executor.getRecognizer().getBooleanValue(cmd);
		}

		@Override
		public int isValid(Option<Class<?>, Member> opt, Recognizer recognizer, String cmd, int index) {
			return recognizer.isLongBooleanOptionValid(cmd, opt, index);
		}
		
		abstract public Class<? super Boolean> convertedType();
	}

	public static final Converter<?>[] DEFAULTS_OPTIONS = new Converter<?>[] {
		new BooleanOptionConverter() {
			@Override
			public Class<? super Boolean> convertedType() {
				return boolean.class;
			}
		},
		new BooleanOptionConverter() {
			@Override
			public Class<? super Boolean> convertedType() {
				return Boolean.class;
			}
		},
	};
	
	public static final Converter<?>[] DEFAULTS = new Converter<?>[] {
		STR_CONVERTER,

		new IntFactory().getOjectConverter(),
		new IntFactory().getPrimitiveConverter(),
		new LongFactory().getOjectConverter(),
		new LongFactory().getPrimitiveConverter(),
		new ShortFactory().getOjectConverter(),
		new ShortFactory().getPrimitiveConverter(),
		new MapEntryConverter()
	};

	public static class BasicConverter implements fr.labri.shelly.ConverterFactory {
		
		@SuppressWarnings("unchecked")
		public Converter<?> getConverter(Class<?> type, boolean isOption) {
			Converter<?> converter = null;
			if (type.isArray())
				converter = getActionArrayConverter((Class<Object[]>)type);
			else
				converter = getObjectConverter(type, isOption);

			if (converter == null)
				throw new RuntimeException(String.format("No converter for type %s", type.toString()));
			return converter;
		}
		
		public Converter<?> getActionArrayConverter(Class<? extends Object[]> type) {
			return new ArrayConverter<>(getConverter(type.getComponentType(), false));
		}
		
		@SuppressWarnings("unchecked")
		public <E> Converter<E> getObjectConverter(Class<E> type, boolean isOption) {
			if(isOption)
				for(Converter<?> c: DEFAULTS_OPTIONS)
					if(type.isAssignableFrom(c.convertedType()))
						return (Converter<E>) c;
			for(Converter<?> c: DEFAULTS)
				if(type.isAssignableFrom(c.convertedType()))
					return (Converter<E>) c;
			return null;
		}
	}

	static class MapEntryConverter implements Converter<Map.Entry<String, String>> {
		String getMapSeparator() {
			return "=";
		}
		@Override
		public Entry<String, String> convert(String cmd, Executor executor) {
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

		@Override
		public int isValid(Option<Class<?>, Member> opt, Recognizer recognizer, String cmd, int index) {
			return recognizer.isLongOptionValid(cmd, opt);
		}
	}

	static class ArrayConverter<E> implements Converter<E[]> {
		final Converter<? extends E> _converter;
		
		public int isValid(Option<Class<?>, Member> opt, Recognizer recognizer, String cmd, int index) {
			return recognizer.isLongOptionValid(cmd, opt);
		}
		
		public ArrayConverter(Converter<E> converter) {
			_converter = converter;
		}

		@SuppressWarnings("unchecked")
		final public E[] convert(String cmd, Executor executor) {
			ArrayList<E> lst = new ArrayList<>();
			PeekIterator<String> line = executor.getCommandLine();
			while (line.hasNext())
				lst.add(_converter.convert(cmd, executor));
			return lst.toArray((E[]) Array.newInstance(_converter.convertedType(), lst.size()));
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<? super E[]> convertedType() {
			return (Class<? super E[]>) Array.newInstance(_converter.convertedType(), 0).getClass();
		}
	};
	
	public static abstract class SimpleConverter<T> implements Converter<T> {
		@Override
		public int isValid(Option<Class<?>, Member> opt, Recognizer recognizer, String cmd, int index) {
			return recognizer.isLongOptionValid(cmd, opt);
		}

		final public T convert(String cmd, Executor executor) {
			return convert(executor.getCommandLine().next());
		}

		@Override
		public Class<? super T> convertedType() {
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

	public static Converter<?>[] getConverters(final fr.labri.shelly.ConverterFactory factory, Class<?> param, final boolean isOption) {
		return new Converter[] { new fr.labri.shelly.impl.Converters() {
			public Converter<?> getConverter(Class<?> type, Object context) {
				Converter<?> converter = factory.getConverter(type, isOption);
				return new ArrayConverter<>(converter);
			}
		}.getConverter(param, param) };
	}

	static Converter<?>[] getConverters(fr.labri.shelly.ConverterFactory factory, Class<?>[] params, boolean isOption) {
		int i = 0;
		Converter<?>[] converters = new Converter<?>[params.length];
		for (Class<?> a : params)
			converters[i++] = factory.getConverter(a, isOption);
		return converters;
	}

	static Converter<?>[] getConverters(fr.labri.shelly.ConverterFactory factory, Class<?>[] params, Annotation[][] annotations, boolean isOption) {
		int i = 0;
		Converter<?>[] converters = new Converter<?>[params.length];
		for (Class<?> a : params) {
			fr.labri.shelly.ConverterFactory f = factory;
			Class<? extends fr.labri.shelly.ConverterFactory> c;
			Param pa = AnnotationUtils.getAnnotation(annotations[i], Param.class);
			if(pa != null && !fr.labri.shelly.ConverterFactory.class.equals(c = pa.converter()))
				f = cache.newFactory(c);
			
			converters[i++] =  ((f == null) ? factory : f).getConverter(a, isOption);
		}
		return converters;
	}
	
	public static fr.labri.shelly.ConverterFactory getComposite(fr.labri.shelly.ConverterFactory parent, Class<? extends fr.labri.shelly.ConverterFactory>[] newFactory) {
		fr.labri.shelly.ConverterFactory[] factories = new fr.labri.shelly.ConverterFactory[newFactory.length];
		for(int i = 0; i < newFactory.length; i ++)
			factories[i] = cache.newFactory(newFactory[i]);
		return new CompositeFactory(factories, parent);
	}

	private static Cache<fr.labri.shelly.ConverterFactory> cache = new Cache<fr.labri.shelly.ConverterFactory>(){
		@Override
		fr.labri.shelly.ConverterFactory newItem(Class<fr.labri.shelly.ConverterFactory> clazz) throws InstantiationException, IllegalAccessException {
			return clazz.newInstance();
		}
	};
	
}
