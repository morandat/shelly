package fr.labri.shelly.impl;


import demo.CpOptions;
import demo.TestStatic;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.Shell;
import fr.labri.shelly.Shelly;

public class Test {
	public static void main(String[] args) throws Exception {
		Recognizer test = Recognizer.Java;
		
		Shell shell = Shelly.createShell(test, TestStatic.class);
//		shell.getRoot().addCommand(HelpFactory.getHelpCommand(shell.getRoot()));
		shell.loop(System.in);
		
	}
}
