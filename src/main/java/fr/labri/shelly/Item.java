package fr.labri.shelly;

import java.lang.annotation.Annotation;

public interface Item<C, M> {
	public abstract String getID();
	public abstract Composite<C, M> getParent();

	public abstract void startVisit(Visitor<C, M> visitor);
	public abstract void accept(Visitor<C, M> visitor);
	public abstract <A extends Annotation> A getAnnotation(Class<A> a);
	public abstract boolean hasAnnotation(Class<? extends Annotation> a);

}
