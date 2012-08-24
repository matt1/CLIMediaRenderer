package org.matt1.climediarenderer.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Helper class that loads the properties from disk
 * @author Matt
 *
 */
public class PropertyHelper {

	private static Properties properties = new Properties();
	static {
		try {
			properties.load(new FileInputStream("cliMediaRenderer.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the playerType property for use by the player factory
	 * @return
	 */
	public static String getMediaPlayer() {
		return (String) properties.get("playerType");
	}
	
	/**
	 * Gets the path to MPlayer
	 * @return
	 */
	public static String getMPlayerPath() {
		return (String) properties.get("mplayerPath");
	}
	
	/**
	 * Gets the name of the player
	 * @return
	 */
	public static String getName() {
		return (String) properties.getProperty("name");
	}
	
	/**
	 * Gets the path to the icon file
	 * @return
	 */
	public static String getIconPath() {
		return (String) properties.getProperty("icon");
	}
	
	/**
	 * Gets the hostname of the MPD client
	 * @return
	 */
	public static String getMPDHost() {
		return (String) properties.getProperty("mpdHost");
	}
	
	/**
	 * Gets the port of the MPD client
	 * @return
	 */
	public static int getMPDPort() {
		return Integer.valueOf(properties.getProperty("mpdPort"));
	}
}
