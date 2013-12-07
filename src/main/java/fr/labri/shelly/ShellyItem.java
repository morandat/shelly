package fr.labri.shelly;

public interface ShellyItem {
	public abstract String getID();
	public abstract Context getParent();

	public abstract boolean isValid(String str);

	public abstract void accept(Visitor visitor);
	public abstract void visit_all(Visitor visitor);
}
