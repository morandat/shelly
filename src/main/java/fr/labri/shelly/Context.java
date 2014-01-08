package fr.labri.shelly;

import java.lang.annotation.Annotation;

public interface Context<C, M> extends Composite<C, M> {

	public abstract class AbstractContext<C, M> extends AbstractComposite<C, M> implements Context<C, M> {
		public AbstractContext(Composite<C, M> parent, String name, C clazz, Annotation[] annotations) {
			super(parent, name, clazz, annotations);
		}

		@Override
		public void startVisit(Visitor<C, M> visitor) {
			visitor.visit(this);
		}

		public void accept(Visitor<C, M> visitor) {
			visitor.visit(this);
		}

		@Override
		public String toString() {
			return "context " + getID();
		}
	}
}
