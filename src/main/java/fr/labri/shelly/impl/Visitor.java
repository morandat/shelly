package fr.labri.shelly.impl;

import java.lang.reflect.Member;

import fr.labri.shelly.Command;
import fr.labri.shelly.Group;
import fr.labri.shelly.Option;
import fr.labri.shelly.Context;
import fr.labri.shelly.ShellyItem;

public class Visitor<C, M> implements fr.labri.shelly.Visitor<C, M> {
	@Override
	public void visit(ShellyItem<C, M> item) {
	}
	
	public void visit_parent(ShellyItem<C, M> item) {
		ShellyItem<C, M> p = item.getParent();
		if(p != null)
			p.accept(this);
	}
	
	@Override
	public void visit(Option<C, M> option) {
		visit((ShellyItem<C, M>)option);
	}
	
	@Override
	public void visit(Command<C, M> cmd) {
		visit((ShellyItem<C, M>)cmd);
	}

	@Override
	public void visit(Context<C, M> optionGroup) {
		visit((ShellyItem<C, M>)optionGroup);
	}

	@Override
	public void visit(Group<C, M> cmdGroup) {
		visit((ShellyItem<C, M>)cmdGroup);
	}
	
	public static class TraversalVisitor<C, M> extends Visitor<C, M> {
		@Override
		public void visit(ShellyItem<C, M> item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group<C, M> cmdGroup) {
			visit((Context<C, M>)cmdGroup);
		}
	}
	
	public static class OptionVisitor<C, M> extends Visitor<C, M> {
		public void visit(Group<C, M> grp) {
		}

		public void visit(Command<C, M> grp) {
			visit_parent(grp);
		}

		@Override
		public void visit(Context<C, M> grp) {
			grp.visit_options(this);
			visit_parent(grp);
		}

		public void visit_options(Command<C, M> cmd) {
			if (cmd instanceof Group) {
				visit((Context<C, M>) (Group<C, M>)cmd);
			} else
				cmd.accept(this);
		}
	}

	
	public static class CommandVisitor<C, M> extends Visitor<C, M> {
		
		@Override
		public void visit(Group<C, M> cmdGrp) {
			visit((Command<C, M>) cmdGrp);
		}
		
		@Override
		public void visit(Context<C, M> cmd) {
			cmd.visit_commands(this);
		}
		
		public void visit_commands(Command<C, M> cmd) {
			if (cmd instanceof Group) {
				visit((Context<C, M>) (Group<C, M>)cmd);
			} else
				cmd.accept(this);
		}
	}
	
	@SuppressWarnings("serial")
	public static class FoundCommand extends RuntimeException {
		public Command<Class<?>, Member> cmd;

		public FoundCommand(Command<Class<?>, Member> cmd) {
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
