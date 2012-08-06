package org.matt1.climediarenderer.player;

import org.matt1.climediarenderer.utils.PropertyHelper;

/**
 * Factory class to create a new player based on the value setup in the properties file
 * 
 * @author Matt
 *
 */
public class PlayerFactory {

	/** Static instance to stop making countless players */
	private static BasicPlayer instance;
	
	/**
	 * Get an appropriate player type based on the mediaType in the properties file
	 * @return
	 * @throws PlayerException 
	 */
	public static BasicPlayer getPlayer(String mediaPath) throws PlayerException {
		
		if (instance == null) {
		
			String type = PropertyHelper.getMediaPlayer().toLowerCase();
			
			if (type.equals("mplayer")) {
				instance = new MPlayer(mediaPath);
			} else {
				throw new PlayerException("Unknown media player type");
			}
		}
		
		return instance;
	}
	
}
