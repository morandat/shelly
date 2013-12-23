package fr.labri.shelly;

import java.lang.annotation.Annotation;

public interface Visitor<C, M> {

	void visit(Item<C, M> item);
	void visit(Composite<C, M> optionGroup);
	void visit(Option<C, M> option);
	void visit(Command<C, M> cmd);
	void visit(Group<C, M> cmdGroup);
	void visit(Context<C, M> optionGroup);
	void visit(Terminal<C, M> option);
	void visit(Action<C, M> option);

	void startVisit(Option<C, M> option);
	void startVisit(Command<C, M> cmd);
	void startVisit(Group<C, M> cmdGroup);
	void startVisit(Context<C, M> optionGroup);
	void startVisit(Composite<C, M> cmp);
	
	public class VisitorAdapter<C, M> implements Visitor<C, M> {
		@Override
		public void visit(Item<C, M> item) {
		}
		
		public void visit_parent(Item<C, M> item) {
			Item<C, M> p = item.getParent();
			if(p != null)
				p.accept(this);
		}
		
		public void visit_options(Item<C, M> comp) {
			VisitorAdapter<C, M> option_visitor = new VisitorAdapter<C, M>() {
				@Override
				public void visit(Option<C, M> option ) {
					VisitorAdapter.this.visit(option);
				}
				@Override
				public void visit(Composite<C, M> cmp ) {
					cmp.visit_all(this);
				}
			};
			comp.startVisit(option_visitor);
		}
		public void visit_actions(Composite<C, M> comp ) {
			VisitorAdapter<C, M> action_visitor = new VisitorAdapter<C, M>() {
				@Override
				public void visit(Action<C, M> option) {
					VisitorAdapter.this.visit(option);
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
		@Override
		public void visit(Option<C, M> item ) {
			visit((Terminal<C, M>)item);
		}
		
		@Override
		public void visit(Command<C, M> item ) {
			visit((Terminal<C, M>)item);
		}
		
		@Override
		public void visit(Terminal<C, M> item ) {
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
	}
	
	public class TraversalVisitor<C, M> extends VisitorAdapter<C, M> {
		@Override
		public void visit(Composite<C, M> item) {
			item.visit_all(this);
		}
		@Override
		public void visit(Group<C, M> item) {
			visit((Composite<C, M>)item);
		}
	}
	public class ParentVisitor<C, M> extends VisitorAdapter<C, M> {
		@Override
		public void visit(Item<C, M> item) {
			visit_parent(item);
		}
		
	}	
	public class OptionVisitor<C, M> extends VisitorAdapter<C, M> {
		@Override
		public void visit(Group<C, M> item ) {
		}
		@Override
		public void visit(Terminal<C, M> item ) {
			visit_parent(item);
		}

		@Override
		public void visit(Composite<C, M> item ) {
			visit_options(item);
			visit_parent(item);
		}
		
		@Override
		public void startVisit(Composite<C, M> item ) {
			item.visit_all(this);
		}
		
		@Override
		public void startVisit(Command<C, M> item ) {
			item.getParent().accept(this);
		}
		
		@Override
		public void startVisit(Option<C, M> item ) {
			item.getParent().accept(this);
		}
	}

	public class ActionVisitor<C, M> extends VisitorAdapter<C, M> {
		
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
	public class FoundCommand extends RuntimeException {
		public Action<?, ?> cmd;

		public FoundCommand(Action<?,?> cmd) {
			this.cmd = cmd;
		}
	}
	
	@SuppressWarnings("serial")
	public class FoundOption extends RuntimeException {
		public Option<?, ?> opt;

		public FoundOption(Option<?, ?> opt) {
			this.opt = opt;
		}
	}
	
	@SuppressWarnings("serial")
	public class FoundAnnotation extends RuntimeException {
		public Annotation annotation;

		public FoundAnnotation(Annotation a) {
			this.annotation = a;
		}
	}
}
