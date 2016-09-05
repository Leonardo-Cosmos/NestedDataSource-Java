package org.pseudoscript.nesteddatasource.exception;

public class DataNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8800403572619568914L;

	private String key;	
	
	public DataNotFoundException() {
		
	}
	
	public DataNotFoundException(String message) {
		super(message);
	}
	
	public DataNotFoundException(String key, String message) {
		super(message);
		this.key = key;
	}
	
	/**
	 * Returns key of the data.
	 * @return key of the data that cannot be found.
	 */
	public String getKey() {
		return key;
	}
	
}
