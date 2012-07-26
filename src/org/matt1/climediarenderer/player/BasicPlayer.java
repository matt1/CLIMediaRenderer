package org.matt1.climediarenderer.player;

/**
 * Simple interface that any player implementation must abide by.  This allows us to extend
 * the software with additional players (e.g. something like gstreamer) easily.
 * 
 * @author Matt
 *
 */
public interface BasicPlayer {

	/**
	 * Play the network media location.
	 * 
	 * @throws PlayerException is audio cannot be played
	 */
	public void play() throws PlayerException;
	
	/**
	 * Stop the media playback.
	 * 
	 * @throws PlayerException is audio cannot be stopped
	 */
	public void stop() throws PlayerException;
	
	/**
	 * Pause the currently playing audio.
	 * 
	 * @throws PlayerException
	 */
	public void pause() throws PlayerException;
	
	/**
	 * Skip forwards or backwards by a number of milliseconds.  Pass in a negative value to skip
	 * backwards.
	 * 
	 * @param milliseconds
	 * @throws PlayerException
	 */
	public void skip(long milliseconds) throws PlayerException;
}
