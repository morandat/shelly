package fr.labri.shelly;

import java.lang.reflect.Member;


public interface Converter<T> {
	public abstract T convert(String cmd, Executor executor);

	public abstract Class<? super T> convertedType();
	int isValid(Option<Class<?>, Member> opt, Recognizer recognizer, String cmd, int index); // FIXME this is ugly
}
