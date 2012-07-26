package org.matt1.climediarenderer.player;

import java.util.logging.Logger;

import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.support.model.PositionInfo;
import org.matt1.climediarenderer.services.CliMRAVTransportService;
import org.matt1.climediarenderer.utils.PropertyHelper;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

/**
 * A BasicPlayer using the VLCJ library to play back audio.
 * 
 * @author Matt
 *
 */
public class VlcjPlayer implements BasicPlayer {
	 final private static Logger log = Logger.getLogger(VlcjPlayer.class.getName());
	AudioMediaPlayerComponent player;
	String mediaPath;
	CliMRAVTransportService service;
	PositionInfo positionInfo = null;
	
	private float duration = 0;
	private float position = 0;
	
	/** Keep track of our paused state so we can resume */
	private boolean isPaused = false;
	
	private MediaPlayerEventListener eventListener = new MediaPlayerEventListener() {
		
		@Override
		public void videoOutput(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void titleChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void timeChanged(MediaPlayer arg0, long arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void subItemPlayed(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void subItemFinished(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void stopped(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void snapshotTaken(MediaPlayer arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void seekableChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void positionChanged(MediaPlayer arg0, float arg1) {
			position = arg1;
			updatePosition();
			
		}
		
		@Override
		public void playing(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void paused(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void pausableChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void opening(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void newMedia(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mediaSubItemAdded(MediaPlayer arg0, libvlc_media_t arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mediaStateChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mediaParsedChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mediaMetaChanged(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mediaFreed(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mediaDurationChanged(MediaPlayer arg0, long arg1) {
			duration = arg1;
			updatePosition();
			
		}
		
		@Override
		public void mediaChanged(MediaPlayer arg0, libvlc_media_t arg1, String arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void lengthChanged(MediaPlayer arg0, long arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void forward(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void finished(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void endOfSubItems(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void buffering(MediaPlayer arg0, float arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void backward(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			
		}
	};

	/**
	 * Create a new VLCJ media player using the JNA native library path provided in the jnaPath
	 * property.
	 * 
	 * @param mediaPath
	 * @param service
	 */
	public VlcjPlayer(String mediaPath, CliMRAVTransportService service) {
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PropertyHelper.getVLCPath());
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		this.service = service;
		
		player = new AudioMediaPlayerComponent();
		this.mediaPath = mediaPath;
	}
	
	public void play() throws PlayerException {
		if (isPaused) {
			player.getMediaPlayer().setPause(false);
		} else {
			if (mediaPath == null || mediaPath.isEmpty()) {
				throw new PlayerException("Unable to playback media - none specified.");
			}
			player.getMediaPlayer().playMedia(mediaPath);
			player.getMediaPlayer().addMediaPlayerEventListener(eventListener);
		}
		isPaused = false;
	}
	
	public void stop() throws PlayerException {
		player.getMediaPlayer().stop();
		isPaused = false;
	}
	
	public void pause() throws PlayerException {
		player.getMediaPlayer().setPause(true);
		isPaused = true;
	}
	
	public void skip(long milliseconds) throws PlayerException {
		player.getMediaPlayer().skip(milliseconds);
	}
	
	private void updatePosition() {
		long pos = (long) Math.floor(position * (duration/1000));
		PositionInfo info = new PositionInfo(positionInfo, ModelUtil.toTimeString(pos), ModelUtil.toTimeString(pos));
		log.info(info.toString());
		service.setPositionInfo(info);
	
	}
}
