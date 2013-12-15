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
		public int isValid(String str, int index) {
			return startWith(str, _id, index);
		}
		
		@Override
		public M getAssociatedElement() {
			return _element;
		}

		static public int startWith(String str, String prefix, int offset) {
			return str.startsWith(prefix, offset) ? prefix.length() : -1;
		}
		
		static public boolean endsWith(String str, String suffix, int offset) {
			return (offset + suffix.length() == str.length()) ? str.endsWith(suffix) : false;
		}
		
		@Override
		public void executeAction(Object receive, String string, Executor executor) {
		}

}
