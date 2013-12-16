package fr.labri.shelly.annotations;

import fr.labri.shelly.Item;

public @interface Ignore {
	ExecutorMode[] value() default {};
	
	public enum ExecutorMode {
		INTERACTIVE, BATCH, HELP, ALL;

		public boolean isIgnored(Item<?, ?> a) {
			Ignore ign = a.getAnnotation(Ignore.class);
			if(ign != null)
				return isIgnored(ign.value());
			return false;
		}
		
		public boolean isIgnored(ExecutorMode[] values) {
			for(ExecutorMode v: values)
				if(ALL.equals(v) || equals(v))
					return true;
			return false;
		}
	}
}
