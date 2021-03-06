package fr.labri.shelly;

import java.util.Arrays;

import fr.labri.shelly.Converter.ArrayConverter;
import fr.labri.shelly.Converter.MapEntryConverter;
import fr.labri.shelly.Converter.SimpleConverter;

public interface ConverterFactory {
	public Converter<?> getConverter(Class<?> type);
	
	public static Converter<String> STR_CONVERTER = new SimpleConverter<String>() {
		public String convert(String cmd) {
			return cmd;
		}

		@Override
		public Class<String> convertedType() {
			return String.class;
		}
	};
	
	public static final Converter<?>[] DEFAULTS = new Converter<?>[] {
		STR_CONVERTER,

		new IntFactory().getOjectConverter(),
		new IntFactory().getPrimitiveConverter(),
		new LongFactory().getOjectConverter(),
		new LongFactory().getPrimitiveConverter(),
		new ShortFactory().getOjectConverter(),
		new ShortFactory().getPrimitiveConverter(),
		new BooleanFactory().getOjectConverter(),
		new BooleanFactory().getPrimitiveConverter(),
		new MapEntryConverter()
	};

	public class BasicConverters implements ConverterFactory {
		@SuppressWarnings("unchecked")
		public Converter<?> getConverter(Class<?> type) {
			Converter<?> converter = null;
			if (type.isArray())
				converter = getActionArrayConverter((Class<Object[]>)type);
			else if (Enum.class.isAssignableFrom(type)) {
				converter = getEnumConverter(type);
			} else
				converter = getObjectConverter(type);

			if (converter == null)
				throw new RuntimeException(String.format("No converter for type %s", type.toString()));
			return converter;
		}

		public Converter<?> getEnumConverter(final Class<?> type) {
			return new SimpleConverter<Object>() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Object convert(String value) {
					return (Object)Enum.valueOf((Class)type, value.toUpperCase());
				}
			};
		}	
		
		public Converter<?> getActionArrayConverter(Class<? extends Object[]> type) {
			return new ArrayConverter<>(getConverter(type.getComponentType()));
		}
		
		@SuppressWarnings("unchecked")
		public <E> Converter<E> getObjectConverter(Class<E> type) {
			for(Converter<?> c: DEFAULTS)
				if(type.isAssignableFrom(c.convertedType()))
					return (Converter<E>) c;
			return null;
		}
	}

	public class CompositeFactory implements ConverterFactory {
		final ConverterFactory[] _factories;
		final ConverterFactory _parent;
		public CompositeFactory(ConverterFactory[] factories, ConverterFactory parent) {
			_factories = factories;
			_parent = parent;
		}
		@Override
		public Converter<?> getConverter(Class<?> type) {
			for(ConverterFactory factory : _factories) {
				Converter<?> converter = factory.getConverter(type);
				if(converter != null) {
					return converter;
				}
			}
			if(_parent != null)
				return _parent.getConverter(type);
			return null;
		}
	}
	
	public abstract class PrimitiveConverterFactory<T> {
		Class<T> type, primitiveType;
		PrimitiveConverterFactory(Class<T> type, Class<T> primitiveType) {
			this.type = type; 
			this.primitiveType = primitiveType;
		}
		Converter<T> getPrimitiveConverter() {
			return new SimpleConverter<T>() {
				@Override
				public T convert(String value) {
					return convertValue(value);
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
					return convertValue(value);
				}
				@Override
				public Class<T> convertedType() {
					return type;
				}
			}; 
		}
		abstract public T convertValue(String cmd);
	};
	
	public class BooleanFactory extends PrimitiveConverterFactory<Boolean>{
		BooleanFactory() {
			super(Boolean.class, boolean.class);
		}

		@Override
		public Boolean convertValue(String cmd) {
			return Boolean.parseBoolean(cmd);
		}
	}
	
	public class IntFactory extends PrimitiveConverterFactory<Integer>{
		IntFactory() {
			super(Integer.class, int.class);
		}

		@Override
		public Integer convertValue(String cmd) {
			return Integer.parseInt(cmd);
		}
	}
	
	public class ShortFactory extends PrimitiveConverterFactory<Short>{
		ShortFactory() {
			super(Short.class, short.class);
		}

		@Override
		public Short convertValue(String cmd) {
			return Short.parseShort(cmd);
		}
	}
	public class LongFactory extends PrimitiveConverterFactory<Long>{
		LongFactory() {
			super(Long.class, long.class);
		}

		@Override
		public Long convertValue(String cmd) {
			return Long.parseLong(cmd);
		}
	}
	public abstract class SeparatedString implements ConverterFactory {
		abstract String getSeparator();
		@Override
		public Converter<?> getConverter(Class<?> type) {
			if (type.isAssignableFrom(String[].class))
				return new Converter.SimpleConverter<String[]>() {
					@Override
					public String[] convert(String value) {
						System.out.println("Convert "+ value + " " + Arrays.toString(value.split(getSeparator())));
						return value.split(getSeparator());
					}
				};
			return null;
		}
	}
	public class CommaSeparated extends SeparatedString {
		@Override
		String getSeparator() {
			return ",";
		}
	}
}