package com.example.android.geofence;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class restartPendingIntents extends Service{
	
	GeofenceRequester mGeofenceRequester;
	SimpleGeofenceStore mGeofenceStore;
	LocationClient mLocationClient;
	SimpleGeofence simpleGeofence;
	Context mContext;
	PendingIntent mCurrentIntent;
	List<Geofence> geofences;
	SharedPreferences mPrefs;
	int itemCount;
	String [] IdList;
	ArrayList<SimpleGeofence> geoList;
	
	public int onStartCommand(Intent intent, int flags, int startId) {
	    super.onStartCommand(intent, flags, startId);
	    mContext = getApplicationContext();
	    mGeofenceStore = new SimpleGeofenceStore(this);
	    geoList= new ArrayList<SimpleGeofence>();
	    mGeofenceRequester = new GeofenceRequester(mContext);
	    String fence = "";
	 //   mCurrentIntent = requester.getRequestPendingIntent();
	    itemCount = getSharedPreferences("Geofencer",MODE_PRIVATE).getInt("geofence_list_count", 0);
	    Log.v("GeofenceList","" + itemCount);
	    IdList = new String [itemCount];
	    for(int i=0; i < itemCount; i++){
	    //	Log.v("Print", "" + getSharedPreferences("Geofencer",MODE_PRIVATE).getString("List_item_" + i, ""));
	    	fence = "" + getSharedPreferences("Geofencer",MODE_PRIVATE).getString("List_item_" + i, "");
        	IdList[i] = fence;       		
        	}
	    for(int i=0; i<IdList.length; i++){
	    	Log.v("GeofenceList",IdList[i]);
	    	Log.v("GeofenceList","" + mGeofenceStore.getGeofences(IdList[i]).getLongitude());
	    	simpleGeofence = new SimpleGeofence(IdList[i],
	    			mGeofenceStore.getGeofences(IdList[i]).getLatitude(),
	    			mGeofenceStore.getGeofences(IdList[i]).getLongitude(),
	    			mGeofenceStore.getGeofences(IdList[i]).getRadius(),
	    			mGeofenceStore.getGeofences(IdList[i]).getExpirationDuration(),
	    			mGeofenceStore.getGeofences(IdList[i]).getTransitionType());
	    	geoList.add(simpleGeofence);
	    }
	        
	    ArrayList<Geofence> geofenceObjs = new ArrayList<Geofence>();
	    for (int i = 0; i < geoList.size(); i++) {
          	 geofenceObjs.add((geoList.get(i)).toGeofence());
    	}
	    try {
            // Try to add geofences
			if(geofenceObjs.size()!=0)
				mGeofenceRequester.addGeofences(geofenceObjs);
			Log.v("INFO", "ADD SUCCESS");
        } catch (Exception e) {
        	Log.v("INFO", "ADD FAILURE");
        	
        }
	    
	    
	    Log.v("TESTING SERVICE","TESTING SERVICE");
	    return(START_REDELIVER_INTENT);
	  }
	
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


	


	

}
