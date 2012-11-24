package org.matt1.climediarenderer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.fourthline.cling.UpnpServiceImpl;
import org.matt1.climediarenderer.services.CliMediaRenderer;
import org.matt1.climediarenderer.utils.PropertyHelper;

/**
 * Main entry class.  All it does it setup the UPnP service, create a new instane of the
 * main class and start it up.
 * 
 * @author Matt
 *
 */
public class MediaRenderer {

    public static void main(final String[] args) throws Exception {

    	configureLogging();
    	
    	try {
	    	CliMediaRenderer mediaRenderer = new CliMediaRenderer(PropertyHelper.getInstance().getName());
	
	    	UpnpServiceImpl upnp = new UpnpServiceImpl(new ApacheServiceConfiguration());
	    	
	    	upnp.getRegistry().addDevice(
	                mediaRenderer.getDevice()
	        );
    	} catch (Exception e) {
    		Logger.getLogger(MediaRenderer.class.getName()).severe("Unexpected error starting up: " + e.getMessage());
    	}

    }

    /**
     * Setup logging level to hide all the noise from Cling
     */
    private static void configureLogging() {
    	Logger.getLogger("org.fourthline").setLevel(Level.OFF);
    }
}
