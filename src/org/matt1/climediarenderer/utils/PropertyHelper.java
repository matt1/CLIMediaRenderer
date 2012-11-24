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

	private Properties properties;
	private static PropertyHelper propertyHelper;

	
	public static PropertyHelper getInstance() throws Exception {
		
		if (propertyHelper == null) {
		
			propertyHelper = new PropertyHelper();
			
			try {
				propertyHelper.properties.load(new FileInputStream("cliMediaRenderer.properties"));
			} catch (FileNotFoundException e) {
				throw new Exception("File not found: ./cliMediaRenderer.properties", e);
			} catch (IOException e) {
				throw new Exception("Cannot open file: ./cliMediaRenderer.properties", e);
			}
		}
		return propertyHelper;
	}
	
	private PropertyHelper() {
		properties = new Properties();
		
	}
	
	/**
	 * Gets the playerType property for use by the player factory
	 * @return
	 */
	public String getMediaPlayer() {
		return (String) properties.get("playerType");
	}
	
	/**
	 * Gets the path to MPlayer
	 * @return
	 */
	public String getMPlayerPath() {
		return (String) properties.get("mplayerPath");
	}
	
	/**
	 * Gets the name of the player
	 * @return
	 */
	public String getName() {
		return (String) properties.getProperty("name");
	}
	
	/**
	 * Gets the path to the icon file
	 * @return
	 */
	public String getIconPath() {
		return (String) properties.getProperty("icon");
	}
	
	/**
	 * Gets the hostname of the MPD client
	 * @return
	 */
	public String getMPDHost() {
		return (String) properties.getProperty("mpdHost");
	}
	
	/**
	 * Gets the port of the MPD client
	 * @return
	 */
	public int getMPDPort() {
		return Integer.valueOf(properties.getProperty("mpdPort"));
	}
}
