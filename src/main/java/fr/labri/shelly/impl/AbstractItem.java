package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Item;
import fr.labri.shelly.annotations.AnnotationUtils;

public abstract class AbstractItem<C, M> implements Item<C, M> {
	protected final Annotation[] _annotations;
	protected final Composite<C, M> _parent;
	protected final String _id;

	public AbstractItem(String name, Composite<C, M> parent, Annotation[] annotations) {
		_id = name;
		_parent = parent;
		_annotations = annotations;
	}

	@Override
	public String getID() {
		return _id;
	}

	@Override
	public Composite<C, M> getParent() {
		return _parent;
	}
	
	@Override
	public <A extends Annotation> A getAnnotation(Class<A> a) {
		return AnnotationUtils.getAnnotation(_annotations, a);
	}
	
	@Override
	public boolean hasAnnotation(Class<? extends Annotation> a) {
		return getAnnotation(a) != null;
	}	
}
