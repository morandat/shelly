package fr.labri.shelly.annotations;

import fr.labri.shelly.ConverterFactory;

public @interface Param {
	String value();
	Class<? extends ConverterFactory>[] converter() default ConverterFactory.class;
}
