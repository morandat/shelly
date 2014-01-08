package fr.labri.shelly;

import static fr.labri.shelly.annotations.AnnotationUtils.getName;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import fr.labri.shelly.Visitor.ActionVisitor;
import fr.labri.shelly.annotations.AnnotationUtils;
import fr.labri.shelly.annotations.Option;
import fr.labri.shelly.annotations.Param;
import fr.labri.shelly.annotations.Ignore.ExecutorMode;

public interface Description {
	String getShortDescription();
	String[][] getDescription();
	String getLongDescription();

	static final String NO_DESCRIPTION_TEXT = "No description";

	Description NO_DESCRIPTION = new Description() {
		@Override
		public String getShortDescription() {
			return NO_DESCRIPTION_TEXT;
		}
		
		@Override
		public String getLongDescription() {
			return NO_DESCRIPTION_TEXT;
		}

		@Override
		public String[][] getDescription() {
			return null;
		}
	};
	
	public abstract class ExtraDescription implements Description {
		Description _parent;
		public ExtraDescription(Description parent) {
			_parent = parent;
		}
		@Override
		public String getShortDescription() {
			return _parent.getShortDescription();
		}
		@Override
		public String getLongDescription() {
			return _parent.getLongDescription();
		}
		
		public static Description getExtraDescription(Description parent, final String[][] extra) {
			return new Description.ExtraDescription(parent) {
				@Override
				public String[][] getDescription() {
					return extra;
				}
				
			};
		}
	}
	
	static class Desc<M> {
		String shortDesc, longDesc, url;
		int type;
	}

	public class Factory {
		public static Description getDescription(fr.labri.shelly.annotations.Description a, String shortDesc) {
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

		public static <V> Description getDescription(V elt, String shortDesc, AnnotationUtils.AnnotationType<V> a) {
			return getDescription(a.getValue(elt, fr.labri.shelly.annotations.Description.class), shortDesc);
		}
		
		public static Description getDescription(AnnotatedElement elt, String shortDesc) {
			fr.labri.shelly.annotations.Description a = elt.getAnnotation(fr.labri.shelly.annotations.Description.class);
			return getDescription(a, shortDesc);
		}
		
		public static Description getDescription(Element elt, String shortDesc) {
			fr.labri.shelly.annotations.Description a = elt.getAnnotation(fr.labri.shelly.annotations.Description.class);
			return getDescription(a, shortDesc);
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
		
		public static Description getCommandDescription(Method method, String shortDesc) {
			return Description.ExtraDescription.getExtraDescription(getDescription(method, shortDesc), describeParamaters(method));			
		}

		public static <C, M> Description getGroupDescription(final Group<C, M> grp, Class<?> c, String shortDesc) {
			return Description.ExtraDescription.getExtraDescription(getDescription(c, shortDesc), describeSubCommands(grp));			
		}
		public static <C, M> Description getGroupDescription(final Group<C, M> grp, TypeElement c, String shortDesc) {
			return Description.ExtraDescription.getExtraDescription(getDescription(c, shortDesc), describeSubCommands(grp));			
		}
		@SuppressWarnings("unchecked")
		public static <C, M, V extends C> Description getGroupDescription(final Group<C, M> grp, String shortDesc, AnnotationUtils.AnnotationType<V> ann) {
			return Description.ExtraDescription.getExtraDescription(getDescription((V)grp.getAssociatedElement(), shortDesc, ann), describeSubCommands(grp));			
		}
		
		static <C,M> String[][] describeSubCommands(Group<C, M> grp) {
			final ArrayList<String[]> list = new ArrayList<String[]>();
			new ActionVisitor<C, M>() {
				public void visit(Action<C, M> cmd) {
					if(!ExecutorMode.HELP.isIgnored(cmd)) {
						list.add(new String[]{cmd.getID(), cmd.getDescription().getShortDescription()});
					}
				}
			}.startVisit(grp);
			final String[][] res = new String[list.size()][]; 
			return list.toArray(res);
		}
	}
	
	public class TextDescription implements Description {
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
	
	public class RessourceDescription extends URLDescription {
		public RessourceDescription(String summary, String uri) {
			super(summary, uri);
		}
	
		InputStream openRessource(String uri) throws IOException {
			InputStream is = Factory.class.getClassLoader().getResourceAsStream(uri);
			if(is == null) throw new IOException();
			return is;
		}
	}
	
	public class URLDescription implements Description {
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
}
