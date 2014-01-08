package fr.labri.shelly.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.ExecutableModelFactory;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command{
	String name() default Option.NO_NAME;
	String summary() default Option.NO_NAME;
	Class<? extends ConverterFactory>[] converter() default ConverterFactory.BasicConverters.class;
	Class<? extends ExecutableModelFactory> factory() default ExecutableModelFactory.class;
}
