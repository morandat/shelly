package fr.labri.shelly;

import org.testng.annotations.Test;

import demo.SimpleProject;
import fr.labri.shelly.Shell;

public class BasicTests {
		@Test
		public void testCreate() {
			Shelly.createShell(SimpleProject.class).printHelp(System.out);;
		}
		
		@Test
		static public void testParse() {
			Shell shell = Shelly.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"help"});
		}

		@Test
		static public void testLevel() {
			Shell shell = Shelly.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"--virt", "42",  "branch", "take"});
			shell.parseCommandLine(new String[]{"--virt", "42",  "branch", "foo" });
			shell.parseCommandLine(new String[]{"--verbose", "ext", "branch", "--format", "42", "take", "--user", "user", "--pass", "pass"});
			shell.parseCommandLine(new String[]{"--verbose", "ext", "describe", "--level", "42", "--verbose", "int", "yo", "man"});
		}

		@Test
		public void testParse2() {
			Shell shell = Shelly.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"describe", "text1", "text2"});
		}
		
		@Test
		public void testParse3() {
			Shell shell = Shelly.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"describe", "--verbose", "valeur de verbose", "text1", "text2"});
		}
		
		@Test
		public void testParse4() {
			Shell shell = Shelly.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"color", "text2"});
		}

}
