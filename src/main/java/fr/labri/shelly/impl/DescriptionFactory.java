package fr.labri.shelly.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import fr.labri.shelly.Command;
import fr.labri.shelly.Description;
import fr.labri.shelly.Group;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Option;
import fr.labri.shelly.annotations.Param;
import fr.labri.shelly.impl.Visitor.CommandVisitor;
import static fr.labri.shelly.annotations.AnnotationUtils.*;

public class DescriptionFactory {

	public static Description getDescription(AnnotatedElement elt, String shortDesc) {
		fr.labri.shelly.annotations.Description a = elt.getAnnotation(fr.labri.shelly.annotations.Description.class);
		String longDesc = shortDesc;
		if (a != null) {
			shortDesc = getName(a.summary(), shortDesc);
			longDesc = getName(a.value(), shortDesc);
			longDesc = getName(a.url(), longDesc);
		}
		
		if (shortDesc.equals(Option.NO_NAME) && longDesc.equals(Option.NO_NAME))
			return Description.NO_DESCRIPTION;
		
		if (a != null && !a.url().equals(Option.NO_NAME)) {
			if(longDesc.startsWith("!"))
				return new RessourceDescription(shortDesc, longDesc.substring(1));
			else
				return new URLDescription(shortDesc, longDesc);
		}
		return new TextDescription(shortDesc, longDesc) ;
	}
	
	static class TextDescription implements Description {
		final private String _short;
		final private String _long;
		public TextDescription(String sh, String lg) {
			_short = sh;
			_long = lg;
		}

		@Override
		public String getShortDescription() {
			return _short;
		}

		@Override
		public String getLongDescription() {
			return _long;
		}

		@Override
		public String[][] getDescription() {
			return null;
		}
	}
	
	static class RessourceDescription extends URLDescription {
		public RessourceDescription(String summary, String uri) {
			super(summary, uri);
		}
	
		InputStream openRessource(String uri) throws IOException {
			InputStream is = DescriptionFactory.class.getClassLoader().getResourceAsStream(uri);
			if(is == null) throw new IOException();
			return is;
		}
	}
	
	static class URLDescription implements Description {
		private String _uri;
		private String _summary;
		public URLDescription(String summary, String uri) {
			_uri = uri;
			_summary = summary;
		}
		@Override
		public String getShortDescription() {
			return _summary;
		}
		@Override
		public String[][] getDescription() {
			return null;
		}

		@Override
		public String getLongDescription() {
			InputStream res = null;
			String text = _summary;
			try {
				res = openRessource(_uri);
				text = readRessource(res);
			} catch (IOException e) {
			} finally {
				try {
					if(res != null)
						res.close();
				} catch (IOException e) {
				}
			}
			return text;
		}
		
		protected String readRessource(InputStream res) {
			Scanner s = new Scanner(res);
			s.useDelimiter("\\A");
			String str = s.next();
			s.close();
			return str;
		}
		
		InputStream openRessource(String uri) throws IOException {
			return new URL(uri).openStream();
		}
		
	}

	static String[][] describeParamaters(Method method ) {
		ArrayList<String[]> list = new ArrayList<String[]>();
		Annotation[][] pa = method.getParameterAnnotations();
		Param a;
		int i = 0;
		
		for(Class<?> t: method.getParameterTypes()) {
			if((a = AnnotationUtils.getAnnotation(pa[i++], Param.class)) != null && !a.value().equals(Option.NO_NAME))
				list.add(new String[]{t.getSimpleName().toLowerCase() , a.value()});
			else
				list.add(new String[]{t.getSimpleName().toLowerCase(), t.getSimpleName()});
		}
		
		final String[][] res = new String[list.size()][]; 
		return list.toArray(res);
	}
	
	public static Description getCommandDescription(final Method method, String shortDesc) {
		return Description.ExtraDescription.getExtraDescription(getDescription(method, shortDesc), describeParamaters(method));			
	}
	
	public static Description getGroupDescription(final Group<Class<?>, Member> grp, String shortDesc) {
		return Description.ExtraDescription.getExtraDescription(getDescription(grp.getAssociatedElement(), shortDesc), describeSubCommands(grp));			
	}
	
	static String[][] describeSubCommands(Group<Class<?>, Member> grp) {
		final ArrayList<String[]> list = new ArrayList<String[]>();
		new CommandVisitor<Class<?>, Member>() {
			public void visit(Command<Class<?>, Member> cmd) {
				list.add(new String[]{cmd.getID(), cmd.getDescription().getShortDescription()});
			}
		}.visit_commands(grp);
		final String[][] res = new String[list.size()][]; 
		return list.toArray(res);
	}
}
