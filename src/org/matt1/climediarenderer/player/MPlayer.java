package org.matt1.climediarenderer.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

	
	/** Log used to display messages to the console */
	private static Logger log = Logger.getLogger(CliMediaRenderer.class.getName());
	
	/** Process that is controlling MPlayer */
	private static Process mPlayer;
	
	/** Print Stream for writing commands to MPlayer */
	PrintStream mPlayerStream;
	
	/** Output stream from the player */
	BufferedReader mPlayerOutput;
	
	/** Path for the media */
	String mediaPath;
	
	/**
	 * Thread to try and cleanup child processes on exit
	 */
	protected static Thread cleanupThread = new Thread() {
		public void run() {
			try {
				getInstance().destroy();
			} catch (IOException e) {
				log.warning("Unable to kill child MPlayer process");
			}
		}
	};
	
	/** Get a single instance of the media player */
	protected static Process getInstance() throws IOException {
		if (mPlayer == null) {
			mPlayer = Runtime.getRuntime().exec(PropertyHelper.getMPlayerPath() + " -slave -quiet -idle "); // + mediaPath);
			Runtime.getRuntime().addShutdownHook(cleanupThread);
		}
		return mPlayer;
	}
	
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
	
	public MPlayer(String mediaPath) throws PlayerException {
		try {
			mPlayer = getInstance();
			mPlayerStream = new PrintStream(mPlayer.getOutputStream());
			mPlayerOutput = new BufferedReader(new InputStreamReader(mPlayer.getInputStream()));
			this.mediaPath = mediaPath;
			log.info("New MPlayer player ready.");
		} catch (IOException e) {
			log.warning("Unable to create MPlayer instance.");
			throw new PlayerException("Unable to instantiate player.");
		}
	}
	
	/**
	 * Send a command to the media player.  See http://www.mplayerhq.hu/DOCS/tech/slave.txt for
	 * commadnds available
	 * 
	 * @param command
	 */
	private String sendCommand(String command, Boolean waitForResponse) {
		mPlayerStream.print(command);
		mPlayerStream.print("\n");
		mPlayerStream.flush();

		String result = "";
		
		if (waitForResponse) {
			try {
				String output ="";
				while ((output = mPlayerOutput.readLine()) != null) {
					if (output.startsWith("ANS_")){
						result = output;
						log.info("Got answer" + result);
						break;
					}
				}
				
			} catch (IOException e) {
				log.warning("Failure reading MPlayer output");
			}

		} 
		
		return result;
		
	}
	
	@Override
	public void play() throws PlayerException {
		sendCommand("loadfile \"" + mediaPath + "\" 0", false);
		log.info("MPlayer player: play");
		

	}

	@Override
	public void stop() throws PlayerException {
		sendCommand("stop", false);
		log.info("MPlayer player: stop");
	}

	@Override
	public void pause() throws PlayerException {
		sendCommand("pause", false);
		log.info("MPlayer player: pause");

	}

	@Override
	public void skip(long seconds) throws PlayerException {
		log.info("MPlayer player: skip " + seconds + "s");

	}

	@Override
	public long getPosition() {
		String length = sendCommand("get_time_pos", true);
		if (length.indexOf("ANS_TIME") > -1) {
			String pos = length.substring("ANS_TIME_POSITION=".length());
			return Long.valueOf(pos);
		}
		return 0;
	}

	@Override
	public long getDuration() {
		String length = sendCommand("get_property length", true);
		if (length.indexOf("ANS_length") > -1) {
			String seconds = length.substring("ANS_length=".length());
			return Long.valueOf(seconds);
		}
		return 0;
	}

	@Override
	public float getPositionPercentage() {
		String length = sendCommand("get_percent_pos", true);
		if (length.indexOf("ANS_PERCENT") > -1) {
			String pos = length.substring("ANS_PERCENT_POSITION=".length());
			return Long.valueOf(pos);
		}
		return 0;
	}

	@Override
	public String getCurrentUri() {
		return mediaPath;
	}

}
