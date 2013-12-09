package demo;

import java.awt.Color;
import java.util.Arrays;

import fr.labri.shelly.annotations.*;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.impl.ConverterFactory;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Shell;

@Group(name = "git")
public class SimpleProject {

	@Option(name = "verbose")
	public String verbose;

	int level = 0;

	@Option(summary = "an accessor exemple")
	public void setVirt(int val) {
		level = val;
		System.err.println(val);
	}

	@Command
	@Description(summary = "a another short ex.")
	public void oldhelp(String[] cmds) {
		Shell shell = Shell.createShell(SimpleProject.class);
		if (cmds.length == 0) {
			shell.printHelp(System.out);
		} else {
			fr.labri.shelly.Command parent = shell.getRoot();
			for (int i = 0; i < cmds.length; i++) {
				fr.labri.shelly.Command cmd = Shell.find_command(parent, cmds[i]);
				if (cmd == null) {
					System.out.println("No topic " + cmds[i]);
					break;
				} else {
					parent = cmd;
				}
			}
			HelpFactory.printHelp(parent, System.out);
		}
	}

	@Command(summary = "short way to go")
	@Description("A more long way to go !")
	public void newoldhelp(String[] cmds) {
		HelpFactory.NAVIGATOR.printHelp(Shell.createShell(SimpleProject.class).getRoot(), cmds);
	}

	@Command(summary = "Some short text")
	@Description(url = "!echo.txt")
	public void echo(String data[]) {
		System.out.println(Arrays.toString(data));
	}

	@Command
	public void ints(Integer data[]) {
		System.out.println(Arrays.toString(data));
	}

	@Context
	public class HelpCmds {
		@Option
		public String verbose;

		@Option
		public int level;

		@Context
		public class HelpCmds2 {

			@Option
			public String truc;

			@Command
			public void describe(@Param(value="v1", factory = MyFactory.class) String v1, @Param("v2") String v2) {
				System.out.println("verbose(in): " + verbose);
				System.out.println("level: " + level);
				System.out.println("verbose(out):" + SimpleProject.this.verbose);
				System.out.println("Value is: " + v1 + "/" + v2);
			}

		}

		public void describe(String v1) {
		}
	}

	@Group
	public class Branch {
		@Option
		public int format;

		@Default
		@Command
		public void list() {
			System.out.println("default !!!!!");
		}

		@Context
		public class PasswordInfo {
			@Option
			public String pass;

			@Context
			public class UserInfo {
				@Option
				public String user;

				@Command
				public void take() {
					System.out.printf("u %s p %s, f %d v %s\n", user, pass, format, verbose);
				};
			}
		}
	}

	@Command(factory = MyFactory.class)
	public void color(Color c) {
		System.out.println(c);
	}

	static public class MyFactory extends ConverterFactory {
		public Converter<?> getObjectConverter(Class<?> type) {
			if (type.isAssignableFrom(Color.class))
				return new SimpleConverter<Color>() {
					@Override
					public Color convert(String value) {
						return new Color(253); //
					}
				};
			return super.getObjectConverter(type);
		}
	}

	@Error
	void errorHandler(Exception e, String[] cmds) {
		System.err.println("erreur " + e);
		for (String c : cmds)
			System.err.println(c);
	}
}
