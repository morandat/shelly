package fr.labri.shelly.impl;

import java.util.ArrayList;

import fr.labri.shelly.Command;
import fr.labri.shelly.CommandGroup;
import fr.labri.shelly.OptionGroup;
import fr.labri.shelly.Option;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.ShellyItem;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import fr.labri.shelly.impl.Visitor.OptionVisitor;

public class HelpHelper {
	
	public static void printHelp(Shell shell) {
		printHelp(shell.getGroup());
	}
	
	public static void printHelp(CommandGroup grp) {
		System.out.println("Options:");
		for (String[] help : new HelpOptionVisitor().getHelp(grp))
			System.out.print(formater.format(help));

		System.out.println("Commands:");
		for (String[] help : new HelpCommandVisitor().getHelp(grp))
			System.out.print(formater.format(help));
	}

	public static void printHelp(Command cmd) {
		System.out.println("Options:");
		for (String[] help : new HelpAcceptedOptionVisitor().getHelp(cmd))
			System.out.printf("\t%s:\t%s\n", help[0], help[1]);

		String help[] = cmd.getHelpString();
		System.out.println("Description: " + help[0]);
		System.out.println(help[1]);

	}
	
	
	interface HelpFormater {
		String format(String helpText[]);
	}

	static final HelpFormater formater = new HelpFormater() {
		@Override
		public String format(String helpText[]) {
			assert(helpText != null && helpText.length == 2);
			return String.format("\t%s:\t%s\n", helpText[0], helpText[1]);
		}
	};
	
	static class HelpAcceptedOptionVisitor extends OptionVisitor {
		HelpVisitor help = new HelpVisitor();
		
		public void visit(Option opt) {
			help.addHelp(opt);
		}
		
		public String[][] getHelp(Command item) {
			item.accept(this);
			return help.getHelp(this, item);
		}
	}
	
	static class HelpOptionVisitor extends Visitor {
		HelpVisitor help = new HelpVisitor();
		
		@Override
		public void visit(OptionGroup grp) {
			grp.visit_options(this);
		}
		
		@Override
		public void visit(CommandGroup grp) {
		}

		@Override
		public void visit(Option opt) {
			help.addHelp(opt);
		}
		
		public String[][] getHelp(CommandGroup item) {
			visit(item);
			return help.getHelp(this, item);
		}
	}

	static class HelpCommandVisitor extends CommandVisitor {
		HelpVisitor help = new HelpVisitor();

		@Override
		public void visit(Command cmd) {
			help.addHelp(cmd);
		}

		public String[][] getHelp(CommandGroup item) {
			this.visit((fr.labri.shelly.impl.OptionGroup)item);
			return help.getHelp(this, item);
		}
	}

	static class HelpVisitor {
		ArrayList<String[]> help = new ArrayList<>();

		void addHelp(String name, String desc) {
			addHelp(new String[]{name, desc});
		}
		
		void addHelp(ShellyDescriptable item) {
			addHelp(item.getHelpString());
		}
		void addHelp(String[] strings) {
			if(strings != null)
				help.add(strings);
		}

		String[][] getHelp(Visitor visitor, ShellyItem item) {
			String[][] res = new String[help.size()][];
			help.toArray(res);
			return res;
		}
	}
}
