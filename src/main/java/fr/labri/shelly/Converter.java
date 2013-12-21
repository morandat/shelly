package fr.labri.shelly;

public interface Converter<T> {
	public abstract T convert(Executor executor);

	public abstract Class<? super T> convertedType();
}
