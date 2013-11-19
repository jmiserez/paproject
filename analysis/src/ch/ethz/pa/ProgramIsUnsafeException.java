package ch.ethz.pa;

@SuppressWarnings("serial")
public class ProgramIsUnsafeException extends RuntimeException {
	
	public ProgramIsUnsafeException() {
		super();
	}
	public ProgramIsUnsafeException(String reason) {
		super(reason);
	}
}