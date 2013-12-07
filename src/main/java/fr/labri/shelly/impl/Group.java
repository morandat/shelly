package fr.labri.shelly.impl;

import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.Visitor;

public class Group extends Context implements fr.labri.shelly.Group, ShellyDescriptable {
	public Group(Context parent, String name, Class<?> clazz) {
		super(parent, name, clazz);
	}
	
	@Override
	public void accept(Visitor visitor) {
		visitor.visit((Group)this);
	}

	@Override
	public String[] getHelpString() {
		return new String[]{_id, "no description" };
	}
}
