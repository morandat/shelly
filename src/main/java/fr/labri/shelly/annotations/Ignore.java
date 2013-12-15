package fr.labri.shelly.annotations;

import fr.labri.shelly.Item;

public @interface Ignore {
	ExecutorMode[] value() default {};
	
	public enum ExecutorMode {
		INTERACTIVE, BATCH, HELP;

		public boolean isIgnored(Item<?, ?> a) {
			Ignore ign = a.getAnnotation(Ignore.class);
			if(ign != null)
				return isIgnored(ign.value());
			return false;
		}
		
		public boolean isIgnored(ExecutorMode[] values) {
			for(ExecutorMode v: values)
				if(equals(v))
					return true;
			return false;
		}
	}
}
