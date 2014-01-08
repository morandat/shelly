package fr.labri.shelly;

import java.lang.annotation.Annotation;

public interface Terminal<C, M> extends Triggerable<C, M> {
	M getAssociatedElement();
	
	public abstract class AbstractTerminal<C, M> extends AbstractItem<C, M> implements Terminal<C, M> {
		protected final M _element;
		public AbstractTerminal(String name, Composite<C, M> parent, M item, Annotation[] annotations) {
			super(name, parent, annotations);
			_element = item;
		}

		@Override
		public int isValidLongOption(Recognizer parser, String str, int index) {
			return Util.startWith(str, _id, index);
		
		}
		
		@Override
		public M getAssociatedElement() {
			return _element;
		}

		@Override
		public void execute(Object receive, String string, Executor executor) {
		}
}

}
