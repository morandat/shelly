package fr.labri.shelly.impl;

import java.io.IOException;

import demo.SimpleProject;
import fr.labri.shelly.BasicTests;
import fr.labri.shelly.Group;
import fr.labri.shelly.PrintVisitor;
import fr.labri.shelly.Shell;

public class Test {
	public static void main(String[] args) throws IOException {
//		ModelFactory f = new ModelFactory();
//		Group grp = f.createModel(SimpleProject.class);
		Shell.createShell(SimpleProject.class).loop(System.in);
//		Shell.printHelp(SimpleProject.class);
		
//		new PrintVisitor().print(grp);
//		BasicTests.testLevel();
	}
}
