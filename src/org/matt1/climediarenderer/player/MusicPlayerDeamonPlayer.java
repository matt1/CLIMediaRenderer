package org.matt1.climediarenderer.player;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.a0z.mpd.MPD;
import org.a0z.mpd.exception.MPDClientException;
import org.a0z.mpd.exception.MPDServerException;
import org.matt1.climediarenderer.utils.PropertyHelper;

/**
 * BasicPlayer that uses the Music Player Daemon player
 * 
 * @author Matt
 *
 */
public class MusicPlayerDeamonPlayer implements BasicPlayer {

	/** MPD client instance */
	private MPD mpdPlayer;
	
	
	public static void main(String[] args) throws MPDServerException, MPDClientException, MalformedURLException, PlayerException, InterruptedException, UnknownHostException {
		
		MusicPlayerDeamonPlayer player = new MusicPlayerDeamonPlayer("http://monkeydrive:50599/disk/DLNA-PNMP3-OP01-FLAGS01700000/O0$1$8I450314.mp3");
		player.play();
		Thread.sleep(10000);
		player.stop();
		player.cleanup();
		
	}
	
	public MusicPlayerDeamonPlayer(String path) throws 
		MPDServerException, MPDClientException, MalformedURLException, UnknownHostException {
		getInstance().getPlaylist().clear();
		getInstance().getPlaylist().add(new URL(path));
		
	}
	
	/**
	 * Gets a new connection and a new player instance from MPD
	 * @return
	 * @throws UnknownHostException
	 * @throws MPDServerException 
	 */
	private MPD getInstance() throws MPDServerException, UnknownHostException {
		if (mpdPlayer == null) {
			try {
				mpdPlayer = new MPD(PropertyHelper.getInstance().getMPDHost(), PropertyHelper.getInstance().getMPDPort());
			} catch (Exception e) {
				throw new MPDServerException("Unabled to load MPD config from settings: " + e.getMessage(), e);
			}
		}
		return mpdPlayer;
	}
	
	@Override
	public void play() throws PlayerException {
		try {
			getInstance().play();
		} catch (Exception e) {
			throw new PlayerException(e.getMessage());
		}

	}

	@Override
	public void stop() throws PlayerException {
		try {
			getInstance().stop();
		} catch (Exception e) {
			throw new PlayerException(e.getMessage());
		}

	}

	@Override
	public void pause() throws PlayerException {
		try {
			getInstance().pause();
		} catch (Exception e) {
			throw new PlayerException(e.getMessage());
		}

	}

	@Override
	public void skip(long seconds) throws PlayerException {
		
	}

	@Override
	public long getPosition() {
		long position = 0;
		try {
			position = getInstance().getStatus().getSongPos();
		} catch (Exception e) {
			// swallow it for now
		} 
		
		return position;
		
	}

	@Override
	public long getDuration() {
		long duration = 0;
		try {
			duration = getInstance().getStatus().getTotalTime();
		} catch (Exception e) {
			// swallow it for now
		} 
		
		return duration;
	}

	@Override
	public float getPositionPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCurrentUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanup() {
		try {
			if (getInstance().isConnected()) {
				getInstance().stop();
				getInstance().disconnect();
			}
			
		} catch (Exception e) {
			// what now?
		} 
			
		
		

	}

}
