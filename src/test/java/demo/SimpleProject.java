package demo;

import java.awt.Color;

import fr.labri.shelly.annotations.*;
import fr.labri.shelly.impl.ConverterFactory;
import fr.labri.shelly.Converter;
import fr.labri.shelly.Shell;

@CommandGroup(name = "git")
public class SimpleProject {
	@Option(name = "verbose")
	public String verbose;

	@Command(name = "help")
	public void help() {
		Shell.printHelp(SimpleProject.class);
	}

	@Command(name = "describe")
	public void describe(String v1, String v2) {
		System.out.println("Contexte: "+ verbose);
		System.out.println("Value is: " + v1 + "/" + v2);
	}
	
	@Command(name = "color", factory = demo.SimpleProject.MyFactory.class)
	public void color(Color c) {
		System.out.println(c);
	}
	
	static public class MyFactory extends ConverterFactory {
		public Converter<?> getObjectConverter(Class<?> type) {
			if(type.isAssignableFrom(Color.class))
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
