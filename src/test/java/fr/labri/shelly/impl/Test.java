package fr.labri.shelly.impl;

import java.io.IOException;

import demo.SimpleProject;
import fr.labri.shelly.Shell;

public class Test {
	public static void main(String[] args) throws IOException {
		Shell shell = Shell.createShell(SimpleProject.class);
		shell.getGroup().addCommand(HelpFactory.helpCommand(shell.getGroup(), "help", true));
		shell.loop(System.in);
	}
}
