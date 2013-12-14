package fr.labri.shelly.impl;

import fr.labri.shelly.Composite;
import fr.labri.shelly.Terminal;
import fr.labri.shelly.Visitor;

public abstract class AbstractTerminal<C, M> implements Terminal<C, M> {
		protected final M _element;
		protected final Composite<C, M> _parent;
		protected final String _id;
		
		public AbstractTerminal(String name, Composite<C, M> parent, M item) {
			_id = name;
			_parent = parent;
			_element = item;
		}

		public String getID() {
			return _id;
		}

		@Override
		public int isValid(String str, int index) {
			return startWith(str, _id, index);
		}
		
		@Override
		public M getAssociatedElement() {
			return _element;
		}

		@Override
		public void visit_all(Visitor<C, M> visitor) {
		}

		@Override
		public Composite<C, M> getParent() {
			return _parent;
		}
		
		static public int startWith(String str, String prefix, int offset) {
			return str.startsWith(prefix, offset) ? prefix.length() : -1;
		}
		
		static public boolean endsWith(String str, String suffix, int offset) {
			return (offset + suffix.length() == str.length()) ? str.endsWith(suffix) : false;
		}
		
		@Override
		public Object apply(Object receive, String string, Executor executor) {
			return null;
		}

}
