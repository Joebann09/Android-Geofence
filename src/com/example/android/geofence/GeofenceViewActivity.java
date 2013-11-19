package com.example.android.geofence;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class GeofenceViewActivity extends Activity {

	ParcelableGeofence mGeofence ;
	TextView geofenceID;
	TextView geofenceLong;
	TextView geofenceLat;
	TextView geofenceRad;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geofence_view);
				 
		mGeofence = this.getIntent().getParcelableExtra("data");
		Log.i("Radius here","" + mGeofence.getRadius());
		geofenceID = (TextView) findViewById(R.id.geofenceID);
		geofenceLong = (TextView) findViewById(R.id.geofenceLat);
		geofenceLat = (TextView) findViewById(R.id.geofenceLong);
		geofenceRad = (TextView) findViewById(R.id.geofenceRad);
		geofenceID.setText(mGeofence.getId());
		geofenceLong.setText(String.valueOf(mGeofence.getLatitude()));
		geofenceLat.setText(String.valueOf(mGeofence.getLongitude()));
		geofenceRad.setText( mGeofence.getStringRadius());
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.geofence_view, menu);
		return true;
	}

}
