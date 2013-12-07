package demo;

import java.awt.Color;

import fr.labri.shelly.annotations.*;
import fr.labri.shelly.impl.ConverterFactory;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Shell;

@Group(name = "git")
public class SimpleProject {

	@Option(name = "verbose")
	public String verbose;

	@Command(name = "help")
	public void help() {
		System.out.println("Contexte: " + verbose);
		Shell.printHelp(SimpleProject.class);
	}

	@Context
	public class HelpCmds {
		@Option(name = "verbose")
		public String verbose;

		@Option
		public int level;

		@Context
		public class HelpCmds2 {

			@Option
			public String truc;

			@Command(name = "describe")
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
		@Command
		public void list(){};

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

	@Command(name = "color", factory = MyFactory.class)
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
