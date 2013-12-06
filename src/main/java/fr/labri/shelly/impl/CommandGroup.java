package fr.labri.shelly.impl;

import fr.labri.shelly.Visitor;

public abstract class CommandGroup extends OptionGroup implements fr.labri.shelly.CommandGroup {
	CommandGroup(OptionGroup parent, String name, Class<?> clazz) {
		super(parent, name, clazz);
	}
	
	@Override
	public void accept(Visitor visitor) {
		visitor.visit((CommandGroup)this);
	}
}
