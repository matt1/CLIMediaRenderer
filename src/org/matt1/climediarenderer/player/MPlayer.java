package org.matt1.climediarenderer.player;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.matt1.climediarenderer.services.CliMediaRenderer;
import org.matt1.climediarenderer.utils.PropertyHelper;

/**
 * Class that controls "slave" MPlayer instances.  No messing about with JNI!
 * @author Matt
 *
 */
public class MPlayer implements BasicPlayer {

	
	/** Log used to display messaes to the console */
	private static Logger log = Logger.getLogger(CliMediaRenderer.class.getName());
	
	/** Process that is controlling MPlayer */
	Process mPlayer = null;
	
	/** Print Stream for writing commands to MPlayer */
	PrintStream mPlayerStream;
	
	/** Path for the media */
	String mediaPath;
	
	/** Clean up any process we've left behind. */
	protected Thread cleanupThread = new Thread() {
		public void run() {
			cleanup();
		}
	};
	
	/**
	 * Simple test method.
	 * @param args
	 * @throws PlayerException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws PlayerException, InterruptedException {
		MPlayer player = new MPlayer("http://192.168.1.68:50599/disk/DLNA-PNMP3-OP01-FLAGS01700000/O0$1$8I450314.mp3");
		player.play();
		Thread.sleep(10000);
		player.stop();
		
	}
	
	/**
	 * Create a new MPlayer
	 * @param mediaPath
	 * @throws PlayerException
	 */
	public MPlayer(String mediaPath) throws PlayerException {
		try {
			mPlayer = Runtime.getRuntime().exec(PropertyHelper.getMPlayerPath() + " -slave -quiet -idle "); 
			mPlayerStream = new PrintStream(mPlayer.getOutputStream());
			this.mediaPath = mediaPath;
			Runtime.getRuntime().addShutdownHook(cleanupThread);
			log.info("New MPlayer player ready.");
		} catch (IOException e) {
			log.warning("Unable to create MPlayer instance.");
			throw new PlayerException("Unable to instantiate player.");
		}
	}
	
	/**
	 * Clean up any process left over.
	 */
	public void cleanup() {
		if (mPlayer != null) {
			mPlayer.destroy();
		}
	}
	
	/**
	 * Send a command to the media player.  See http://www.mplayerhq.hu/DOCS/tech/slave.txt for
	 * commadnds available
	 * 
	 * @param command
	 */
	private void sendCommand(String command) {
		mPlayerStream.print(command);
		mPlayerStream.print("\n");
		mPlayerStream.flush();

	}
	
	@Override
	public void play() throws PlayerException {
		sendCommand("loadfile \"" + mediaPath + "\" 0");
		log.info("MPlayer player: play");
		

	}

	@Override
	public void stop() throws PlayerException {
		sendCommand("stop");
		log.info("MPlayer player: stop");
	}

	@Override
	public void pause() throws PlayerException {
		sendCommand("pause");
		log.info("MPlayer player: pause");

	}

	@Override
	public void skip(long seconds) throws PlayerException {
		// TODO Auto-generated method stub
		log.info("MPlayer player: skip " + seconds + "s");

	}

	@Override
	public long getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getPositionPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCurrentUri() {
		return mediaPath;
	}

}
