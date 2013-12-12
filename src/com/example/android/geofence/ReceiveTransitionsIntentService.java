package com.example.android.geofence;


import java.util.Arrays;
import java.util.List;




import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;


/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {
	private int numFences;
	String[] geofenceIds;
	

	  public int onStartCommand(Intent intent, int flags, int startId) {
	    super.onStartCommand(intent, flags, startId);
	    Log.v("TESTING SERVICE","TESTING SERVICE");
	    return(START_REDELIVER_INTENT);
	  }


    /**
     * Sets an identifier for this class' background thread
     */
	
    public ReceiveTransitionsIntentService() {
    	
        super("ReceiveTransitionsIntentService");
        Log.d(this.getClass().getName(), "ReceiveTransitionsIntentService()");
        Log.d("TEST", "");
    }
    
    public void addFenceNum(int temp){
    	numFences = temp;
    }

    
    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	Log.d(this.getClass().getName(), "onHandleIntent");
    	
    	
        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();
        
        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
        Log.d(this.getClass().getName(), "TEST1");
        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code
            int errorCode = LocationClient.getErrorCode(intent);
            // Get the error message
            String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
            // Log the error
            Log.e(GeofenceUtils.APPTAG,
                    getString(R.string.geofence_transition_error_detail, errorMessage)
            );

            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        // If there's no error, get the transition type and create a notification
        } else {
            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (
                    (transition == Geofence.GEOFENCE_TRANSITION_ENTER)
                    ||
                    (transition == Geofence.GEOFENCE_TRANSITION_EXIT)
               ) {
            	 List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                 geofenceIds = new String[geofences.size()];
                 for (int index = 0; index < geofences.size() ; index++) {
                     geofenceIds[index] = geofences.get(index).getRequestId();
                 }
            	
            	if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
            		sendTransit(getUniqueID(getApplicationContext()), 1);
            	}
            	else{
            		sendTransit(getUniqueID(getApplicationContext()), 0);
            	}

                // Post a notification
               
                String ids = TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER,geofenceIds);
                String transitionType = getTransitionString(transition);

                sendNotification(transitionType, ids);
                // Log the transition type and a message
                Log.d(GeofenceUtils.APPTAG,
                        getString(
                                R.string.geofence_transition_notification_title,
                                transitionType,
                                ids));
                Log.d(GeofenceUtils.APPTAG,
                        getString(R.string.geofence_transition_notification_text));

            // An invalid transition was reported
            } else {
                // Always log as an error
                Log.e(GeofenceUtils.APPTAG,
                        getString(R.string.geofence_transition_invalid_type, transition));
            }
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * @param transitionType The type of transition that occurred.
     *
     */
    private void sendNotification(String transitionType, String ids) {

        // Create an explicit content Intent that starts the main Activity
    	Intent notificationIntent =
                new Intent(getApplicationContext(),GeofenceListActivity.class);
    		notificationIntent.putExtra("checked", "1");
        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the main Activity to the task stack as the parent
    //    stackBuilder.addParentStack(GeofenceListActivity.class);

        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.ic_notification)
               .setContentTitle(
                       getString(R.string.geofence_transition_notification_title,
                               transitionType, ids))
               .setContentText(getString(R.string.geofence_transition_notification_text))
               .setContentIntent(notificationPendingIntent);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        
        builder.setAutoCancel(true);
        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }
    
    private static String getUniqueID(Context context) {

	    String telephonyDeviceId = "NoTelephonyId";
	    String androidDeviceId = "NoAndroidId";

	    // get telephony id
	    try {
	        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	        telephonyDeviceId = tm.getDeviceId();
	        if (telephonyDeviceId == null) {
	            telephonyDeviceId = "NoTelephonyId";
	        }
	    } catch (Exception e) {
	    }

	    // get internal android device id
	    try {
	        androidDeviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
	                android.provider.Settings.Secure.ANDROID_ID);
	        if (androidDeviceId == null) {
	            androidDeviceId = "NoAndroidId";
	        }
	    } catch (Exception e) {

	    }

	    // build up the uuid
	    try {
	        String id = getStringIntegerHexBlocks(androidDeviceId.hashCode())
	                + "-"
	                + getStringIntegerHexBlocks(telephonyDeviceId.hashCode());

	        return id;
	    } catch (Exception e) {
	        return "0000-0000-1111-1111";
	    }
	}
    

	public static String getStringIntegerHexBlocks(int value) {
	    String result = "";
	    String string = Integer.toHexString(value);

	    int remain = 8 - string.length();
	    char[] chars = new char[remain];
	    Arrays.fill(chars, '0');
	    string = new String(chars) + string;

	    int count = 0;
	    for (int i = string.length() - 1; i >= 0; i--) {
	        count++;
	        result = string.substring(i, i + 1) + result;
	        if (count == 4) {
	            result = "-" + result;
	            count = 0;
	        }
	    }

	    if (result.startsWith("-")) {
	        result = result.substring(1, result.length());
	    }

	    return result;
	}
	
	
	public void sendTransit(String deviceId, int transition) {
		for(int i=0; i<geofenceIds.length; i++){
		String url = "http://54.213.80.98:8080/input.html?dID=" + deviceId +"&transition=" + transition + "&location=" + geofenceIds[i];
		
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpClient.execute(httpPost, localContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
		}
	}
}
