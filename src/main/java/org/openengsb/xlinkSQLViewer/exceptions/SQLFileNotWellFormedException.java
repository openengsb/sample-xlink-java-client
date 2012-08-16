package org.openengsb.xlinkSQLViewer.exceptions;

/**This exception is thrown if a parsed SQL File was not well formed, 
 * in reflection to the defined SQL Statement Format*/
@SuppressWarnings("serial")
public class SQLFileNotWellFormedException extends Exception {
    public SQLFileNotWellFormedException() {
        super();
	}
	
	public SQLFileNotWellFormedException(String message) {
		super(message);
	}
	
	public SQLFileNotWellFormedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SQLFileNotWellFormedException(Throwable cause) {
		super(cause);
	}
}
