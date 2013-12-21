package demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import fr.labri.shelly.Shell;
import fr.labri.shelly.Shelly;
import fr.labri.shelly.annotations.*;

@Group
public class TestProject {
	@Option public boolean verbose;
	@Option public int level;
	@Command public void test1(int nb) {
		System.out.printf("%b %d -- %d\n", verbose, level, nb);
	}
	
	@Group public class Branch {
		@Command public void list(int a) {
			System.out.printf("list %b %d -- %d\n", verbose, level, a);
		}
		@Command public void add(int a) {
			System.out.printf("add %b %d -- %d\n", verbose, level, a);
		}
		@Command public void remove(int a) {
			System.out.printf("remove %b %d -- %d\n", verbose, level, a);
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		Shell shell = Shelly.createShell(TestProject.class);
		shell.loop(System.in, shell.getMultiLevelShell(new BufferedReader(new InputStreamReader(System.in))));
	}
}
