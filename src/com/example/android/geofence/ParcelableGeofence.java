package com.example.android.geofence;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.Geofence;

public class ParcelableGeofence implements Parcelable {
	
    private final String mId;
    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;
    private String mStringRadius;
	
	public ParcelableGeofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius,
            long expiration,
            int transition) {
        // Set the instance fields from the constructor

        // An identifier for the geofence
        this.mId = geofenceId;

        // Center of the geofence
        this.mLatitude = latitude;
        this.mLongitude = longitude;

        // Radius of the geofence, in meters
        this.mRadius = radius;

        // Expiration time in milliseconds
        this.mExpirationDuration = expiration;

        // Transition type
        this.mTransitionType = transition;
        
        this.mStringRadius = String.valueOf(mRadius);
    }
	
	
	public ParcelableGeofence(Parcel in){
		mId= in.readString();
		mStringRadius= in.readString();
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
		mRadius = in.readFloat();
		mExpirationDuration = in.readLong();
		mTransitionType = in.readInt();
	}
	 /**
     * Get the geofence ID
     * @return A SimpleGeofence ID
     */
    public String getId() {
        return mId;
    }

    /**
     * Get the geofence latitude
     * @return A latitude value
     */
    public double getLatitude() {
        return mLatitude;
    }

    public String getStringRadius(){
    	return mStringRadius;
    }
    /**
     * Get the geofence longitude
     * @return A longitude value
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Get the geofence radius
     * @return A radius value
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * Get the geofence expiration duration
     * @return Expiration duration in milliseconds
     */
    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    /**
     * Get the geofence transition type
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                       .setRequestId(getId())
                       .setTransitionTypes(mTransitionType)
                       .setCircularRegion(
                               getLatitude(),
                               getLongitude(),
                               getRadius())
                       .setExpirationDuration(mExpirationDuration)
                       .build();
    }
    
    @Override
    public String toString() {
        return this.mId;
    }
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mStringRadius);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeInt(mTransitionType);
		dest.writeLong(mExpirationDuration);
		dest.writeFloat(mRadius);
		
		
		
	}
	
	public static final Parcelable.Creator<ParcelableGeofence> CREATOR = new Parcelable.Creator<ParcelableGeofence>()
		    {
		        public ParcelableGeofence createFromParcel(Parcel in)
		        {
		            return new ParcelableGeofence(in);
		        }
		        public ParcelableGeofence[] newArray(int size)
		        {
		            return new ParcelableGeofence[size];
		        }
		    };

}
