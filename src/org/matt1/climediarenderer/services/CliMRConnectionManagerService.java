package org.matt1.climediarenderer.services;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

/**
 * Simple ConnectionManager service.  Basically doesn't do anything significant apart from
 * provide media protocol information.
 * 
 * @author Matt
 *
 */
public class CliMRConnectionManagerService extends ConnectionManagerService {

    /**
     * Basic MP3 support only for now...
     */
    public CliMRConnectionManagerService() {
        sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/mp3:*"));
    	sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/mpeg:*"));
    	sinkProtocolInfo.add(new ProtocolInfo("http-get:*:audio/mpeg3:*"));
          
    }
    
    public ProtocolInfos getSinkProtocolInfo() {
    	return super.getSinkProtocolInfo();
    }

}
