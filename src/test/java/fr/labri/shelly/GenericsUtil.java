package fr.labri.shelly;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class GenericsUtil<E> {

	public List<String> lst;

	public static void main(String[] args) throws NoSuchFieldException, SecurityException {
		Field f = GenericsUtil.class.getField("lst");
		getCollection(f.getGenericType(), Collection.class);
//		Type[] gt = GenericsUtil.class.getField("lst").getGenericType();
//				.getGenericInterfaces();
//		Type ft = gt[0];
//		Type at = getActualType(ft, Collection.class);
//		Type ac = getActualClass(ft, Collection.class);
//		System.out.println(at);
//		System.out.println(ac);
	}
	@SuppressWarnings("unchecked")
	public static final Class<? extends Collection<?>>[] collection_types = (Class<? extends Collection<?>>[]) new Class<?>[] { 
		ArrayList.class,
		Vector.class,
		HashSet.class
	};
	
	@SuppressWarnings("unchecked")
	public static final Class<? extends Map<?, ?>>[] map_types = (Class<? extends Map<?, ?>>[]) new Class<?>[] { 
		LinkedHashMap.class,
		HashMap.class
	};

	public static <K,V> Map<K, V> getMap(Type type, Class<?> targetClass) {
		if (!(type instanceof ParameterizedType))
			throw new RuntimeException("Map should have a two type declared and be " + Arrays.toString(map_types)+ " or a super type of those");
		ParameterizedType pt = (ParameterizedType)type;
		Class<?> clazz = (Class<?>) (pt.getRawType());
		Type[] at = pt.getActualTypeArguments();
		if (!(at.length == 2 ||  at[0] instanceof Class && at[1] instanceof Class))
			throw new RuntimeException("Map key/val types must (currently??) be classes");
		Class<?> key = (Class<?>)at[0];
		Class<?> val = (Class<?>)at[1];
		
		if (targetClass.isAssignableFrom(clazz)) {
			for (Class<? extends Collection<?>> c: collection_types) {
				if(clazz.isAssignableFrom(c)) {
					System.out.println("found " + clazz + " " + c);
					System.out.println("bound "+ key + " " + val);
					return null;
				}
			}
		}
		return null;
	}
	
	public static <E> Collection<E> getCollection(Type type, Class<?> targetClass) {
		if (!(type instanceof ParameterizedType))
			throw new RuntimeException("Collections should have a single type declared and be " + Arrays.toString(collection_types)+ " or a super type of those");
		ParameterizedType pt = (ParameterizedType)type;
		Class<?> clazz = (Class<?>) (pt.getRawType());
		Type[] at = pt.getActualTypeArguments();
		if (at.length != 1 &&  !(at[0] instanceof Class))
			throw new RuntimeException("Collection type must (currently??) be classes");

		Class<?> bound = (Class<?>)at[0];
		if (targetClass.isAssignableFrom(clazz)) {
			for (Class<? extends Collection<?>> c: collection_types) {
				if(clazz.isAssignableFrom(c)) {
					System.out.println("found " + clazz + " " + c);
					System.out.println("bound "+ bound);
					return null;
				}
			}
		}
		return null;
	}

	public static Class<?> getActualClass(Type type, Class<?> targetClass) {
		Map<TypeVariable<?>, Type> map = getTypeVariableMap(targetClass);
		return getActualClass(type, map);
	}

	public static Type getActualType(Type type, Class<?> targetClass) {
		Map<TypeVariable<?>, Type> map = getTypeVariableMap(targetClass);
		return getActualType(type, map);
	}

	private static Class<?> getActualClass(Type type,
			Map<TypeVariable<?>, Type> map) {
		if (Class.class.isInstance(type)) {
			return Class.class.cast(type);
		}
		if (ParameterizedType.class.isInstance(type)) {
			final Type actualType = getActualType(type, map);
			return getActualClass(actualType, map);
		} else if (TypeVariable.class.isInstance(type)) {
			final Type actualType = getActualType(type, map);
			return getActualClass(actualType, map);
		} else if (GenericArrayType.class.isInstance(type)) {
			GenericArrayType genericArrayType = GenericArrayType.class
					.cast(type);
			final Type genericComponentType = genericArrayType
					.getGenericComponentType();
			Class<?> componentClass = getActualClass(genericComponentType, map);
			return Array.newInstance(componentClass, 0).getClass();
		} else {
			return null;
		}
	}

	private static Type getActualType(Type type, Map<TypeVariable<?>, Type> map) {
		if (Class.class.isInstance(type)) {
			return type;
		} else if (ParameterizedType.class.isInstance(type)) {
			return ParameterizedType.class.cast(type).getRawType();
		} else if (TypeVariable.class.isInstance(type)) {
			return map.get(TypeVariable.class.cast(type));
		} else {
			return null;
		}
	}

	private static Map<TypeVariable<?>, Type> getTypeVariableMap(
			final Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyMap();
		}
		final Map<TypeVariable<?>, Type> map = new LinkedHashMap<TypeVariable<?>, Type>();
		final Class<?> superClass = clazz.getSuperclass();
		final Type superClassType = clazz.getGenericSuperclass();
		if (superClass != null) {
			gatherTypeVariables(superClass, superClassType, map);
		}
		final Class<?>[] interfaces = clazz.getInterfaces();
		final Type[] interfaceTypes = clazz.getGenericInterfaces();
		for (int i = 0; i < interfaces.length; ++i) {
			gatherTypeVariables(interfaces[i], interfaceTypes[i], map);
		}
		return map;
	}

	private static void gatherTypeVariables(final Class<?> clazz,
			final Type type, final Map<TypeVariable<?>, Type> map) {
		if (clazz == null) {
			return;
		}
		gatherTypeVariables(type, map);

		final Class<?> superClass = clazz.getSuperclass();
		final Type superClassType = clazz.getGenericSuperclass();
		if (superClass != null) {
			gatherTypeVariables(superClass, superClassType, map);
		}
		final Class<?>[] interfaces = clazz.getInterfaces();
		final Type[] interfaceTypes = clazz.getGenericInterfaces();
		for (int i = 0; i < interfaces.length; ++i) {
			gatherTypeVariables(interfaces[i], interfaceTypes[i], map);
		}
	}

	private static void gatherTypeVariables(final Type type,
			final Map<TypeVariable<?>, Type> map) {
		if (ParameterizedType.class.isInstance(type)) {
			final ParameterizedType parameterizedType = ParameterizedType.class
					.cast(type);
			final TypeVariable<?>[] typeVariables = GenericDeclaration.class
					.cast(parameterizedType.getRawType()).getTypeParameters();
			final Type[] actualTypes = parameterizedType
					.getActualTypeArguments();
			for (int i = 0; i < actualTypes.length; ++i) {
				map.put(typeVariables[i], actualTypes[i]);
			}
		}
	}

}
