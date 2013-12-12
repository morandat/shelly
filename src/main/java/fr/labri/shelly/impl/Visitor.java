package fr.labri.shelly.impl;

import java.lang.reflect.Member;

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
		public void visit(Item<C, M> item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group<C, M> cmdGroup) {
			visit((Composite<C, M>)cmdGroup);
		}
	}
	public static class ParentVisitor<C, M> extends Visitor<C, M> {
		
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
			grp.visit_options(this);
			visit_parent(grp);
		}

		public void visit_options(Action<C, M> cmd) {
			if (cmd instanceof Group) {
				visit((Composite<C, M>) (Group<C, M>)cmd);
			} else
				cmd.accept(this);
		}
	}

	
	public static class CommandVisitor<C, M> extends Visitor<C, M> {
		
		@Override
		public void visit(Group<C, M> cmdGrp) {
			visit((Action<C, M>) cmdGrp);
		}
		
		@Override
		public void visit(Composite<C, M> cmd) {
			cmd.visit_commands(this);
		}
		@Override
		public void visit(Command<C, M> cmd) {
			visit((Action<C, M>)cmd);
		}
		
		public void visit_commands(Action<C, M> cmd) {
			if (cmd instanceof Group) {
				visit((Composite<C, M>) (Group<C, M>)cmd);
			} else
				cmd.accept(this);
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundCommand extends RuntimeException {
		public Action<Class<?>, Member> cmd;

		public FoundCommand(Action<Class<?>, Member> cmd) {
			this.cmd = cmd;
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundOption extends RuntimeException {
		public Option<Class<?>, Member> opt;

		public FoundOption(Option<Class<?>, Member> opt) {
			this.opt = opt;
		}
	}
}
