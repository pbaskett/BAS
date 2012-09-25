package edu.missouri.bas.service.modules.location;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationControl {
	
	private final int TIME_THRESHOLD;
	private final int ACCURACY_THRESHOLD;
	
	private LocationManager locationManager;
	private volatile boolean running = false;
	private Location bestLocation;
	
	int checkInterval;
	float minDistance;
	
	Timer locationTimer;
	
	long duration;
	
	Context serviceContext;
	
	public static final String LOCATION_INTENT_KEY = "location_intent_key";
	public static final String INTENT_ACTION_LOCATION ="intent_action_location";
	
	public LocationControl(Context serviceContext,
			LocationManager locationManager, int time, int accuracy, long duration){
		this.locationManager = locationManager;
		this.serviceContext = serviceContext;
		this.duration = duration;
		TIME_THRESHOLD = time;
		ACCURACY_THRESHOLD = accuracy;
		bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
	public void startRecording(){
		if(locationTimer != null) locationTimer.cancel();
		locationTimer = new Timer();
		
		for(String provider: locationManager.getAllProviders()){
			if(provider != LocationManager.NETWORK_PROVIDER)
				locationManager.requestLocationUpdates(provider,
					checkInterval, minDistance, locationListener);
		}
		
		running = true;
		
		locationTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				locationManager.removeUpdates(locationListener);
				running = false;
				Location bestLocation = getLastCachedBestLocation();
				Intent i = new Intent(INTENT_ACTION_LOCATION);
				i.putExtra(LOCATION_INTENT_KEY, bestLocation);
				serviceContext.sendBroadcast(i);
			}
		}, duration);
	}
	
	public boolean isRunning(){
		return running;
	}
	
	public Location getLastCachedNetworkLocation(){
		return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
	public Location getLastCachedGPSLocation(){
		return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	public Location getLastCachedBestLocation(){
		synchronized(bestLocation){
			return bestLocation;
		}
	}
	
	public Location updateBestLocation(Location newLocation, Location bestLocation){
		if(running == false) return bestLocation;
		if(bestLocation == null) return newLocation;
		
		long timeDifference = newLocation.getTime() - bestLocation.getTime();
		boolean isNewer = timeDifference > 0;
		
		if(timeDifference > TIME_THRESHOLD)
			return newLocation;
		else if(timeDifference < -TIME_THRESHOLD)
			return bestLocation;
		
		int accuracyDifference = (int)(newLocation.getAccuracy() - bestLocation.getAccuracy());
		boolean fromSameProvider = isSameProvider(newLocation.getProvider(), bestLocation.getProvider());
		
		if(accuracyDifference < 0){
			return newLocation;
		}
		else if(isNewer && (accuracyDifference == 0)){
			return newLocation;
		}
		else if(isNewer && (accuracyDifference <= ACCURACY_THRESHOLD) && fromSameProvider){
			return newLocation;
		}
		
		return bestLocation;
	}
	
	private boolean isSameProvider(String provider1, String provider2) {
		if(provider1 == null) return provider2 == null;
		return provider1 == provider2;
	}
	
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.d("LocationControl","Got new location");
			synchronized(bestLocation){
				bestLocation = updateBestLocation(location, bestLocation);
			}
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	};
}
