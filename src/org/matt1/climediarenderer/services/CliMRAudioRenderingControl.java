package org.matt1.climediarenderer.services;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;

/**
 * The rendering control doesn't offer the CLI Media Renderer much functionality, but it seems to
 * be required for UPnP control points to "see" it.
 * 
 * @author Matt
 *
 */
public class CliMRAudioRenderingControl extends AbstractAudioRenderingControl {

    protected CliMRAudioRenderingControl(LastChange lastChange) {
        super(lastChange);
    }


    protected void checkChannel(String channelName) throws RenderingControlException {
        if (!getChannel(channelName).equals(Channel.Master)) {
            throw new RenderingControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) 
    		throws RenderingControlException {
   
        return false;
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instId, String channelName, boolean desiredMute) 
    		throws RenderingControlException {
       
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instId, String channelName) 
    		throws RenderingControlException {
        return null;
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instId, String channelName, 
    		UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        
    }

    @Override
    protected Channel[] getCurrentChannels() {
    	return null;
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return null;
    }
}