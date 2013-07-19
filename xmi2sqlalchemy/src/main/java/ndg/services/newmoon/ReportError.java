/**
 * 
 */
package ndg.services.newmoon;

class ReportError {
	 
	final private String errorType;
	final private String errorMessage;
	final private String customMessage;
	
	public ReportError(String customMessage, String errorType, String errorMessage) {
		super();
		this.errorType = errorType;
		this.errorMessage = errorMessage;
		this.customMessage = customMessage;
	}

	@Override
	public String toString() {
		return "ReportError: " + customMessage + " [errorMessage=" + errorMessage + ", errorType=" + errorType + "]";
	}
}