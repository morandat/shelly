package fr.labri.shelly.impl;

import java.lang.annotation.Annotation;

import fr.labri.shelly.Action;
import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Item;
import fr.labri.shelly.Terminal;

public class Visitor<C, M> implements fr.labri.shelly.Visitor<C, M> {
	@Override
	public void visit(Item<C, M> item) {
	}
	
	public void visit_parent(Item<C, M> item) {
		Item<C, M> p = item.getParent();
		if(p != null)
			p.accept(this);
	}
	
	public void visit_options(Item<C, M> comp) {
		Visitor<C, M> option_visitor = new Visitor<C, M>() {
			@Override
			public void visit(Option<C, M> option) {
				Visitor.this.visit(option);
			}
			@Override
			public void startVisit(Composite<C, M> cmp) {
				cmp.visit_all(this);
			}
		};
		comp.startVisit(option_visitor);
	}
	public void visit_actions(Composite<C, M> comp) {
		Visitor<C, M> action_visitor = new Visitor<C, M>() {
			@Override
			public void visit(Action<C, M> option) {
				Visitor.this.visit(option);
			}
			@Override
			public void visit(Group<C, M> cmp) {
				visit((Action<C, M>) cmp);
			}
			@Override
			public void visit(Command<C, M> cmp) {
				visit((Action<C, M>) cmp);
			}
			@Override
			public void visit(Composite<C, M> cmp) {
				cmp.visit_all(this);
			}
		};
		comp.visit_all(action_visitor);
	}
	@Override
	public void visit(Option<C, M> item) {
		visit((Terminal<C, M>)item);
	}
	
	@Override
	public void visit(Command<C, M> item) {
		visit((Terminal<C, M>)item);
	}
	
	@Override
	public void visit(Terminal<C, M> item) {
		visit((Item<C, M>)item);
	}

	@Override
	public void visit(Action<C, M> item) {
		visit((Item<C, M>)item);
	}

	@Override
	public void visit(Composite<C, M> item) {
		visit((Item<C, M>)item);
	}
	
	@Override
	public void visit(Context<C, M> item) {
		visit((Composite<C, M>)item);
	}

	@Override
	public void visit(Group<C, M> item) {
		visit((Composite<C, M>)item);
	}
	
	public static class TraversalVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(Composite<C, M> item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group<C, M> item) {
			visit((Composite<C, M>)item);
		}
	}
	public static class ParentVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(Item<C, M> item) {
			visit_parent(item);
		}
		
	}	
	public static class OptionVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(Group<C, M> item) {
		}
		@Override
		public void visit(Terminal<C, M> item) {
			visit_parent(item);
		}

		@Override
		public void visit(Composite<C, M> item) {
			visit_options(item);
			visit_parent(item);
		}
		
		public void startVisit(Group<C, M> item) {
			item.visit_all(this);
		}
	}

	
	public static class ActionVisitor<C, M> extends Visitor<C, M> {
		
		@Override
		public void visit(Group<C, M> item) {
			visit((Action<C, M>) item);
		}
		
		@Override
		public void visit(Composite<C, M> item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Command<C, M> item) {
			visit((Action<C, M>)item);
		}
		
		public void startVisit(Command<C, M> item) {
		}
		
		public void startVisit(Group<C, M> item) {
			visit_actions(item);
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundCommand extends RuntimeException {
		public Action<?, ?> cmd;

		public FoundCommand(Action<?,?> cmd) {
			this.cmd = cmd;
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundOption extends RuntimeException {
		public Option<?, ?> opt;

		public FoundOption(Option<?, ?> opt) {
			this.opt = opt;
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundAnnotation extends RuntimeException {
		public Annotation annotation;

		public FoundAnnotation(Annotation a) {
			this.annotation = a;
		}
	}

	@Override
	public void startVisit(Option<C, M> item) {
		visit(item);
	}

	@Override
	public void startVisit(Command<C, M> item) {
		visit(item);
	}

	@Override
	public void startVisit(Group<C, M> item) {
		startVisit((Composite<C, M>)item);
	}

	@Override
	public void startVisit(Context<C, M> item) {
		startVisit((Composite<C, M>)item);
	}
	
	@Override
	public void startVisit(Composite<C, M> item) {
		visit(item);
	}
}
