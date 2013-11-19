package com.example.android.geofence;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.android.geofence.GeofenceUtils.REMOVE_TYPE;
import com.example.android.geofence.GeofenceUtils.REQUEST_TYPE;
import com.example.android.geofence.MainActivity.ErrorDialogFragment;
import com.example.android.geofence.MainActivity.GeofenceSampleReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class GeofenceListActivity extends Activity {
	
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;
    
    // Store the current request
    private REQUEST_TYPE mRequestType;

    // Store the current type of removal
    private REMOVE_TYPE mRemoveType;
    
    ArrayList<ParcelableGeofence> mCurrentGeofences;
    ArrayList<SimpleGeofence> mRetrievedGeofence;
    ArrayList<String> tempList;
    ArrayList<String> IdList;
    
    private SimpleGeofenceStore mPrefs;
    private static GeofenceRequester mGeofenceRequester;
    private GeofenceRemover mGeofenceRemover;
    private GeofenceSampleReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;
    private List<String> mGeofenceIdsToRemove;
    private ListView listview;
    private boolean checked;
    private ReceiveTransitionsIntentService extraSender;
    int id, itemCount;
    Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geofence_listing);
		id = 0;
		try{
			intent = this.getIntent();
			id = Integer.parseInt(intent.getStringExtra("checked"));
				Log.e("TEST", "" +id);
		}catch(Exception e){
			Log.e("TEST", "try");
		}
		
		//IdList = new ArrayList<String>();
		
		tempList = new ArrayList<String>();
		
		extraSender = new ReceiveTransitionsIntentService();
		
		// Instantiate a Geofence requester
		mGeofenceRequester = new GeofenceRequester(this);
		
		// Create a new broadcast receiver to receive updates from the listeners and service
		mBroadcastReceiver = new GeofenceSampleReceiver();
		
		// Instantiate a new geofence storage area
		mPrefs = new SimpleGeofenceStore(this);
		
		// Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);
        
        mRetrievedGeofence = new ArrayList<SimpleGeofence>();
        
        listview = (ListView) findViewById(R.id.listView);
        
        IdList = new ArrayList<String>();
      
        if(id != 1){
    	GeofenceLoader geode = new GeofenceLoader();

		geode.execute(mPrefs);

		checked = true;
        }
        Log.d("TEST", "Check MAIN ON CREATE" );
        try{
       // if(mPrefs.getChecked("checked").equals("0"))
     //   refillIdList();
        } catch(NullPointerException e){
        	
        }
        
        getPreferences(MODE_PRIVATE).edit().putString("","Preference Test 1").commit();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TEST", "Check Resume");
        try{
	        if(id == 1){
	        	refillIdList();
	        }
	        } catch(NullPointerException e){
	        	
	        } 
                
    }
	
	
	@Override
	protected void onStart(){
		super.onStart();
        Log.e("LAST TEST","" + 1);
        Log.e("LAST TEST","" + mPrefs.getCheckedInt("test"));
        Log.e("LAST TEST","" + 2);  
	}
	
	@Override
	protected void onRestart(){
		super.onRestart();	
	//	Log.d("COUNT AFT", "" + itemCount);
		 Log.d("TEST", "Checking On Restart");
		 try{
		    //    if(mPrefs.getChecked("checked").equals("0"))
		     //   refillIdList();
		        } catch(NullPointerException e){
		        	
		        }
	}

    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TEST", "Check Pause");
        int count = 0;
        for(int i = 0; i< mRetrievedGeofence.size(); i++, count++){
        	 mPrefs.setGeofence(mRetrievedGeofence.get(i).getId(), mRetrievedGeofence.get(i));
        	 
        	 checked = true;
        }
        Log.d("COUNT BEF", "" + count);
        getPreferences(MODE_PRIVATE).edit().putInt("geofence_list_count", count).commit();
       
      // 
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	 getPreferences(MODE_PRIVATE).edit().putInt("checker", 1).commit();
    }
	
	
	
	public void refillIdList(){
		
		int itemCount = mPrefs.getCheckedInt("geofence_list_count");
		Log.d("COUNT AFT","" + itemCount);
        if(IdList.size()==0){
        for(int i=0; i < 2; i++){
        	Log.d("Print", "" + mPrefs.getChecked("List_item_" + i));
        	IdList.add(getPreferences(MODE_PRIVATE).getString("List_item_" + i, ""));
        		
        	}
       }
 		for(int i = 0;i< IdList.size();i++){
        	Log.e("TESTING","" + IdList.get(i));    
        } 
 		populateListView(listview);
	}
	
	
	
	@Override
    public void onSaveInstanceState(Bundle savedState) { 
        savedState.putStringArrayList("IdList", IdList);
        savedState.putString("Id", "TEST");
        // ... save more data
        Log.d("TEST", "Checking Save Instance set");
        super.onSaveInstanceState(savedState);
        Log.d("TEST", "Checking Save Instance set 2");
    }
	
	@Override
    public void onRestoreInstanceState(Bundle InstanceState) {
        super.onRestoreInstanceState(InstanceState);
        Log.d("TEST", "Checking Save Instance set 4");
        IdList = InstanceState.getStringArrayList("IdList");
        for(int i = 0;i< IdList.size();i++){
        	Log.e("TESTING","" + IdList.get(i));
        	
        	populateListView(listview);
        }
    }
	
	
	
	public void populateListView(ListView geolistview){
		
		final StableArrayAdapter adapter = new StableArrayAdapter(GeofenceListActivity.this,
 		        android.R.layout.simple_list_item_1, IdList);
 		    geolistview.setAdapter(adapter);
 		    
 		   geolistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

   		     public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
   		                             long id) {

   		         // We know the View is a TextView so we can cast it
   		         TextView clickedView = (TextView) view;

   		         Toast.makeText(GeofenceListActivity.this, "Item with id ["+id+"] - Position ["+position+"] - Geofence ["+clickedView.getText()+"]", Toast.LENGTH_SHORT).show();
   		         ParcelableGeofence partGeo = mPrefs.getGeofence((String)(clickedView.getText()));
   		        Intent intent = new Intent(getApplicationContext(), GeofenceViewActivity.class);
   		       //     Bundle b = new Bundle();
   		        
   		        Log.i("Radius","" + partGeo.getRadius());
   		            intent.putExtra("data", partGeo);
   		     //       intent.putExtras(b);
   		            startActivity(intent);
   		     }
   		});
	}
	
	
	
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        ArrayList<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); i++) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }
	
	
	private class GeofenceLoader extends AsyncTask<SimpleGeofenceStore, Void, ArrayList<SimpleGeofence>> {

        @Override
        protected ArrayList<SimpleGeofence> doInBackground(SimpleGeofenceStore... params) {
        	Document doc;
        	ArrayList<SimpleGeofence> mGeofences = new ArrayList<SimpleGeofence>();
        	
        	try{
        		if(isNetworkAvailable()){
		        	doc = getDocument();
		        	mGeofences = getGeofences(doc);
        		}
        	}catch (RuntimeException e){
        		
       		
        	}
        	
   
        	

              return mGeofences;
        }      

        @Override
        protected void onPostExecute(ArrayList<SimpleGeofence> result) {
        	
        	IdList = new ArrayList<String>();
        	 mRetrievedGeofence = result;
     		ArrayList<Geofence> geofenceObjs = new ArrayList<Geofence>();     		
    		for (int i = 0; i < result.size(); i++) {
           	 geofenceObjs.add((result.get(i)).toGeofence());
             mPrefs.setGeofence(result.get(i).getId(),result.get(i));
     		}
    		
        
    		
    		if(id != 1){
	    		try {
	                // Try to add geofences
	    			if(geofenceObjs.size()!=0)
	                mGeofenceRequester.addGeofences(geofenceObjs);
	                id = 1;
	            } catch (UnsupportedOperationException e) {
	            	
	            }
    		}

             
             
        	 String mId;
        	 for (int i = 0; i < result.size(); ++i) {
     			mId = result.get(i).getId();
     	        IdList.add(mId);
     	        getPreferences(MODE_PRIVATE).edit().putString("List_item_" + i, mId).commit();
     		}
        	 populateListView(listview);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
  }  
	
	public Document getDocument() {
		
        try {
        	HttpURLConnection urlConnection = null;
            URL url = new URL("http://54.213.80.98:8080/geofences.xml");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            InputStream in = urlConnection.getInputStream();
            urlConnection.connect();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(GeofenceListActivity.this, "Server Connection is Down", Toast.LENGTH_SHORT).show();
        }
		return null;
	}


	public static ArrayList<SimpleGeofence> getGeofences (Document doc) {
		NodeList nl1, nl2;
		ArrayList<SimpleGeofence> geofenceList = new ArrayList<SimpleGeofence>();
		SimpleGeofence fence; 
		
        nl1 = doc.getElementsByTagName("GEOFENCE");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();
                
                Node IDNode = nl2.item(getNodeIndex(nl2, "ID"));
                String id = IDNode.getTextContent();
                Node latNode = nl2.item(getNodeIndex(nl2, "LATITUDE"));
                double lat = Double.parseDouble(latNode.getTextContent());
                Node lngNode = nl2.item(getNodeIndex(nl2, "LONGITUDE"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                Node radNode = nl2.item(getNodeIndex(nl2, "RADIUS"));
                float rad = Float.parseFloat(radNode.getTextContent());
                
                fence = new SimpleGeofence(id,
						lat,
						lng,
						rad, 
						GEOFENCE_EXPIRATION_IN_MILLISECONDS,
						Geofence.GEOFENCE_TRANSITION_ENTER);
                
                geofenceList.add(fence);
                 
            }
            
        }
        return geofenceList;
	}
            
            private static int getNodeIndex(NodeList nl, String nodename) {
        		for(int i = 0 ; i < nl.getLength() ; i++) {
        			if(nl.item(i).getNodeName().equals(nodename))
        				return i;
        		}
        		return -1;
        	}    
	
	public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    ||
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                        }
                    break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(GeofenceUtils.APPTAG,
                       getString(R.string.unknown_activity_request_code, requestCode));

               break;
        }
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            // Request to clear the geofence1 settings in the UI
            case R.id.menu_item_refresh_list:
            	id = 0;
            	try{
            	GeofenceLoader geode = new GeofenceLoader();
        		geode.execute(mPrefs);
            	}catch(Exception e){
            		
            	}
                return true;

      /*      // Remove all geofences from storage
            case R.id.menu_item_clear_geofence_history
                return true;*/

            // Pass through any other request
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
