/**
 * 
 */
package org.poregex.core.exception;

import java.text.MessageFormat;

/**
 * @author kikimans
 *
 */
public class PorgexException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public PorgexException(Throwable cause, String message, Object... arguments){
		super(MessageFormat.format(message, arguments), cause);
	}
	
	public PorgexException(String message, Object... arguments){
		super(MessageFormat.format(message, arguments));
	}

}
