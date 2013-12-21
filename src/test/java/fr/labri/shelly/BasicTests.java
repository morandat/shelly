package fr.labri.shelly;

import org.testng.annotations.Test;

import demo.SimpleProject;

public class BasicTests {
		@Test
		public void testCreate() {
			Shelly.createShell(SimpleProject.class).printHelp(System.out);;
		}
		
		@Test
		static public void testParse() {
			CommandLine cmd = Shelly.createCommandLine(SimpleProject.class);
			cmd.parseCommandLine(new String[]{"help"});
		}

		@Test
		static public void testLevel() {
			CommandLine cmd = Shelly.createCommandLine(SimpleProject.class);
			cmd.parseCommandLine(new String[]{"--virt", "42",  "branch", "take"});
			cmd.parseCommandLine(new String[]{"--virt", "42",  "branch", "foo" });
			cmd.parseCommandLine(new String[]{"--verbose", "ext", "branch", "--format", "42", "take", "--user", "user", "--pass", "pass"});
			cmd.parseCommandLine(new String[]{"--verbose", "ext", "describe", "--level", "42", "--verbose", "int", "yo", "man"});
		}

		@Test
		public void testParse2() {
			CommandLine cmd = Shelly.createCommandLine(SimpleProject.class);
			cmd.parseCommandLine(new String[]{"describe", "text1", "text2"});
		}
		
		@Test
		public void testParse3() {
			CommandLine cmd = Shelly.createCommandLine(SimpleProject.class);
			cmd.parseCommandLine(new String[]{"describe", "--verbose", "valeur de verbose", "text1", "text2"});
		}
		
		@Test
		public void testParse4() {
			CommandLine cmd = Shelly.createCommandLine(SimpleProject.class);
			cmd.parseCommandLine(new String[]{"color", "text2"});
		}

}
