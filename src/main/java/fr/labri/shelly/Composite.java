package fr.labri.shelly;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public interface Composite<C, M> extends Item<C, M> {

	public abstract C getAssociatedElement();

	public abstract void addItem(Item<C, M> cmd);

	public abstract Iterable<Item<C, M>> getItems();

	public abstract void visit_all(Visitor<C, M> visitor);

	public abstract boolean isEnclosed();

	public abstract Object instantiateObject(Object parent);

	public abstract class AbstractComposite<C, M> extends AbstractItem<C, M> implements Composite<C, M> {

		protected final C _clazz;
		protected final List<Item<C, M>> commands = new ArrayList<Item<C, M>>();

		public AbstractComposite(Composite<C, M> parent, String name, C clazz, Annotation[] annotations) {
			super(name, parent, annotations);
			_clazz = clazz;
		}

		@Override
		public void visit_all(Visitor<C, M> visitor) {
			for (Item<C, M> cmd : commands)
				cmd.accept(visitor);
		}

		public void addItem(Item<C, M> cmd) {
			if (cmd != null)
				commands.add(cmd);
		}

		@Override
		public C getAssociatedElement() {
			return _clazz;
		}

		@Override
		public String getID() {
			return _id;
		}

		@Override
		public Iterable<Item<C, M>> getItems() {
			return commands;
		}

		@Override
		public Object instantiateObject(Object parent) {
			return null;
		}
	}
}
