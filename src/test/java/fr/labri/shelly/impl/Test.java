package fr.labri.shelly.impl;

import java.io.IOException;

import demo.SimpleProject;
import fr.labri.shelly.Shell;

public class Test {
	public static void main(String[] args) throws Exception {
		
		if(Test.class.getClassLoader().getResourceAsStream("echo.txt") == null)
			throw new IOException();
		Shell shell = Shell.createShell(SimpleProject.class);
//		shell.getRoot().addCommand(HelpFactory.getHelpCommand(shell.getRoot()));
		shell.loop(System.in);
	}
}
