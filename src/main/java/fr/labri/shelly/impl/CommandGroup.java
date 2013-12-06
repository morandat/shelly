package fr.labri.shelly.impl;

import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.Visitor;

public class CommandGroup extends OptionGroup implements fr.labri.shelly.CommandGroup, ShellyDescriptable {
	public CommandGroup(OptionGroup parent, String name, Class<?> clazz) {
		super(parent, name, clazz);
	}
	
	@Override
	public void accept(Visitor visitor) {
		visitor.visit((CommandGroup)this);
	}

	@Override
	public String[] getHelpString() {
		return new String[]{_id, "no description" };
	}
}
