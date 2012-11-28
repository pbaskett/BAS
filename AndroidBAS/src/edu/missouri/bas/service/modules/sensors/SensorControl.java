package edu.missouri.bas.service.modules.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import edu.missouri.bas.service.ScheduleController;
import edu.missouri.bas.service.SensorService;

//TODO: Convert intent broadcasting to handler
public class SensorControl extends ScheduleController{

	private static final String TAG = "SensorControl";
	
	/*
	 * Sensor variables
	 */
	private SensorManager mSensorManager;
	private SensorEventListener sensorEventListener;
	
	private volatile float[] average = {0.0f, 0.0f, 0.0f};
	private double readings;
	
	Context serviceContext;
	
	public static final String SENSOR_AVERAGE = "INTENT_EXTRA_SENSOR_AVG";
	protected static final String DATA_KEY_AVG = "DATA_KEY_AVG";
	
	public SensorControl(SensorManager sensorManager,
			Context serviceContext, long duration){
		this.duration = duration;
		mSensorManager = sensorManager;
		this.sensorEventListener = new SensorEventListener(){
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {	}
			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
		    	synchronized (average) {
		    		if(running){
		    			Log.d(TAG,"Got sensor reading: "+values[0]+" "+values[1] + " "+values[2]);
		    			readings++;
		    			average[0] += values[0];
		    			average[1] += values[1];
		    			average[2] += values[2];
		    		}
		    	}
			}
		};
		this.serviceContext = serviceContext;
		Log.d(TAG,"Init - "+this.sensorEventListener);
	}
	
	/*public SensorControl(SensorManager sensorManager, 
			SensorEventListener listener){
		mSensorManager = sensorManager;
		this.sensorEventListener = listener;
	}*/
	
	@Override
	protected void setup() {
		average = new float[3];
		readings = 0;
		running = true;
        boolean b = mSensorManager.registerListener(sensorEventListener, 
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG,"Setup "+b);
	}

	@Override
	protected void executeTimer() {
		running = false;
		mSensorManager.unregisterListener(sensorEventListener);
		
		float[] avg = new float[3];
		
		synchronized(average){
			average[0] /= readings;
			average[1] /= readings;
			average[2] /= readings;
			avg[0] = average[0];
			avg[1] = average[1];
			avg[2] = average[2];
		}
		

		Log.d(TAG,"Vals: "+avg[0]+" "+avg[1] + " " + avg[2]);
		
		Intent i = new Intent(SensorService.ACTION_SENSOR_DATA);
		i.putExtra(SENSOR_AVERAGE, avg);
		serviceContext.sendBroadcast(i);
		//serviceContext = null;
	}
}
