package fr.labri.shelly;

import org.testng.annotations.Test;

import demo.SimpleProject;
import fr.labri.shelly.Shell;

public class BasicTests {
		@Test
		public void testCreate() {
			Shell.printHelp(SimpleProject.class);
		}
		
		@Test
		static public void testParse() {
			Shell shell = Shell.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"help"});
		}

		@Test
		static public void testLevel() {
			Shell shell = Shell.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"branch", "take"});
			shell.parseCommandLine(new String[]{"--verbose", "ext", "branch", "--format", "42", "take", "--user", "user", "--pass", "pass"});
			shell.parseCommandLine(new String[]{"--verbose", "ext", "describe", "--level", "42", "--verbose", "int", "yo", "man"});
		}

		@Test
		public void testParse2() {
			Shell shell = Shell.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"describe", "text1", "text2"});
		}
		
		@Test
		public void testParse3() {
			Shell shell = Shell.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"describe", "--verbose", "valeur de verbose", "text1", "text2"});
		}
		
		@Test
		public void testParse4() {
			Shell shell = Shell.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"color", "text2"});
		}

}
