package fr.labri.shelly;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Visitor.ParentVisitor;
import fr.labri.shelly.annotations.AnnotationUtils;

public interface Item<C, M> {
	public abstract String getID();

	public abstract Composite<C, M> getParent();

	public abstract void startVisit(Visitor<C, M> visitor);

	public abstract void accept(Visitor<C, M> visitor);

	public abstract <A extends Annotation> A getAnnotation(Class<A> a);

	public abstract boolean hasAnnotation(Class<? extends Annotation> a);

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
		public <A extends Annotation> A getAnnotation(Class<A> a) {
			return AnnotationUtils.getAnnotation(_annotations, a);
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> a) {
			return getAnnotation(a) != null;
		}

		@Override
		public Composite<C, M> getParent() {
			return _parent;
		}

		static public <C, M> String getFullName(Item<C, M> item) {
			final StringBuilder builder = new StringBuilder();
			item.accept(new ParentVisitor<C, M>() {
				@Override
				public void visit(Item<C, M> item) {
					super.visit(item);
					if (item.getParent() != null)
						builder.append(".");
					builder.append(item.getID());
				}
			});
			return builder.toString();
		}
	}
}
