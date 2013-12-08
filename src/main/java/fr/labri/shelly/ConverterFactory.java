package fr.labri.shelly;

public interface ConverterFactory {
	public Converter<?> getConverter(Class<?> type, Object context);
	
	ConverterFactory DEFAULT = new fr.labri.shelly.impl.ConverterFactory();
}