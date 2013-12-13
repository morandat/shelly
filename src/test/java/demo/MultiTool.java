package demo;

import fr.labri.shelly.annotations.Option;

public class MultiTool {

	public static void main(String[] args) {
//		Shell shell = Shell.createComposite("multitool", new Class<?>[] {SimpleProject.class, DoctorProject.class});
//		shell.parseCommandLine(args);
	}
	
	public static class GeneralOptions {
		@Option
		boolean verbose;
	}
}
