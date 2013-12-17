package fr.labri.shelly;

public class ShellyException extends RuntimeException {
	public static class EOLException extends ShellyException {
		private static final long serialVersionUID = 1L;
	}

	private static final long serialVersionUID = 1L;

	public ShellyException() {
		super();
	}

	public ShellyException(String msg) {
		super(msg);
	}

	public ShellyException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ShellyException(Throwable cause) {
		super(cause);
	}

	static class ConverstionException extends ShellyException {
		public ConverstionException() {
		}

		private static final long serialVersionUID = 1L;
	}
}
