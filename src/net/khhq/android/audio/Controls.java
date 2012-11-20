package net.khhq.android.audio;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class Controls extends Activity {

	public TextView m_output;
	public TextView m_position;
	public TextView m_np;
	public Context m_context;
	public String m_url;
	public ImageButton m_play_button;
	public ImageButton m_stop_button;
	public INStreamService m_service = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controls);
		
		m_url = getIntent().getStringExtra("net.khhq.android.audio.URL");
		m_position = (TextView) findViewById(R.id.position);
		m_output = (TextView) findViewById(R.id.output);
		m_np = (TextView) findViewById(R.id.np);
		m_play_button = (ImageButton) findViewById(R.id.play);
		m_play_button.setOnClickListener(clickstart);
		m_play_button.setImageResource(android.R.drawable.ic_media_play);
		m_stop_button = (ImageButton) findViewById(R.id.stop);
		m_stop_button.setOnClickListener(clickstop);
		m_stop_button.setImageResource(android.R.drawable.ic_lock_power_off);
		m_context = getApplicationContext();
		bindService(new Intent(m_context, StreamService.class),
				m_service_connection, Context.BIND_AUTO_CREATE);
		m_run_refresh.run();
	}

	private ServiceConnection m_service_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder service) {
			m_service = INStreamService.Stub.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
			m_service = null;
		}
	};

	private final Runnable m_run_refresh = new Runnable() {
		public void run() {
			refresh();
			m_handler.postDelayed(this, 1000);
		}
	};

	private final Handler m_handler = new Handler();

	private OnClickListener clickstart = new OnClickListener() {
		public void onClick(View v) {
			if (m_service == null) {
				return;
			}
			try {
				int state = m_service.state();
				if (state == StreamService.PLAYING) {
					Log.d("click", "pausing");
					m_service.pause();
				} else {
					Log.d("click", "starting");
					m_service.start(m_url);
				}
			} catch (RemoteException e) {
				Log.e("onclick", "Error connecting", e);
				m_output.setText("Error connecting to StreamService: "
						+ e.toString() + "\n");
			}
		}
	};

	private OnClickListener clickstop = new OnClickListener() {
		public void onClick(View v) {
			Log.d("click", "you want me to stop :(");
			if (m_service == null) {
				return;
			}
			try {
				m_service.stop();
			} catch (RemoteException e) {
				Log.e("onclick", "Error connecting", e);
				m_output.setText("Error connecting to StreamService: "
						+ e.toString() + "\n");
			}

		}
	};

	public void refresh() {
		if (m_service == null) {
			return;
		}
		try {
			String errors = m_service.errors();
			String status = "";
			String np = "";
			String pos = "";
			int state = m_service.state();			
			
			if (state == StreamService.STOPPED) {
				m_play_button
						.setImageResource(android.R.drawable.ic_media_play);
				status = "Stopped";
			} else if (state == StreamService.PAUSED) {
				m_play_button
						.setImageResource(android.R.drawable.ic_media_play);
				status = "Paused";
			} else if (state == StreamService.STARTING) {
				m_play_button
						.setImageResource(android.R.drawable.ic_media_play);
				status = "Connecting...";
			} else {
				m_play_button
						.setImageResource(android.R.drawable.ic_media_pause);
				status = "Playing " + m_url;
				np = "Now playing:";
				pos = "Spotify";
			}
			
			m_np.setText(np);
			m_position.setText(pos);
			if (errors.length() > 1) {
				m_output.setText(status + " - " + errors);
			} else {
				m_output.setText(status);
			}
		} catch (RemoteException e) {
			Log.e("refresh", "Error connecting", e);
			m_output.setText("Error connecting to StreamService: "
					+ e.toString() + "\n");
		}
	}
}