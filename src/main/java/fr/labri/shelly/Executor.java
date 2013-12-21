package fr.labri.shelly;

import java.lang.reflect.Member;

import fr.labri.shelly.annotations.Ignore.ExecutorMode;
import fr.labri.shelly.impl.PeekIterator;

public interface Executor {

	public abstract Recognizer getRecognizer();
	public abstract ExecutorMode getMode();
	
	public abstract PeekIterator<String> getCommandLine();
	
	public abstract void execute(Group<Class<?>, Member> start);
	public abstract void error(Composite<Class<?>, Member> grp);

}
