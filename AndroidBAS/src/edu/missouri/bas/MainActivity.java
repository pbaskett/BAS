package edu.missouri.bas;


import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import edu.missouri.bas.activities.DeviceListActivity;
import edu.missouri.bas.bluetooth.BluetoothRunnable;
import edu.missouri.bas.service.SensorService;
import edu.missouri.bas.survey.XMLSurveyMenu;

public class MainActivity extends ListActivity {

	private boolean mIsRunning=false;
	private BluetoothAdapter mAdapter;
	
	private final static String TAG = "SensorServiceActivity";
	
	public static final int REQUEST_ENABLE_BT = 3;
	public static final int INTENT_SELECT_DEVICES = 0;
	public static final int INTENT_DISCOVERY = 1;
	public static final int INTENT_VIEW_DEVICES = 2;
	
	protected static final int START = 0;
	protected static final int STOP = 1;
	protected static final int SURVEY = 2;
	protected static final int STATE = 3;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        
    	String[] options = {"Start Service", "Stop Service", "Survey Menu",
		"Check Bluetooth State", "Disconnect Bluetooth"};
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
    			android.R.layout.simple_list_item_1, options);
    	setListAdapter(adapter);
    
        ListView listView = getListView();
        
        //listView.setBackgroundColor(Color.BLACK);
        //listView.
        
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
		    	switch(position){
	    		case START:
	    			startSService();
	    			break;
	    		case STOP:
	    			stopSService();
	    			break;
	    		case SURVEY: 
	    			startSurveyMenu();
	    			break;
	    		case STATE: 
	    			getState();
	    			break;
		    	}
			}
        	
        });

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if(mAdapter == null){
        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }
        
        if (!mAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
        
        IntentFilter intentFilter = new IntentFilter(SensorService.ACTION_BLUETOOTH_STATE_RESULT);
        this.registerReceiver(bluetoothReceiver, intentFilter);
        
        startSService();
                
        getState();
    }
    
    private void startSurveyMenu(){
		Intent i = new Intent(getApplicationContext(), XMLSurveyMenu.class);
		startActivity(i);
    }
    
    private void stopSService() {
    	mIsRunning = false;
    	this.stopService(new Intent(MainActivity.this,SensorService.class));
    }
    private void startSService() {
        if (! mIsRunning) {
            mIsRunning = true;
            this.startService(new Intent(MainActivity.this,SensorService.class));
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("SensorServiceActivity", "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Bluetooth could not be enabled, exiting",
                		Toast.LENGTH_LONG).show();
                finish();
            }
            break;
		case MainActivity.INTENT_SELECT_DEVICES:
				String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				Intent connectIntent = new Intent(SensorService.ACTION_CONNECT_BLUETOOTH);
				connectIntent.putExtra(SensorService.INTENT_EXTRA_BT_DEVICE,address);
				connectIntent.putExtra(SensorService.INTENT_EXTRA_BT_MODE,
						BluetoothRunnable.BluetoothMode.CLIENT);
				connectIntent.putExtra(SensorService.INTENT_EXTRA_BT_TYPE,
						BluetoothRunnable.BluetoothSocketType.INSECURE);
				connectIntent.putExtra(SensorService.INTENT_EXTRA_BT_UUID,
						"00001101-0000-1000-8000-00805F9B34FB");
				this.sendBroadcast(connectIntent);
			break;
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.bluetooth_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Intent menuIntent;
		if(item.getItemId() == R.id.selectDevice){
			menuIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(menuIntent, 
					MainActivity.INTENT_SELECT_DEVICES);
			return true;
		}
		return false;
	}
	
	protected void getState(){
			Intent i = new Intent(SensorService.ACTION_GET_BLUETOOTH_STATE);
			this.sendBroadcast(i);
	}

	@Override
	public void onDestroy(){
		this.unregisterReceiver(bluetoothReceiver);
		super.onDestroy();
	}
	
	BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(SensorService.ACTION_BLUETOOTH_STATE_RESULT)){
				Log.d(TAG,"Got bluetooth state change");
				String stateInformation = intent.getStringExtra(SensorService.INTENT_EXTRA_BT_STATE);
				Toast.makeText(getApplicationContext(), stateInformation, Toast.LENGTH_LONG).show();
			}
		}
	};
}

