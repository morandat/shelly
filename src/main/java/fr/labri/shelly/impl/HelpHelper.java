package fr.labri.shelly.impl;

import java.util.ArrayList;

import fr.labri.shelly.Command;
import fr.labri.shelly.CommandGroup;
import fr.labri.shelly.Option;
import fr.labri.shelly.ShellyItem;

public class HelpHelper {

	public static void printHelp(OptionGroup grp) {
		System.out.println("Options:");
		for (String[] help : new HelpOptionVisitor().getHelp(grp))
			System.out.printf("\t%s:\t%s\n", help[0], help[1]);

		System.out.println("Commands:");
		for (String[] help : new HelpCommandVisitor().getHelp(grp))
			System.out.printf("\t%s:\t%s\n", help[0], help[1]);
	}

	public static void printHelp(Command cmd) {
		System.out.println("Options:");
		for (String[] help : new HelpOptionVisitor().getHelp(cmd))
			System.out.printf("\t%s:\t%s\n", help[0], help[1]);

		System.out.println("Description");
		String help[] = cmd.getHelpString();
		System.out.printf("\t%s:\t%s\n", help[0], help[1]);
	}

	
	static class HelpOptionVisitor extends HelpVisitor {
		@Override
		public void visit(Option opt) {
			help.add(opt.getHelpString());
		}

		@Override
		public void visit(CommandGroup cmdGrp) {
		}

		@Override
		public void visit(fr.labri.shelly.OptionGroup cmd) {
			cmd.visit_options(this);
			if(cmd.getParent() != null)
				visit(cmd.getParent());
		}
	}

	static class HelpCommandVisitor extends HelpVisitor {
		@Override
		public void visit(Command cmd) {
			help.add(cmd.getHelpString());
		}
		

		@Override
		public void visit(CommandGroup cmdGrp) {
			super.visit((Command) cmdGrp);
		}

		@Override
		public void visit(fr.labri.shelly.OptionGroup cmd) {
			cmd.visit_commands(this);
		}
	}

	static abstract class HelpVisitor extends Visitor {
		ArrayList<String[]> help = new ArrayList<>();

		String[][] getHelp(ShellyItem item) {
			item.accept(this);

			String[][] res = new String[help.size()][];
			help.toArray(res);
			return res;
		}
	}
}
