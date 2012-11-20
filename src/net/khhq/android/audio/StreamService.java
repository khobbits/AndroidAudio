package net.khhq.android.audio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.*;
import android.os.IBinder;
import android.util.Log;

public class StreamService extends Service implements OnCompletionListener,
		OnPreparedListener, OnErrorListener {
	
	public URL m_url = null;
	public MediaPlayer m_media_player = null;
	public String m_errors = "";
	public int m_state = 0;
	public static final int STOPPED = 0;
	public static final int STARTING = 1;
	public static final int PLAYING = 2;
	public static final int PAUSED = 3;

	@Override
	public void onCompletion(MediaPlayer mp) {
		m_state = STOPPED;
		Log.d("Complete", "I am now Complete!");
		mp.release();
		if (mp.equals(m_media_player)) {
			m_media_player = null;
		}
		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d("Start", "I am now prepared, lets start!");
		mp.start();
		m_state = PLAYING;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d("Complete",
				"OMG ERROR: " + String.valueOf(what) + " - " + String.valueOf(extra));
		m_errors = "Error with connection to stream";
		return false;
	}

	public StreamService() {
		m_state = STOPPED;
	}

	public void start(String url_string) {
		m_errors = "";
		try {
			m_url = new URL(url_string);
		} catch (MalformedURLException e) {
			m_errors += "Error parsing URL (" + url_string + "): "
					+ e.toString() + "\n";
			Log.e("download", m_errors);
		}

		if (m_errors == "") {
			Log.d("start", "OK I hear you, lets start!");
			m_state = STARTING;
			try {
				if (m_media_player == null) {
					Log.d("start", "I didn't exist yet so let me get prepared.");
					m_media_player = new MediaPlayer();
					m_media_player.setDataSource(url_string);
					m_media_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
					m_media_player.setOnPreparedListener(this);
					m_media_player.setOnCompletionListener(this);
					m_media_player.setOnErrorListener(this);
					m_media_player.prepareAsync();

				} else if (!m_media_player.isPlaying()) {
					Log.d("start", "I exist but i'm not playing? let me fix that...");
					
					if (m_state != STOPPED) {
						Log.d("start", "Killing current connection...");
						m_media_player.reset();
						m_media_player.release();
						m_media_player = null;
						start(url_string);						
					}
					else {
						Log.d("start", "Hitting play");
						m_media_player.start();
						if (m_media_player.isPlaying()) {
							m_state = PLAYING;
						} else {
							m_errors += "Error starting stream: object created but wont restart\n";
						}
					}
				}
			} catch (IOException e) {
				m_errors += "Error starting stream: " + e.toString() + "\n";
				Log.e("start", m_errors, e);
			}
		}
	}

	public IBinder onBind(Intent intent) {
		return m_binder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

	public int state() {
		return m_state;
	}

	public void pause() {
		if (m_media_player != null) {
			if (m_state == PLAYING) {
				m_media_player.pause();
			}
		}
		m_state = PAUSED;
	}

	public void stop() {
		if (m_media_player != null) {
			Log.d("stop", "I existed so I'm cleaning up");

			m_media_player.reset();
			Log.d("stop", "Alright, im cleared up");
			m_media_player.release();
			Log.d("stop", "And now im shut down");
			m_media_player = null;
		}
		m_state = STOPPED;
	}

	private final INStreamService.Stub m_binder = new INStreamService.Stub() {
	    public void start(String url) {
			StreamService.this.start(url);
		}

		public String errors() {
			return m_errors;
		}

		public int state() {
			return StreamService.this.state();
		}

		public void pause() {
			StreamService.this.pause();
		}

		public void stop() {
			StreamService.this.stop();
		}
	};

}
