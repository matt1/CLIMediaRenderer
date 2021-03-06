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
import org.matt1.climediarenderer.managers.LenientChangeAwareServiceManager;
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
    
    /** Longer lock for slower hardware/JRE */
    protected static final int TIMEOUT = 2000;
    
    /** PropertyHelper instance */
    protected PropertyHelper properties;
    
    /**
     * Creates a new device using the default device details
     * 
     * @throws IllegalArgumentException
     * @throws ValidationException
     * @throws IOException 
     */
    public CliMediaRenderer() throws IllegalArgumentException, ValidationException, IOException, Exception {
    	
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
    	ValidationException, IOException, Exception {
    	this(new DeviceDetails(
                name,
                new ManufacturerDetails(MANUFACTURER_NAME, MANUFACTURER_SITE),
                new ModelDetails(MODEL_NAME, MODEL_DESCRIPTION, MODEL_VERSION, MODEL_SITE)
        ));
    }
    
    /**
     * Try loading the properties from the configuration file.
     * 
     * @throws Exception
     */
    private void loadProperties() throws Exception {
    	
    	// Load properties
    	try {
			properties = PropertyHelper.getInstance();
		} catch (Exception e) {
			log.severe("Unable to load the config file! " + e.getMessage());
			System.exit(1);
		}	
    }
    
    /**
     * Try loading the icon specified in the properties file
     */
    private Icon loadIcon() {
    	File iconFile = new File(properties.getIconPath());
    	Icon icon = null;
    	try {
	    	if (iconFile.exists() && iconFile.canRead()) {
	    		icon = new Icon("image/png", 48, 48, 8, iconFile);
	    	} else {
	    		log.warning("Custom icon " + properties.getIconPath() + " could not be read.");
	    	}
    	} catch (IOException e) {
    		log.warning("IO Exception trying to load icon file at " + properties.getIconPath());
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
		ValidationException, IOException, Exception {
        	
    	loadProperties();
    	
        LocalService<CliMRConnectionManagerService> connectionManagerService = serviceBinder.read(CliMRConnectionManagerService.class);
        connectionServiceManager =
                new DefaultServiceManager<CliMRConnectionManagerService>(connectionManagerService) {
                    @Override
                    protected CliMRConnectionManagerService createServiceInstance() throws Exception {
                        return new CliMRConnectionManagerService();
                    }
                    
                    @Override
                    protected int getLockTimeoutMillis() {
                        return TIMEOUT;
                    }
                };
        connectionManagerService.setManager(connectionServiceManager);

        LocalService<CliMRAVTransportService> audioTransportService = serviceBinder.read(CliMRAVTransportService.class);
        audioTransportServiceManager =
                new LenientChangeAwareServiceManager<CliMRAVTransportService>(
                        audioTransportService,
                        new AVTransportLastChangeParser()) {
                    @Override
                    protected CliMRAVTransportService createServiceInstance() throws Exception {
                        return new CliMRAVTransportService(avTransportLastChange);
                    }
                    @Override
                    protected int getLockTimeoutMillis() {
                        return TIMEOUT;
                    }
                };
        audioTransportService.setManager(audioTransportServiceManager);

        LocalService<CliMRAudioRenderingControl> renderingControlService = serviceBinder.read(CliMRAudioRenderingControl.class);
        renderingControlServiceManager =
                new LenientChangeAwareServiceManager<CliMRAudioRenderingControl>(
                        renderingControlService,
                        new RenderingControlLastChangeParser()
                ) {
                    @Override
                    protected CliMRAudioRenderingControl createServiceInstance() throws Exception {
                        return new CliMRAudioRenderingControl(renderingControlLastChange);
                    }
                    @Override
                    protected int getLockTimeoutMillis() {
                        return TIMEOUT;
                    }
                };
       renderingControlService.setManager(renderingControlServiceManager);

        
        uPnPDevice = new LocalDevice(
                new DeviceIdentity(UDN.uniqueSystemIdentifier("CLI Media Renderer")),
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
        
        log.info("CLI Media Renderer ready.");
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
