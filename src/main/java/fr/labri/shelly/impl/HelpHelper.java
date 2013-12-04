package fr.labri.shelly.impl;

import fr.labri.shelly.Command;
import fr.labri.shelly.Option;

public class HelpHelper {

	public static void printHelp(OptionGroup grp) {
		System.out.println("Options:");
		for(Option opt: grp.options)
			System.out.printf("\t%s\n", opt.toHelpString());
		
		System.out.println("Commands:");
		for(Command cmd: grp.commands)
			System.out.printf("\t%s\n", cmd.toHelpString());		
	}

}
