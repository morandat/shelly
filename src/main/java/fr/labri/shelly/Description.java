package fr.labri.shelly;

public interface Description {
	String getShortDescription();
	String getLongDescription();

	Description NO_DESCRIPTION = new Description() {
		@Override
		public String getShortDescription() {
			return NO_DESCRIPTION_TEXT;
		}
		
		@Override
		public String getLongDescription() {
			return NO_DESCRIPTION_TEXT;
		}
	};
	static final String NO_DESCRIPTION_TEXT = "No description";
}
