package demo;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import fr.labri.shelly.Shell;

public class BasicTests {
		@Test
		public void testCreate() {
			Shell.printHelp(SimpleProject.class);
		}
		
		@Test
		public void testParse() {
			Shell shell = Shell.createShell(SimpleProject.class);
			shell.parseCommandLine(new String[]{"help"});
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
			shell.parseCommandLine(new String[]{"colot", "text2"});
		}

}
