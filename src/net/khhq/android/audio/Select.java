package net.khhq.android.audio;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class Select extends Activity {
	
	public static final String PREFS_NAME = "AndroidAudio";
	public EditText m_url_editor;
	public Button m_button1;
	public Spinner m_spinner1;
	public String m_url;
	private String array_spinner[];
	private SharedPreferences settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);
		
		m_button1 = (Button) findViewById(R.id.button1);
		m_button1.setOnClickListener(clickcontinue);
				
		settings = getSharedPreferences(PREFS_NAME, 0);
	    	    
	    array_spinner=new String[5];
	    array_spinner[0]=settings.getString("lastserver1", "http://192.168.0.2:8124");
	    array_spinner[1]=settings.getString("lastserver2", "http://192.168.1.2:8124");
	    array_spinner[2]=settings.getString("lastserver3", "http://192.168.2.2:8124");
	    array_spinner[3]=settings.getString("lastserver4", "http://192.168.0.20:8124");
	    array_spinner[4]="...";
	    
	    m_url = array_spinner[0];
	    
	    m_spinner1 = (Spinner) findViewById(R.id.spinner1);
	    m_spinner1.setOnItemSelectedListener(selecteditem);
	    
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	    		android.R.layout.simple_spinner_item, array_spinner);
	    m_spinner1.setAdapter(adapter);
		
		m_url_editor = (EditText) findViewById(R.id.url_editor);
		m_url_editor.setText(m_url);
		
		
		
	}
	
	private OnItemSelectedListener selecteditem = new OnItemSelectedListener() {
		@Override
		  public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {

            String selection = parent.getItemAtPosition(pos).toString();
            /*
             * Set the value of the text field in the UI
             */           
            if (selection.equalsIgnoreCase("...")) {            	
            	m_url_editor.setVisibility(View.VISIBLE);
            } else {
            	m_url_editor.setText(selection);
            	m_url_editor.setVisibility(View.GONE);
            }
        }

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
						
		}
	};
	
	private void SavePreferences(String key, String value){
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(key, value);
	    editor.commit();
	}
	
	private OnClickListener clickcontinue = new OnClickListener() {
		public void onClick(View v) {
			if (!m_url_editor.getText().toString().equalsIgnoreCase(array_spinner[0])) {
				Log.i("Save", "Saving recent server");
				SavePreferences("lastserver1", m_url_editor.getText().toString());
				SavePreferences("lastserver2", array_spinner[0]);
				SavePreferences("lastserver3", array_spinner[1]);
				SavePreferences("lastserver4", array_spinner[2]);
			}
			else {
				Log.i("Save", "Used last server, wont bother updating list.");	
			}
			finish();
			Intent intent = new Intent("net.khhq.android.audio.Controls");
			intent.putExtra("net.khhq.android.audio.URL", m_url_editor.getText().toString());
			startActivity(intent);
		}
	};

}