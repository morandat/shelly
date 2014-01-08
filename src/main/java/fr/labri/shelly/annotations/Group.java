package fr.labri.shelly.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.labri.shelly.ExecutableModelFactory;

@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Group {
	String name() default Option.NO_NAME;
	String summary() default Option.NO_NAME;
	Class<? extends ExecutableModelFactory> factory() default ExecutableModelFactory.class;
}
