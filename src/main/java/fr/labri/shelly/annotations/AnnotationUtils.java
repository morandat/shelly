package fr.labri.shelly.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.ExecutableModelFactory;
import fr.labri.shelly.ModelFactory;

public class AnnotationUtils {
	public interface AnnotationType<V> {
		<A extends Annotation> A getValue(V elt, Class<? extends A> a);
	}

	public static abstract class AnnotationValue<T, V> {
		final Map<Class<? extends Annotation>, Method> f = new HashMap<>(ModelFactory.SHELLY_ANNOTATIONS.size());
		final V dflt; 
		public AnnotationValue(String field, V value) {
			dflt = value;
			for (Class<? extends Annotation> a : ModelFactory.SHELLY_ANNOTATIONS)
				try {
					f.put(a, a.getMethod(field));
				} catch (SecurityException | NoSuchMethodException e) {
				}
		}

		public abstract Annotation getAnnotation(T elt, Class<? extends Annotation> a);

		@SuppressWarnings("unchecked")
		public V getValue(Class<? extends Annotation> a, T elt) {
			Annotation c = getAnnotation(elt, a);
			try {
				if (f.containsKey(a))
					return (V) f.get(a).invoke(c);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
			return dflt;
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

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> _dummy) {
		for (Annotation a : annotations)
			if (_dummy.equals(a.annotationType()))
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
		return !name.equals(Option.NO_NAME) ? name : dflt.toLowerCase();
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
		if (converter.length < 1 || ConverterFactory.BasicConverters.class.equals(converter[0]))
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

	public static Annotation[] extractAnnotation(Element clazz) {
		List<? extends AnnotationMirror> l = clazz.getAnnotationMirrors();
		ArrayList<Annotation> res = new ArrayList<Annotation>(l.size());
		for(Class<? extends Annotation> a : ModelFactory.SHELLY_ANNOTATIONS) {
			Annotation v = clazz.getAnnotation(a);
			if(v != null)
				res.add(v);
		}
		return res.toArray(new Annotation[res.size()]);
	}

	public static Annotation[] extractAnnotation(Member item) {
		return extractAnnotation((AnnotatedElement) item);
	}
	
	public static Annotation[] extractAnnotation(AnnotatedElement item) {
		return item.getAnnotations();
	}
	
}
