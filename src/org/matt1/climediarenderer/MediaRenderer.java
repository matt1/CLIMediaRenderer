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

	/**
	 * Sets up logging, creates the new CliMediaRenderer instance and then starts UPnP
	 * 
	 * @param args
	 * @throws Exception
	 */
    public static void main(final String[] args) throws Exception {
    	configureLogging();
    	
    	CliMediaRenderer mediaRenderer = new CliMediaRenderer(PropertyHelper.getName());

    	UpnpServiceImpl upnp = new UpnpServiceImpl(new ApacheServiceConfiguration());
    	
    	upnp.getRegistry().addDevice(
                mediaRenderer.getDevice()
        );

    }
    
    /**
     * Setup logging level to hide all the noise from Cling
     */
    private static void  configureLogging() {
    	Logger.getLogger("org.fourthline").setLevel(Level.OFF);
    }


}
