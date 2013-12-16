package demo;

import fr.labri.shelly.annotations.Command;
import fr.labri.shelly.annotations.Context;
import fr.labri.shelly.annotations.Group;
import fr.labri.shelly.annotations.Option;
import fr.labri.shelly.annotations.Error;

@Group
public class DoctorProject {
	public DoctorProject() {
	}

	@Command void testpackage () { }
	
	@Group
	public abstract class TestAbstract {
		
	}

	public DoctorProject(MultiTool.GeneralOptions options) {
	}

	@Error
	public void gooderror(Exception e, String args[]) { }
	@Error
	void errorvisbility(Exception e, String args[]) { }
	@Error
	void errorargs(String args[]) { }
	@Error
	void errorargs(Exception e, String s, String args[]) { }
	
	public class Test1 {
		@Command public void help() {};
	}
	
	public abstract class Test2 {
		@Command public void help() {};
	}

	public class TestWarn {
		@Command public void foo() {};
	}

	@Option public boolean test;
	@Option public boolean test2;

	@Context public class Test3 {
		@Option public boolean test;
		@Command public void help() {};
	}
	
	@Group public class Test {
		@Option public boolean test;
		
	}
	@Context public class Test5 {
		@Option public boolean test;
		@Command public void annother() {};
	}
	
	@Context public class Test4 {
		@Context public class Test4bis {
//			@Command public void help() {};
		}
	}
}
