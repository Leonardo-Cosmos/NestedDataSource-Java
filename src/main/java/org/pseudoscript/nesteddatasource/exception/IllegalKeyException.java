package org.pseudoscript.nesteddatasource.exception;

public class IllegalKeyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4034992782791625756L;

	private String key;
	
	public IllegalKeyException() {
		
	}
	
	public IllegalKeyException(String message) {
		super(message);
	}
	
	public IllegalKeyException(String key, String message) {
		super(message);
		this.key = key;
	}
	
	/**
	 * Returns key of the data.
	 * @return key of the data that cannot be found.
	 */
	public String getKey() {
		return this.key;
	}
	
}
