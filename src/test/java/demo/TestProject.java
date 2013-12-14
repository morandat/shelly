package demo;

import fr.labri.shelly.annotations.*;

@Group
public class TestProject {
	@Option boolean verbose;
	@Option int level;
	@Command void test1(int nb) {
		System.out.printf("%b %d -- %d\n");
	}
}
