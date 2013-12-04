package fr.labri.shelly.impl;

import fr.labri.shelly.Command;

public abstract class CommandGroup extends OptionGroup implements Command {
	CommandGroup(Class<?> clazz) {
		super(clazz);
	}
}
