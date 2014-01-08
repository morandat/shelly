package fr.labri.shelly;

import java.lang.annotation.Annotation;

public interface Group<C, M> extends Composite<C, M>, Action<C, M> {
	
	public abstract class AbstractGroup<C, M> extends AbstractComposite<C, M> implements Group<C, M>, Triggerable<C,M> {
		public AbstractGroup(Composite<C, M> parent, String name, C clazz, Annotation[] annotations) {
			super(parent, name, clazz, annotations);
		}

		@Override
		public void accept(Visitor<C, M> visitor) {
			visitor.visit((Group<C, M>) this);
		}

		@Override
		public void startVisit(Visitor<C, M> visitor) {
			visitor.startVisit(this);
		}
		
		@Override
		public int isValidLongOption(Recognizer parser, String str, int index) {
			return Util.startWith(str, _id, index);
		}
		
		@Override
		public void execute(Object receive, String string, Executor executor) {
		}

		@Override
		public String toString() {
			return "group " + getID();
		}
	}
}
