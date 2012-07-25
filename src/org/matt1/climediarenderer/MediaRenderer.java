package org.matt1.climediarenderer;


import org.fourthline.cling.UpnpServiceImpl;
import org.matt1.climediarenderer.services.CliMediaRenderer;
import org.matt1.climediarenderer.utils.PropertyHelper;

public class MediaRenderer {

    public static void main(final String[] args) throws Exception {

    	CliMediaRenderer mediaRenderer = new CliMediaRenderer(PropertyHelper.getName());

    	new UpnpServiceImpl().getRegistry().addDevice(
                mediaRenderer.getDevice()
        );

    }


}
