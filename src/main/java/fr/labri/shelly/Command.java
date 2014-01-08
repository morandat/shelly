package fr.labri.shelly;

import java.lang.annotation.Annotation;

public interface Command<C, M> extends Action<C, M>, Terminal<C, M> {
	
	public abstract class AbstractCommand<C, M> extends AbstractTerminal<C, M> implements Command<C, M> {
		public AbstractCommand(String name, Composite<C, M> parent, M item, Annotation[] annotations) {
			super(name, parent, item, annotations);
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
			return "command " + getID();
		}
	}
}
