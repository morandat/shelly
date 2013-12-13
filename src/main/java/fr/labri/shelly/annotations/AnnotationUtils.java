package fr.labri.shelly.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.ModelFactory;
import fr.labri.shelly.impl.ExecutableModelFactory;
import fr.labri.shelly.impl.ConverterFactory.BasicConverter;

public class AnnotationUtils {
	public interface AnnotationType<V> {
		<A extends Annotation> A getValue(V elt, Class<? extends A> a);
	}
	final public static AnnotationType<AnnotatedElement> REFLECT = new AnnotationType<AnnotatedElement>() {
		@Override
		public <A extends Annotation> A getValue(AnnotatedElement elt, Class<? extends A> a) {
			return elt.getAnnotation(a);
		}
	};
	final public static AnnotationType<Element> ELEMENT = new AnnotationType<Element>() {
		@Override
		public <A extends Annotation> A getValue(Element elt, Class<? extends A> a) {
			return elt.getAnnotation(a);
		}
	};
	public static abstract class AnnotationValue<T, V> {
		final Map<Class<? extends Annotation>, Field> f = new HashMap<>(ModelFactory.SHELLY_ANNOTATIONS.size());
		final V dflt; 
		public AnnotationValue(String field, V value) {
			dflt = value;
			try {
				for (Class<? extends Annotation> a : ModelFactory.SHELLY_ANNOTATIONS)
					f.put(a, a.getField(field));
			} catch (NoSuchFieldException | SecurityException e) {
			}
		}

		abstract Annotation getAnnotation(T elt, Class<? extends Annotation> a);

		@SuppressWarnings("unchecked")
		public V getValue(Class<? extends Annotation> a, T elt) {
			Annotation c = getAnnotation(elt, a);
			return c == null ? dflt : (f.containsKey(c) ? (V)f.get(c) : dflt);
		}
		
		public V getOption(T o) {
			return getValue(Option.class, o);
		}
		public V getCommand(T o) {
			return getValue(Command.class, o);
		}
		public V getGroup(T o) {
			return getValue(Group.class, o);
		}
		public V getContext(T o) {
			return getValue(Context.class, o);
		}
	}
	public static class ElementValue<V> extends AnnotationValue<Element, V> {
		public ElementValue(String field, V value) {
			super(field, value);
		}

		@Override
		Annotation getAnnotation(Element elt, Class<? extends Annotation> a) {
			return elt.getAnnotation(a);
		}
		
	}
	public static class ReflectValue<V> extends AnnotationValue<AnnotatedElement, V> {
		public ReflectValue(String field, V value) {
			super(field, value);
		}

		@Override
		Annotation getAnnotation(AnnotatedElement elt, Class<? extends Annotation> a) {
			return elt.getAnnotation(a);
		}
		
	}

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> _dummy) {
		for (Annotation a : annotations)
			if (a.annotationType().equals(_dummy))
				return (T) a;
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Iterable<Annotation> annotations, Class<T> _dummy) {
		for (Annotation a : annotations)
			if (a.annotationType().equals(_dummy))
				return (T) a;
		return null;
	}

	static public String getName(String name, String dflt) {
		return !name.equals(fr.labri.shelly.annotations.Option.NO_NAME) ? name : dflt.toLowerCase();
	}

	final public static Class<? extends ExecutableModelFactory> getFactory(Class<? extends ExecutableModelFactory> factory) {
		if (!ModelFactory.class.equals(factory))
			return factory;
		return null;
	}

	public static Class<? extends ExecutableModelFactory> getFactory(Command annotation) {
		try {
			return getFactory(annotation.factory());
		} catch (javax.lang.model.type.MirroredTypeException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Class<? extends ExecutableModelFactory> getFactory(Option annotation) {
		try {
			return getFactory(annotation.factory());
		} catch (javax.lang.model.type.MirroredTypeException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Class<? extends ExecutableModelFactory> getFactory(Group annotation) {
		try {
			return getFactory(annotation.factory());
		} catch (javax.lang.model.type.MirroredTypeException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Class<? extends ExecutableModelFactory> getFactory(Context annotation) {
		try {
			return getFactory(annotation.factory());
		} catch (javax.lang.model.type.MirroredTypeException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	final public static Class<? extends ConverterFactory>[] getConverterFactory(Class<? extends ConverterFactory> converter[]) {
		if (converter.length < 1 || BasicConverter.class.equals(converter[0]))
			return null;
		return converter;
	}

	public static Class<? extends ConverterFactory>[] getConverterFactory(Command annotation) {
		try {
			return getConverterFactory(annotation.converter());
		} catch (javax.lang.model.type.MirroredTypeException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Class<? extends ConverterFactory>[] getConverterFactory(Option annotation) {
		try {
			return getConverterFactory(annotation.converter());
		} catch (javax.lang.model.type.MirroredTypeException e) {
			return null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	// public static Class<? extends ConverterFactory> getConverterFactory(Group annotation) {
	// try {
	// return getConverterFactory(annotation.converter());
	// } catch (javax.lang.model.type.MirroredTypeException e) {
	// return null;
	// }
	// }
	// public static Class<? extends ConverterFactory> getConverterFactory(Context annotation) {
	// try {
	// return getConverterFactory(annotation.converter());
	// } catch (javax.lang.model.type.MirroredTypeException e) {
	// return null;
	// }
	// }
}
