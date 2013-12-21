package fr.labri.shelly;

public interface ConverterFactory {
	public Converter<?> getConverter(Class<?> type);
}