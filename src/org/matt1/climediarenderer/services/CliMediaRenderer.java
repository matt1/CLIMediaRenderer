/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.matt1.climediarenderer.services;

import java.io.File;
import java.io.IOException;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ModelUtil;
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

/**
 * Creates a new CliMediaRenderer UPnP instance, setting up all of the appropriate UPnP services 
 * that are required such as the AVTransportService.
 * 
 * @author Matt
 *
 */
public class CliMediaRenderer {

	/** Logical "device" represented to the network and which offers the services */
	protected LocalDevice uPnPDevice;
	
	/** Service Binder for attaching services to a device */
    protected LocalServiceBinder serviceBinder = new AnnotationLocalServiceBinder();
    
    /** The LastChange object for the AVTransport */
    protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    
    /** Service manager for the connection service */
    protected ServiceManager<CliMRConnectionManagerService> connectionServiceManager;
    
    /** The actual AV Transport service manager */
    protected LastChangeAwareServiceManager<CliMRAVTransportService> audioTransportServiceManager;

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
                "MediaRenderer on " + ModelUtil.getLocalHostName(false),
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
     * Creates a new CliMediaRenderer device and sets up all of the appropriate services.
     * @param deviceDetails Details about this device
     * @throws ValidationException 
     * @throws IllegalArgumentException 
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public CliMediaRenderer(DeviceDetails deviceDetails) throws IllegalArgumentException, 
		ValidationException, IOException {
    
    	Icon icon = new Icon("image/png", 48, 48, 8, new File("icon.png"));
    	
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

        uPnPDevice = new LocalDevice(
                new DeviceIdentity(UDN.uniqueSystemIdentifier("Cling MediaRenderer")),
                new UDADeviceType("MediaRenderer", 1),
                deviceDetails,
                icon,
                new LocalService[]{
                        audioTransportService,
                        connectionManagerService
                }
        );

        // Finally setup last change thread to send updates back to subscribers.
        initLastChangeThread();
    }

    /**
     * Starts a thread that will run and send all "last change" events (e.g. player state change)
     * back to any subscribing control points
     */
    protected void initLastChangeThread() {
        Thread changeThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        audioTransportServiceManager.fireLastChange();
                        Thread.sleep(250);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
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
