package fr.labri.shelly.impl;


import demo.CpOptions;
import demo.TestStatic;
import fr.labri.shelly.Parser;
import fr.labri.shelly.Shell;

public class Test {
	public static void main(String[] args) throws Exception {
		Parser test = Parser.Java;
		
		Shell shell = Shell.createShell(TestStatic.class);
//		shell.getRoot().addCommand(HelpFactory.getHelpCommand(shell.getRoot()));
		shell.loop(System.in, test);
		
	}
}
