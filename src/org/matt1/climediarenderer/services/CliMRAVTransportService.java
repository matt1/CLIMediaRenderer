package org.matt1.climediarenderer.services;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.fourthline.cling.support.model.TransportState;
import org.matt1.climediarenderer.player.BasicPlayer;
import org.matt1.climediarenderer.player.PlayerException;
import org.matt1.climediarenderer.player.PlayerFactory;
import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;

/**
 * The AVTransportService for the CLI Media Renderer handles mapping the requests from UPnP to
 * the underlying BasicPlayer instance as well as handling events for updating the control point
 * 
 * @author Matt
 *
 */
public class CliMRAVTransportService extends AbstractAVTransportService {

	/** Basic logger */
    final private static Logger log = Logger.getLogger(CliMRAVTransportService.class.getName());

    /** The player instance that we are going to use for playback */
    private BasicPlayer player;  
    
    /** Info about the media file */
    private MediaInfo mediaInfo = new MediaInfo();
    
    /** Position information for the media */
    private PositionInfo positionInfo = new PositionInfo();
    
    private TransportInfo transportInfo = new TransportInfo();
    
    private TransportSettings transportSettings = new TransportSettings();
    
    private DeviceCapabilities deviceCapabilities = new DeviceCapabilities(new StorageMedium[]{});
    
    /**
     * Create a new CLI Media Renderer transport service
     * 
     * @param lastChange
     */
    protected CliMRAVTransportService(LastChange lastChange) {
        super(lastChange);       
    }

    
    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String currentURI, String currentURIMetaData) 
    		throws AVTransportException {
    	
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        if (currentURI.startsWith("http:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri));
            } catch (IOException ex) {
                throw new AVTransportException(AVTransportErrorCode.READ_ERROR, "Unable to read requested URI: " + uri);
            }
        } else  {
            throw new AVTransportException(ErrorCode.INVALID_ACTION, "Requested URI was not a network stream.");
        }
        
        // Instantiate a new player
		try {
			player = PlayerFactory.getPlayer(uri.toString());
		} catch (PlayerException e) {
			throw new AVTransportException(ErrorCode.HUMAN_INTERVENTION_REQUIRED, "Unable to start media player backend.");
		}

		// Build media info
		String duration = ModelUtil.toTimeString(player.getDuration());
		mediaInfo = new MediaInfo(currentURI, currentURIMetaData, null, duration, null);
		positionInfo = new PositionInfo(1, currentURIMetaData, currentURI);
	    transportInfo = new TransportInfo(TransportState.STOPPED);    
		
        getLastChange().setEventedValue(
                getDefaultInstanceID(),
                new AVTransportVariable.TransportState(TransportState.STOPPED),
                new AVTransportVariable.CurrentTransportActions(new TransportAction[]{
                        TransportAction.Play
                })
        );
        getLastChange().fire(getPropertyChangeSupport());
		
		log.info("New player created for " + uri);
        
    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		return mediaInfo;
        
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		return transportInfo;
     
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) 
    		throws AVTransportException {
		if (player == null) {
			// nop
		} else {
			long position = (long) player.getPositionPercentage();
			long duration = player.getDuration();
			String positionString = ModelUtil.toTimeString(position);
			String durationString = ModelUtil.toTimeString(duration);
			positionInfo = new PositionInfo(0, durationString, 
					player.getCurrentUri(), positionString, positionString);
			
		}
		return positionInfo;
       
    }
    
    public void setPositionInfo(PositionInfo newPosition) {
    	positionInfo = newPosition;
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		return deviceCapabilities;
      
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		return transportSettings;
        
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
      if (player != null) {
    	  try {
			player.stop();
			transportInfo = new TransportInfo(TransportState.STOPPED);
			this.getLastChange().setEventedValue(
				this.getCurrentInstanceIds()[0],
				new AVTransportVariable.TransportState(TransportState.STOPPED)
			);
			this.getLastChange().fire(getPropertyChangeSupport());
		} catch (PlayerException e) {
			throw new AVTransportException(ErrorCode.ACTION_FAILED, e.getMessage());
		}

      }
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
       if (player != null) {
    	   try {
	    	   player.play();
	    	   transportInfo = new TransportInfo(TransportState.PLAYING);
	
	           getLastChange().setEventedValue(
	                   getDefaultInstanceID(),
	                   new AVTransportVariable.TransportState(TransportState.PLAYING),
	                   new AVTransportVariable.CurrentTransportActions(new TransportAction[]{
	                           TransportAction.Stop
	                   })
	           );
	           getLastChange().fire(getPropertyChangeSupport());
    	   } catch (PlayerException e) {
    		   throw new AVTransportException(ErrorCode.ACTION_FAILED, e.toString());
    	   }
       } else {
    	   transportInfo = new TransportInfo(TransportState.STOPPED);
    	   throw new AVTransportException(ErrorCode.INVALID_ACTION, "No player created - try setting URI of media first.");
       }
       
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	if (player != null) {
     	   try {
 	    	   player.pause();
 	    	   transportInfo = new TransportInfo(TransportState.PAUSED_PLAYBACK);
 	
 	           getLastChange().setEventedValue(
 	                   getDefaultInstanceID(),
 	                   new AVTransportVariable.TransportState(TransportState.PAUSED_PLAYBACK),
 	                   new AVTransportVariable.CurrentTransportActions(new TransportAction[]{
 	                           TransportAction.Stop,
 	                           TransportAction.Play
 	                   })
 	           );
 	           getLastChange().fire(getPropertyChangeSupport());
     	   } catch (PlayerException e) {
     		   throw new AVTransportException(ErrorCode.ACTION_FAILED, e.toString());
     	   }
        } else {
     	   transportInfo = new TransportInfo(TransportState.STOPPED);
     	   throw new AVTransportException(ErrorCode.INVALID_ACTION, "No player created - try setting URI of media first.");
        }
        
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Record not supported.");
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Seek not supported.");
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Next not supported.");
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Previous not supported.");
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Next URI not supported.");
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Setting playmode not supported.");
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
    	throw new AVTransportException(ErrorCode.INVALID_ACTION, "Record not supported.");
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
		return null;
     
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		return new UnsignedIntegerFourBytes[]{getDefaultInstanceID()};
    }
}
