package demo;

import fr.labri.shelly.annotations.Command;
import fr.labri.shelly.annotations.Context;

public class DoctorProject {
	public DoctorProject() {
	}

	public DoctorProject(MultiTool.GeneralOptions options) {
	}

	class Test1 {
		@Command
		void help() {};
	}
	
	abstract class Test2 {
		@Command
		void help() {};
	}
	
	@Context
	class Test3 {
		@Command
		void help() {};
	}
}
