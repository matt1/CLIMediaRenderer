package org.matt1.climediarenderer.player;

/**
 * Exception that is thrown when there was a problem playing back the audio.
 * 
 * @author Matt
 *
 */
public class PlayerException extends Exception {

	/** Generated serial ID */
	private static final long serialVersionUID = 5049756687686015332L;

	public PlayerException(String reason) {
		super(reason);
	}
	
}
