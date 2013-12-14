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
		Visitor<C, M> option_visitor = new Visitor<C, M>() {
			@Override
			public void visit(Action<C, M> option) {
				Visitor.this.visit(option);
			}
			@Override
			public void visit(Group<C, M> cmp) {
				visit((Action<C, M>) cmp);
			}
			@Override
			public void visit(Composite<C, M> cmp) {
				cmp.visit_all(this);
			}
			@Override
			public void startVisit(Composite<C, M> cmp) {
				cmp.visit_all(this);
			}
		};
		comp.visit_all(option_visitor);
	}
	@Override
	public void visit(Option<C, M> option) {
		visit((Terminal<C, M>)option);
	}
	
	@Override
	public void visit(Command<C, M> cmd) {
		visit((Terminal<C, M>)cmd);
	}
	
	@Override
	public void visit(Terminal<C, M> option) {
		visit((Item<C, M>)option);
	}

	@Override
	public void visit(Action<C, M> option) {
		visit((Item<C, M>)option);
	}

	@Override
	public void visit(Composite<C, M> optionGroup) {
		visit((Item<C, M>)optionGroup);
	}
	
	@Override
	public void visit(Context<C, M> optionGroup) {
		visit((Composite<C, M>)optionGroup);
	}

	@Override
	public void visit(Group<C, M> grp) {
		visit((Composite<C, M>)grp);
	}
	
	public static class TraversalVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(Composite<C, M> item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group<C, M> cmdGroup) {
			visit((Composite<C, M>)cmdGroup);
		}
	}
	public static class ParentVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(Item<C, M> option) {
			visit_parent(option);
		}
		
	}	
	public static class OptionVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(Group<C, M> grp) {
		}
		@Override
		public void visit(Terminal<C, M> grp) {
			visit_parent(grp);
		}

		@Override
		public void visit(Composite<C, M> grp) {
			visit_options(grp);
			visit_parent(grp);
		}
		
		public void startVisit(Group<C, M> cmdGroup) {
			cmdGroup.visit_all(this);
		}
	}

	
	public static class ActionVisitor<C, M> extends Visitor<C, M> {
		
		@Override
		public void visit(Group<C, M> cmdGrp) {
			visit((Action<C, M>) cmdGrp);
		}
		
		@Override
		public void visit(Composite<C, M> cmd) {
			cmd.visit_all(this);
		}
		@Override
		public void visit(Command<C, M> cmd) {
			visit((Action<C, M>)cmd);
		}
		
		public void startVisit(Group<C, M> cmdGroup) {
			cmdGroup.visit_all(this);
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
	public void startVisit(Option<C, M> option) {
		visit(option);
	}

	@Override
	public void startVisit(Command<C, M> cmd) {
		visit(cmd);
	}

	@Override
	public void startVisit(Group<C, M> cmdGroup) {
		startVisit((Composite<C, M>)cmdGroup);
	}

	@Override
	public void startVisit(Context<C, M> optionGroup) {
		startVisit((Composite<C, M>)optionGroup);
	}
	
	@Override
	public void startVisit(Composite<C, M> optionGroup) {
		visit(optionGroup);
	}
}
