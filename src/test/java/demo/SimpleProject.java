package demo;

import java.awt.Color;
import java.util.Arrays;

import fr.labri.shelly.annotations.*;
import fr.labri.shelly.impl.ConverterFactory;
import fr.labri.shelly.impl.HelpHelper;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Shell;

@Group(name = "git")
public class SimpleProject {

	@Option(name = "verbose")
	public String verbose;

	@Option
	public void setVirt(int val) {
		System.err.println("42");
	}

	@Default
	@Command
	public void help(String[] cmds) {
		Shell shell = Shell.createShell(SimpleProject.class);
		if (cmds.length == 0) {
			shell.printHelp();
		} else {
			fr.labri.shelly.Command parent = shell.getGroup();
			for (int i = 0; i < cmds.length; i++) {
				fr.labri.shelly.Command cmd = Shell.find_command(parent, cmds[i]);
				if (cmd == null) {
					System.out.println("No topic " + cmds[i]);
					break;
				} else {
					parent = cmd;
				}
			}
			if (parent instanceof fr.labri.shelly.Group)
				HelpHelper.printHelp((fr.labri.shelly.Group) parent);
			else
				HelpHelper.printHelp(parent);
		}
	}

	@Command
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
			public void describe(String v1, String v2) {
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
}
