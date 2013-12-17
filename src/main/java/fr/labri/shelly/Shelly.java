package fr.labri.shelly;

import java.lang.reflect.Member;

import fr.labri.shelly.impl.ExecutableModelFactory;
import fr.labri.shelly.impl.ModelBuilder;

public class Shelly {
	static public Group<Class<?>, Member> createModel(Class<?> clazz) {
		return createModel(new ExecutableModelFactory.Executable(), clazz);
	}
	
	static public Group<Class<?>, Member> createModel(ModelBuilder<Class<?>, Member> factory, Class<?> clazz) {
		return factory.createModel(clazz);
	}

	static public Shell createShell(ModelBuilder<Class<?>, Member> factory, Class<?> clazz) {
		return new Shell(factory.createModel(clazz));
	}

	static public Shell createShell(Class<?> clazz) {
		return createShell(new ExecutableModelFactory.Executable(), clazz);
	}
}
