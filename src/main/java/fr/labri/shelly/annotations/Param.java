package fr.labri.shelly.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.labri.shelly.ConverterFactory;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
	String value() default Option.NO_NAME;
	Class<? extends ConverterFactory> converter() default ConverterFactory.class;
}
