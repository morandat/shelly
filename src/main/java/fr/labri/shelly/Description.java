package fr.labri.shelly;

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
}
