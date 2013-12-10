package fr.labri.shelly.impl;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import fr.labri.shelly.Command;
import fr.labri.shelly.Context;
import fr.labri.shelly.Converter;
import fr.labri.shelly.ConverterFactory;
import fr.labri.shelly.Description;
import fr.labri.shelly.Option;
import fr.labri.shelly.Shell;
import fr.labri.shelly.Group;
import fr.labri.shelly.ShellyDescriptable;
import fr.labri.shelly.annotations.Default;
import fr.labri.shelly.impl.ModelFactory.AbstractModelFactory;
import fr.labri.shelly.impl.ModelFactory.CommandAdapter;

public class HelpFactory {
	static public class CommandFactory extends AbstractModelFactory {
		public CommandFactory(ModelFactory parent) {
			super(parent);
		}

		@Override
		public Command newCommand(ConverterFactory converterFactory, Context parent, String name, final Method method) {
			final Converter<?>[] converters = fr.labri.shelly.impl.ConverterFactory.getConverters(converterFactory, String.class);
			HelpNavigator navigator = NAVIGATOR;
			HelpFormater formater = FORMATER;
			HelpRenderer renderer = RENDERER;
			for(Class<?> t: method.getParameterTypes()) {
				try {
					navigator = t.isAssignableFrom(HelpNavigator.class) ? (HelpNavigator) t.newInstance() : navigator;
					formater = t.isAssignableFrom(HelpFormater.class) ? (HelpFormater) t.newInstance() : formater;
					renderer = t.isAssignableFrom(HelpRenderer.class) ? (HelpRenderer) t.newInstance() : renderer;
				} catch (InstantiationException | IllegalAccessException e) {
				}
			}
			return newCommand(name, parent, converters, getHelpCommandAdapter(converters, method.isAnnotationPresent(Default.class), NAVIGATOR, FORMATER, RENDERER) );
		}
		
	}

	public interface HelpNavigator {
		public abstract ShellyDescriptable printHelp(Context context, String[] cmds);
	}

	public interface HelpRenderer {
		public abstract Help getHelp(ShellyDescriptable item);
	}

	public interface HelpFormater {
		String renderHelp(Help helpText);

		void renderHelp(PrintStream out, Help helpText);
	}

	public static void printHelp(ShellyDescriptable item, PrintStream out) {
		printHelp(item, out, FORMATER, RENDERER);
	}

	public static void printHelp(ShellyDescriptable item, PrintStream out, HelpFormater formater, HelpRenderer renderer) {
		formater.renderHelp(out, renderer.getHelp(item));
	}

	static public Command getHelpCommand(Context parent) {
		return getHelpCommand(parent, "help", true, fr.labri.shelly.impl.ConverterFactory.DEFAULT);
	}

	static public Command getHelpCommand(Context parent, final String name, final boolean defaultcmd, ConverterFactory factory) {
		return getHelpCommand(parent, name, defaultcmd, factory, NAVIGATOR, FORMATER, RENDERER);
	}

	static public Command getHelpCommand(Context parent, final String name, final boolean defaultcmd, ConverterFactory factory, final HelpNavigator navigator,
			final HelpFormater formater, final HelpRenderer renderer) {
		final Converter<?>[] converters = fr.labri.shelly.impl.ConverterFactory.getConverters(factory, String.class);
		return ModelFactory.EXECUTABLE_MODEL.newCommand(name, parent, converters, getHelpCommandAdapter(converters, defaultcmd, navigator, formater, renderer));
	}

	private static CommandAdapter getHelpCommandAdapter(final Converter<?>[] converters, final boolean isDefault, final HelpNavigator navigator, final HelpFormater formater, final HelpRenderer renderer) {
		return new CommandAdapter() {
			@Override
			public Object apply(AbstractCommand cmd, Object receive, String next, PeekIterator<String> cmdline) {
				String[] args = (String[]) fr.labri.shelly.impl.ConverterFactory.convertArray(converters, next, cmdline)[0]; // FIXME
																																	// not
				ShellyDescriptable item = navigator.printHelp(cmd.getParent(), args);
				printHelp(item, System.err, formater, renderer);
				return null;
			}

			@Override
			public boolean isDefault() {
				return isDefault;
			}

			@Override
			public Description getDescription() {
				return HELP_DESCRIPTION;
			}
		};
	}

