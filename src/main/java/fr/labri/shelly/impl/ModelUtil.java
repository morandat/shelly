package fr.labri.shelly.impl;

import fr.labri.shelly.Action;
import fr.labri.shelly.Composite;
import fr.labri.shelly.Group;
import fr.labri.shelly.Item;
import fr.labri.shelly.Option;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.Visitor.*;

public class ModelUtil {

	@SuppressWarnings("unchecked")
	public static <C,M> Action<C, M> findAction(Action<C, M> start, final Recognizer parser, final String cmd) {
		try {
			VisitorAdapter<C, M> v = new VisitorAdapter.ActionVisitor<C, M>() {
				@Override
				public void visit(Action<C, M> grp) {
					if (parser.isActionValid(cmd, grp) >= 0) {
						throw new VisitorAdapter.FoundCommand(grp);
					}
				}
			};
			start.startVisit(v);
		} catch (VisitorAdapter.FoundCommand e) {
			return (Action<C, M>) e.cmd;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <C,M> Group<C, M> findGroup(Item<C, M> start) {
		try {
		start.accept(new VisitorAdapter<C, M>() {
			@Override
			public void visit(Item<C, M> i) {
				visit_parent(i);
			}
			@Override
			public void visit(Group<C, M> cmdGroup) {
				throw new FoundCommand(cmdGroup);
			}
		});
		} catch (FoundCommand e) {
			return (Group<C, M>)e.cmd;
		}
		return null; 
	}
	
	@SuppressWarnings("unchecked")
	static public <C,M> Option<C, M> findOption(Action<C, M> start, final Recognizer parser, final String cmd) {
		try {
				OptionVisitor<C, M> v = new OptionVisitor<C, M>() {
					@Override
					public void visit(Option<C, M> option) {
						if (parser.isLongOptionValid(cmd, option) >= 0)
							throw new FoundOption(option);
					};
				};
				start.startVisit(v);
		} catch (FoundOption e) {
			return (Option<C, M>) e.opt;
		}
		return null;
	}
	
	public static <C,M> Group<C, M> findRoot(Composite<C, M> parent) {
		Composite<C, M> last = parent;
		while(parent != null)
			parent = (last = parent).getParent();
		assert last instanceof Group;
		return (Group<C, M>)last;
	}
}
