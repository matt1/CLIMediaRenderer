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

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
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

/**
 * Creates a new CliMediaRenderer UPnP instance, setting up all of the appropriate UPnP services that are required.
 * 
 * @author Matt
 *
 */
public class CliMediaRenderer {

	/** Logical "device" represented to the network and which offers the services */
	protected LocalDevice uPnPDevice;
	
	/** Service Binder for attaching services to a device */
    protected LocalServiceBinder serviceBinder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single service
    protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
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
     */
    public CliMediaRenderer() throws IllegalArgumentException, ValidationException {
        
    	this(new DeviceDetails(
                "MediaRenderer on " + ModelUtil.getLocalHostName(false),
                new ManufacturerDetails(MANUFACTURER_NAME, MANUFACTURER_SITE),
                new ModelDetails(MODEL_NAME, MODEL_DESCRIPTION, MODEL_VERSION, MODEL_SITE)
        ));
    }
    
    public CliMediaRenderer(String name) throws IllegalArgumentException, ValidationException {
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
     */
    @SuppressWarnings("unchecked")
	public CliMediaRenderer(DeviceDetails deviceDetails) throws IllegalArgumentException, ValidationException {
    
        // The connection manager doesn't have to do much, HTTP is stateless
        LocalService<CliMRConnectionManagerService> connectionManagerService = serviceBinder.read(CliMRConnectionManagerService.class);
        connectionServiceManager =
                new DefaultServiceManager<CliMRConnectionManagerService>(connectionManagerService) {
                    @Override
                    protected CliMRConnectionManagerService createServiceInstance() throws Exception {
                        return new CliMRConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionServiceManager);

        // The AVTransport just passes the calls on to the backend players
        LocalService<CliMRAVTransportService> audioTransportService = serviceBinder.read(CliMRAVTransportService.class);
        audioTransportServiceManager =
                new LastChangeAwareServiceManager<CliMRAVTransportService>(
                        audioTransportService,
                        new AVTransportLastChangeParser()
                ) {
                    @Override
                    protected CliMRAVTransportService createServiceInstance() throws Exception {
                        return new CliMRAVTransportService(avTransportLastChange);
                    }
                };
               
        audioTransportService.setManager(audioTransportServiceManager);

        // The Rendering Control just passes the calls on to the backend players
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
                new LocalService[]{
                        audioTransportService,
                        renderingControlService,
                        connectionManagerService
                }
        );


        initLastChangeThread();
    }

    protected void initLastChangeThread() {
        // TODO: We should only run this if we actually have event subscribers
        Thread changeThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        audioTransportServiceManager.fireLastChange();
                        renderingControlServiceManager.fireLastChange();
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

    public ServiceManager<CliMRAudioRenderingControl> getRenderingControl() {
        return renderingControlServiceManager;
    }

}
