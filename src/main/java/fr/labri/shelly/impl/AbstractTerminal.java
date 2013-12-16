package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Terminal;

public abstract class AbstractTerminal<C, M> extends AbstractItem<C, M> implements Terminal<C, M> {
		protected final M _element;
		public AbstractTerminal(String name, Composite<C, M> parent, M item, Annotation[] annotations) {
			super(name, parent, annotations);
			_element = item;
		}

		@Override
		public int isValid(Parser parser, String str, int index) {
			return StringUtils.startWith(str, _id, index);
		}
		
		@Override
		public M getAssociatedElement() {
			return _element;
		}

		@Override
		public void executeAction(Object receive, String string, Executor executor) {
		}
}