	static final HelpFormater FORMATER = new SimpleFormater<Integer>() {

		public String format(String helpText[], Integer layout) {
			switch (helpText.length) {
			case 0:
				return "";
			case 1:
				return String.format("%s\n", helpText[0]);
			case 2:
				int i = layout - helpText[0].length() + 1;
				String s = new String(new char[i]).replace("\0", " ");
				return String.format("%s%s%s\n", helpText[0], s, helpText[1]);
			default:
				return join(helpText);
			}
		}

		public String join(String[] strs) {
			StringBuilder sb = new StringBuilder();
			for (String s : strs)
				sb.append(s);
			sb.append("\n");
			return sb.toString();
		}

		@Override
		Integer makeLayout(Help helpText) {
			int i = 0;
			for (String[] l : helpText)
				if (l.length == 2) {
					int len = l[0].length();
					i = (len > i) ? len : i;
				}
			return i;
		}
	};

	static final HelpRenderer RENDERER = new HelpRenderer() {
		@Override
		public Help getHelp(ShellyDescriptable item) {
			final Help help = new Help();
			item.accept(new Visitor() {
				public void visit(Group grp) {
					Description d =  grp.getDescription();
					help.addTitle("Description");
					help.addLongHelp(d);
					help.skipLine();

					help.addTitle("Commands");
					help.addHelp(d.getDescription());
					help.skipLine();

					help.addTitle("Options");
					new OptionVisitor() {
						public void visit(Option option) {
							help.addShortHelp(option);
						}
					}.visit_options(grp);
				}

				public void visit(Command cmd) {
					Description d = cmd.getDescription();
					help.addTitle("Description");
					help.addLongHelp(d);
					help.skipLine();
					
					help.addTitle("Parameters");
					help.addHelp(d.getDescription());
					help.skipLine();

					help.addTitle("Option");
					new OptionVisitor() {
						public void visit(Option option) {
							help.addShortHelp(option);
						}
					}.visit_options(cmd);
				}

				public void visit(Option opt) {
					help.addTitle("Description");
					help.addLongHelp(opt);
				}
			});
			return help;
		}
	};

	static abstract class SimpleFormater<L> implements HelpFormater {
		@Override
		public String renderHelp(Help helpText) {
			L layout = makeLayout(helpText);
			StringBuilder sb = new StringBuilder();
			for (String[] l : helpText)
				sb.append(format(l, layout));
			return sb.toString();
		}

		@Override
		public void renderHelp(PrintStream out, Help helpText) {
			L layout = makeLayout(helpText);
			for (String[] l : helpText)
				out.print(format(l, layout));
		}

		abstract L makeLayout(Help helpText);

		public abstract String format(String helpText[], L layout);
	}

	public static final HelpNavigator NAVIGATOR = new HelpNavigator() {
		@Override
		public ShellyDescriptable printHelp(Context Context, String[] cmds) {
			if (cmds.length == 0) {
				return (Group) Context;
			} else {
				Command parent = (Group) Context;
				for (int i = 0; i < cmds.length; i++) {
					Command cmd = Shell.find_command(parent, cmds[i]);
					if (cmd == null) {
						System.out.println("No topic " + cmds[i]);
						break;
					} else {
						parent = cmd;
					}
				}
				return parent;
			}
		}
	};
	static final Description HELP_DESCRIPTION = new Description() {
		@Override
		public String getShortDescription() {
			return "Give an help message";
		}
		
		@Override
		public String getLongDescription() {
			return getShortDescription();
		}

		@Override
		public String[][] getDescription() {
			return null;
		}
	};
	static class Help implements Iterable<String[]> {
		static final String BLANK_LINE = "";
		
		ArrayList<String[]> help = new ArrayList<>();

		void addHelp(String line) {
			addHelp(new String[] { line });
		}
		public void skipLine() {
			addHelp(BLANK_LINE);
		}
		public void addLongHelp(Description d) {
			addHelp(d.getLongDescription());
			
		}
		void addHelp(String[][] strings) {
			for(String[] s: strings)
				addHelp(s);
		}
		void addHelp(String name, String desc) {
			addHelp(new String[] { name, desc });
		}
		
		void addTitle(String title) {
			addHelp(title);
			String s = new String(new char[title.length()]).replace("\0", "-");
			addHelp(s);
		}

		void addLongHelp(ShellyDescriptable item) {
			addLongHelp(item.getDescription());
		}

		void addShortHelp(ShellyDescriptable item) {
			addHelp(item.getID(), item.getDescription().getLongDescription());
		}

		void addShortHelp(String id, Description description) {
			addHelp(id, description.getShortDescription());
		}

		private void addHelp(String[] strings) {
			if (strings != null)
				help.add(strings);
		}

		@Override
		public Iterator<String[]> iterator() {
			return help.iterator();
		}
	}
}