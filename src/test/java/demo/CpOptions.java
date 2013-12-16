package demo;

import java.util.Arrays;

import fr.labri.shelly.annotations.Command;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.annotations.Group;
import fr.labri.shelly.annotations.Ignore;
import fr.labri.shelly.annotations.Option;

@Group
public class CpOptions {
	@Option public boolean force;
	@Option public Boolean verbose;
	
	@Default @Ignore @Command public void cp(String f1, String fs[]) {
		System.out.printf("Copying %s%s -> %s\n", force ? "force " :"",  f1, Arrays.toString(fs));
	}
}
