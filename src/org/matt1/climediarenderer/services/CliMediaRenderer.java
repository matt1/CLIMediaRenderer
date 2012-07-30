package org.matt1.climediarenderer.services;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import org.matt1.climediarenderer.utils.PropertyHelper;

/**
 * Creates a new CliMediaRenderer UPnP instance, setting up all of the appropriate UPnP services 
 * that are required such as the AVTransportService.
 * 
 * @author Matt
 *
 */
public class CliMediaRenderer {
	
	/** Log used to display messaes to the console */
	private static Logger log = Logger.getLogger(CliMediaRenderer.class.getName());
	
	/** Logical "device" represented to the network and which offers the services */
	protected LocalDevice uPnPDevice;
	
	/** Service Binder for attaching services to a device */
    protected LocalServiceBinder serviceBinder = new AnnotationLocalServiceBinder();
    
    /** The LastChange object for the AVTransport */
    protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    
    /** The last change object for the Rendering Control */
    protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    /** Service manager for the connection service */
    protected ServiceManager<CliMRConnectionManagerService> connectionServiceManager;
    
    /** The actual AV Transport service manager */
    protected LastChangeAwareServiceManager<CliMRAVTransportService> audioTransportServiceManager;
    
    /** The rendering control service manager */
    protected LastChangeAwareServiceManager<CliMRAudioRenderingControl> renderingControlServiceManager;

    /** Default manufacturer name */
    private static final String MANUFACTURER_NAME = "";
    
    /** Default manufacturer site */
    private static final String MANUFACTURER_SITE = "";
    
    /** Default model name */
    private static final String MODEL_NAME = "CliMediaRenderer";
    
    /** Default model description */
    private static final String MODEL_DESCRIPTION = "Commandline UPnP/DNLA media renderer/DMR";
    
    /** Default model version */
    private static final String MODEL_VERSION = "1.0";
    
    /** Default model site */
    private static final String MODEL_SITE = MANUFACTURER_SITE;
    
    /**
     * Creates a new device using the default device details
     * 
     * @throws IllegalArgumentException
     * @throws ValidationException
     * @throws IOException 
     */
    public CliMediaRenderer() throws IllegalArgumentException, ValidationException, IOException {
        
    	this(new DeviceDetails(
                "CliMediaRenderer",
                new ManufacturerDetails(MANUFACTURER_NAME, MANUFACTURER_SITE),
                new ModelDetails(MODEL_NAME, MODEL_DESCRIPTION, MODEL_VERSION, MODEL_SITE)
        ));
    }
    
    /**
     * Creates a new device using a specific name
     * 
     * @param name
     * @throws IllegalArgumentException
     * @throws ValidationException
     * @throws IOException 
     */
    public CliMediaRenderer(String name) throws IllegalArgumentException, 
    	ValidationException, IOException {
    	this(new DeviceDetails(
                name,
                new ManufacturerDetails(MANUFACTURER_NAME, MANUFACTURER_SITE),
                new ModelDetails(MODEL_NAME, MODEL_DESCRIPTION, MODEL_VERSION, MODEL_SITE)
        ));
    }
    
    /**
     * Try loading the icon specified in the properties file
     */
    private Icon loadIcon() {
    	File iconFile = new File(PropertyHelper.getIconPath());
    	Icon icon = null;
    	try {
	    	if (iconFile.exists() && iconFile.canRead()) {
	    		icon = new Icon("image/png", 48, 48, 8, iconFile);
	    	} else {
	    		log.warning("Custom icon " + iconFile.toPath() + " could not be read.");
	    		icon = new Icon("image/png", 48, 48, 8, new File("icon.png"));
	    	}
    	} catch (IOException e) {
    		log.warning("IO Exception trying to load icon file at " + iconFile.toPath());
    	}
    	return icon;
    }
    
    /**
     * Creates a new CliMediaRenderer device and sets up all of the appropriate services.
     * @param deviceDetails Details about this device
     * @throws ValidationException 
     * @throws IllegalArgumentException 
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public CliMediaRenderer(DeviceDetails deviceDetails) throws IllegalArgumentException, 
		ValidationException, IOException {
        	
        LocalService<CliMRConnectionManagerService> connectionManagerService = serviceBinder.read(CliMRConnectionManagerService.class);
        connectionServiceManager =
                new DefaultServiceManager<CliMRConnectionManagerService>(connectionManagerService) {
                    @Override
                    protected CliMRConnectionManagerService createServiceInstance() throws Exception {
                        return new CliMRConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionServiceManager);

        LocalService<CliMRAVTransportService> audioTransportService = serviceBinder.read(CliMRAVTransportService.class);
        audioTransportServiceManager =
                new LastChangeAwareServiceManager<CliMRAVTransportService>(
                        audioTransportService,
                        new AVTransportLastChangeParser()) {
                    @Override
                    protected CliMRAVTransportService createServiceInstance() throws Exception {
                        return new CliMRAVTransportService(avTransportLastChange);
                    }
                };
        audioTransportService.setManager(audioTransportServiceManager);

        LocalService<CliMRAudioRenderingControl> renderingControlService = serviceBinder.read(CliMRAudioRenderingControl.class);
        renderingControlServiceManager =
                new LastChangeAwareServiceManager<CliMRAudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected CliMRAudioRenderingControl createServiceInstance() throws Exception {
                        return new CliMRAudioRenderingControl(renderingControlLastChange);
                    }
                };
       renderingControlService.setManager(renderingControlServiceManager);

        
        uPnPDevice = new LocalDevice(
                new DeviceIdentity(UDN.uniqueSystemIdentifier("Cling MediaRenderer")),
                new UDADeviceType("MediaRenderer", 1),
                deviceDetails,
                loadIcon(),
                new LocalService[]{
                        audioTransportService,
                        renderingControlService,
                        connectionManagerService
                }
        );

        // Finally setup last change thread to send updates back to subscribers.
        initChangeThread();
    }

    /**
     * Starts a thread that will run and send all "last change" events (e.g. player state change)
     * back to any subscribing control points
     */
    protected void initChangeThread() {
        Thread changeThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        audioTransportServiceManager.fireLastChange();
                        renderingControlServiceManager.fireLastChange();
                        Thread.sleep(1000);
                    }
                } catch (Exception ex) {
                	log.info("Exception caught in lastChangeThread(): " + ex.getMessage());
                }
            }
        };
        
        changeThread.start();
    }

    public LocalDevice getDevice() {
        return uPnPDevice;
    }

    public ServiceManager<CliMRConnectionManagerService> getConnectionManager() {
        return connectionServiceManager;
    }

    public ServiceManager<CliMRAVTransportService> getAvTransport() {
        return audioTransportServiceManager;
    }

}
