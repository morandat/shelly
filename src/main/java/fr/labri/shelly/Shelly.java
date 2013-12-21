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
	
	static public Shell createShell(Recognizer model, Group<Class<?>, Member> group) {
		return new Shell(model, group);
	}
	static public Shell createShell(Recognizer model, ModelBuilder<Class<?>, Member> factory, Class<?> clazz) {
		return createShell(model, factory.createModel(clazz)); // FIXME
	}
	static public Shell createShell(ModelBuilder<Class<?>, Member> factory, Class<?> clazz) {
		return createShell(Recognizer.DEFAULT, factory.createModel(clazz)); // FIXME
	}
	static public Shell createShell(Recognizer model, Class<?> clazz) {
		return createShell(new ExecutableModelFactory.Executable(), clazz);
	}
	static public Shell createShell(Class<?> clazz) {
		return createShell(Recognizer.DEFAULT, createModel(clazz));
	}
	

	static public CommandLine createCommandLine(Group<Class<?>, Member> group, Recognizer model) {
		return new CommandLine(Recognizer.DEFAULT, group);
	}
	static public CommandLine createCommandLine(ModelBuilder<Class<?>, Member> factory, Class<?> clazz) {
		return new CommandLine(Recognizer.DEFAULT, factory.createModel(clazz)); // FIXME
	}
	static public CommandLine createCommandLine(Class<?> clazz, Recognizer model) {
		return createCommandLine(new ExecutableModelFactory.Executable(), clazz);
	}
	static public CommandLine createCommandLine(Class<?> clazz) {
		return createCommandLine(createModel(clazz), Recognizer.DEFAULT);
	}
}
