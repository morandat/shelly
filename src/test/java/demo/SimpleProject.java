package demo;

import java.awt.Color;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.labri.shelly.annotations.*;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.impl.Converters.SimpleConverter;
import fr.labri.shelly.impl.HelpFactory;
import fr.labri.shelly.impl.ModelUtil;
import fr.labri.shelly.CommandLine;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Recognizer;
import fr.labri.shelly.Shell;
import fr.labri.shelly.Shelly;

@Group(name = "git")
public class SimpleProject {

	@Option(name = "verbose")
	public String verbose;

	@Option
	public boolean debug = true;

	@Option
	public String[] include;

	@Option
	public Map<String,Integer> levels;

	@Option
	public List<String> exclude;

	@Option
	public Set<String> signals;

	int level = 0;

	@Option(summary = "an accessor exemple")
	public void setVirt(int val) {
		level = val;
		System.err.println(val);
	}
	
	@Option
	public void map(Boolean val, String key) {
		System.err.println(key + " => " + val);
	}


	@Command
	@Description(summary = "a another short ex.")
	public void oldhelp(String[] cmds) {
		Shell shell = Shelly.createShell(SimpleProject.class);
		if (cmds.length == 0) {
			shell.printHelp(System.out);
		} else {
			fr.labri.shelly.Action<Class<?>, Member> parent = shell.getRoot();
			for (int i = 0; i < cmds.length; i++) {
				fr.labri.shelly.Action<Class<?>, Member> cmd = ModelUtil.findAction(parent, Recognizer.GNUNonStrict, cmds[i]);
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
		HelpFactory.NAVIGATOR.findTopic(Shelly.createShell(SimpleProject.class).getRoot(), Recognizer.GNUNonStrict, cmds);
	}

	@Command(factory = HelpFactory.Factory.class)
	public void help() {
		// This method is just a hook for annotation, it won't be called
		// Parameters type are inspected to determine help components
		// (HelpNavigator aNavigatorClassName, HelpFormater aFormaterClassName, HelpRenderer aRendererClassName)
		throw new RuntimeException("CommandFactory has not done its job.");
	}

	@Command(summary = "Some short text")
	@Description(url = "!echo.txt")
	public void echo(String data[]) {
		System.out.println("includes " +Arrays.toString(include));
		System.out.println("excludes " + exclude);
		System.out.println("signals " + signals);
		System.out.println("levels " + levels);
		
		System.out.println("options: " + debug);
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
			public void describe(@Param(value = "a value which may be a prefix", converter = MyFactory.class) String v1,
					@Param("another string, this time it's a suffix") String v2) {
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

	@Command(converter = MyFactory.class)
	public void color(Color c) {
		System.out.println(c);
	}

	@Command
	public void colorbis(@Param(converter = MyFactory.class) Color c) {
		System.out.println(c);
	}

	static public class MyFactory implements ConverterFactory {
		public Converter<?> getConverter(Class<?> type) {
			if (type.isAssignableFrom(Color.class))
				return new SimpleConverter<Color>() {
					@Override
					public Color convert(String value) {
						return new Color(253); //
					}
				};
			return null;
		}
	}

	@Error
	public void errorHandler(Exception e, String[] cmds) {
		// Error handler are hard coded to take an exception and a string[]
		System.err.println("erreur " + e);
		for (String c : cmds)
			System.err.println(c);
	}
	
	public static void main(String[] args) throws Exception {
		fr.labri.shelly.Group<Class<?>, Member> model = Shelly.createModel(SimpleProject.class);
		if (args.length == 0)
			new Shell(Recognizer.GNUNonStrict, model).loop(System.in);
		else
			new CommandLine(Recognizer.GNUNonStrict, model).parseCommandLine(args);
	}
}
