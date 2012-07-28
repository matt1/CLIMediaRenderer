package org.matt1.climediarenderer.player;

import org.matt1.climediarenderer.utils.PropertyHelper;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
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

	/** The actual media player */
	private AudioMediaPlayerComponent player;
	
	/** URI path to the media player */
	private String mediaPath;
		
	/** Total track length in milliseconds */
	private long duration = 0;
	
	/** Current position in the track in milliseconds */
	private long position = 0;
	
	/** Keep track of our paused state so we can resume */
	private boolean isPaused = false;
	
	/** The event listener adaptor that handles events form the VLC player */
	private MediaPlayerEventAdapter eventListener = new MediaPlayerEventAdapter() {
		public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
			position = newTime;
		}
		
		public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
			duration = newDuration;
		}
		
		public void stopped(MediaPlayer mediaPlayer) {
			isPaused = false;
		}
	};

	/**
	 * Create a new VLCJ media player using the JNA native library path provided in the jnaPath
	 * property.
	 * 
	 * @param mediaPath
	 * @param service
	 */
	public VlcjPlayer(String mediaPath) {
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PropertyHelper.getVLCPath());
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		
		player = new AudioMediaPlayerComponent();
		this.mediaPath = mediaPath;
	}
	
	@Override
	public void play() throws PlayerException {
		if (isPaused) {
			player.getMediaPlayer().setPause(false);
		} else {
			if (mediaPath == null || mediaPath.isEmpty()) {
				throw new PlayerException("Unable to playback media - none specified.");
			}
			player.getMediaPlayer().addMediaPlayerEventListener(eventListener);
			player.getMediaPlayer().playMedia(mediaPath);
			
		}
		isPaused = false;
	}
	
	@Override
	public void stop() throws PlayerException {
		player.getMediaPlayer().stop();
		isPaused = false;
	}
	
	@Override
	public void pause() throws PlayerException {
		player.getMediaPlayer().setPause(true);
		isPaused = true;
	}
	
	@Override
	public void skip(long milliseconds) throws PlayerException {
		player.getMediaPlayer().skip(milliseconds);
	}
	
	public long getPosition() {
		return position/1000;
	}

	@Override
	public long getDuration() {
		return duration/1000;
	}

	@Override
	public float getPositionPercentage() {
		return position/duration;
	}

	@Override
	public String getCurrentUri() {
		return player.getMediaPlayer().getMediaMeta().getUrl();
	}
}
