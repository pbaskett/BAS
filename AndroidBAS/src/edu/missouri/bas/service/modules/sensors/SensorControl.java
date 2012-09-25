package edu.missouri.bas.service.modules.sensors;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import edu.missouri.bas.service.SensorService;

public class SensorControl {
	 
	/*
	 * Information about the control
	 */
	private SensorControlSettings controlSettings;
	
	/*
	 * Sensor variables
	 */
	private SensorManager mSensorManager;
	private SensorEventListener sensorEventListener;
	
	/*
	 * File I/O Variables 
	 */
	private File sensorFile;
	private FileWriter fileWriterSensor;
	private BufferedWriter bufferedWriterSensor;
	
	/*
	 * Recording variables
	 */
	//private long duration;
	private boolean isRecording = false;
	//private long startTime;
	private final String TAG = "SensorControl";
	
	private volatile double[] average = {0.0, 0.0, 0.0};
	private double readings;
	
	StringBuilder currentString;
	StringBuilder prevString;
	
	public static final String SENSOR_AVERAGE = "INTENT_EXTRA_SENSOR_AVG";

	protected static final String DATA_KEY_AVG = "DATA_KEY_AVG";
	
	public SensorControl(File sensorFile,
			SensorManager sensorManager) throws IOException{
		Log.d(TAG,"SensorControl created");
		controlSettings = new SensorControlSettings(sensorFile);
		this.sensorFile = sensorFile;
		mSensorManager = sensorManager;
		
		prepareIO();
		prepareListener(sensorEventListener);
	}
	
	public boolean startRecording(long startTime, long recordDuration){
		if(!isRecording){
			//this.startTime = startTime;
			//this.duration = recordDuration;
			registerSensors();
			isRecording = true;
			average = new double[3];
			return true;
		}
		return false;
	}
	
	public boolean startRecording(long startTime){
		if(!isRecording){
			//this.startTime = startTime;
			registerSensors();
			isRecording = true;
			average = new double[3];
			readings = 0;
			return true;
		}
		return false;
	}
	
	public boolean stopRecording(){
		if(isRecording){
			isRecording = false;
			//duration = Long.MIN_VALUE;
			unregisterSensors();
			average[0] /= readings;
			average[1] /= readings;
			average[2] /= readings;
			return true;
		}
		return false;
	}
	
	public SensorControlSettings getRecordParameters(){
		return controlSettings.clone();	
	}
	
	public class SensorControlSettings{
		public SensorControlSettings(){
			
		}
		
		public SensorControlSettings(File sensorFile) {
			
		}

		@Override
		public SensorControlSettings clone(){
			return controlSettings;
			
		}
	}

	public boolean isRecording(){
		return isRecording;
	}
	
	public void forceStop(){
		unregisterSensors();
		if(bufferedWriterSensor != null){
			try {
				bufferedWriterSensor.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriterSensor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void registerSensors() {
        mSensorManager.registerListener(sensorEventListener, 
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 10);
        /*mSensorManager.registerListener(sensorEventListener, 
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 10);        
        mSensorManager.registerListener(sensorEventListener, 
                mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), 10);*/
        Log.d(TAG,"Sensors registered");
	}
	
	private void unregisterSensors(){
		mSensorManager.unregisterListener(sensorEventListener);
		Log.d(TAG,"Sensors unregistered");
	}
	
	private void prepareIO() throws IOException{
		//fileWriterSensor = new FileWriter(sensorFile, true);
    	//bufferedWriterSensor = new BufferedWriter(fileWriterSensor);	
    	Log.d(TAG,"IO prepared");
	}
	
	private void prepareListener(SensorEventListener sensorListener){
		sensorEventListener = new SensorEventListener(){
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {	}
			@Override
			public void onSensorChanged(SensorEvent event) {
		    	//Sensor sensor = event.sensor;
		    	//int type = sensor.getType();
		    	//String sensorName = sensor.getName();
				float[] values = event.values;
				//long sensorTime = event.timestamp;
		    	synchronized (this) {
		    		//Log.d("SensorEventListener","Sensor event received");
		    		if(isRecording){
		    			readings++;
		    			average[0] += values[0];
		    			average[1] += values[1];
		    			average[2] += values[2];
			    		/*try {
							bufferedWriterSensor.write(type + " " + sensorName + " " + sensorTime);
							for(float f: values){
								bufferedWriterSensor.write(" "+ f);
							}
							bufferedWriterSensor.write("\n");
							bufferedWriterSensor.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}*/
		    		}
		    		else{
		    			Log.e("TAG","Sensor event after control stopped");
		    			//unregisterSensors();
		    			//isRecording = false;
		    		}
		    	}
			}
		};
	}
	protected double[] getAverage(){
		double[] avg = {average[0], average[1], average[2]};
		return avg;
	}
	
	public static class SensorControlTask implements Runnable{
		private final String TAG = "SensorControlTask";
		private long stopTime;
		private long duration;
		SensorControl sensorControl;
		private Context serviceContext;
		private volatile boolean running;
		
		public SensorControlTask(long duration, SensorControl control, Context context){
			this.sensorControl = control;
			this.duration = duration;
			this.stopTime = System.currentTimeMillis()+duration;
			this.serviceContext = context;
		}
		
		@Override
		public void run() {
			while(running){
				if(!sensorControl.isRecording()){

					sensorControl.startRecording((stopTime = System.currentTimeMillis()), duration);
					Log.d(TAG,"SensorControl started");
					//stopTime += duration;
					try {
						Thread.sleep(duration);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sensorControl.stopRecording();
					//Broadcast this?
					double[] average = sensorControl.getAverage();
					Intent i = new Intent(SensorService.ACTION_SENSOR_DATA);
					i.putExtra(SENSOR_AVERAGE, average);
					serviceContext.sendBroadcast(i);
					//wakeLock.release();
					//mgr = null;
					serviceContext = null;
					Log.d(TAG,"SensorControl stopped: "+stopTime);
					stop();
				}
			}
		}
		public void stop(){
			running = false;
		}
		public void start(){
			running = true;
		}
	}
}
