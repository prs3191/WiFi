package com.pkg.wifitower;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**Automatically called when State Change is Detected 
because this Receiver is Registered for PHONE_STATE intent filter in AndroidManifest.xml*/

public class PhoneStateReceiver extends BroadcastReceiver {

	public static String LOG_TAG = "CustomPhoneStateListener";
	public static GsmCellLocation gcLoc=new GsmCellLocation();
	public static int last_cid,last_lac,last_psc,mcc,mnc;
	public static boolean app_Running=false;
	
	
	GetTowercidlac gettower_obj=new GetTowercidlac();
	Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {

			System.out.println("in brodacast receiver.onReceive()...");
			//System.out.println("getappcontext: "+context.getApplicationContext());			
			
			CustomPhoneStateListener phoneStateListener =new CustomPhoneStateListener(context);//Creating the Object of Listener
			TelephonyManager manager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);//Getting the Telephony Service Object
			 String networkOperator = manager.getNetworkOperator();

			    if (networkOperator != null) {
			         mcc = Integer.parseInt(networkOperator.substring(0, 3));
			         mnc = Integer.parseInt(networkOperator.substring(3));
			    }
            manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);//Registering the Listener with Telephony to listen the State Change

		}
	
	
	class CustomPhoneStateListener extends PhoneStateListener{
	
		public CustomPhoneStateListener(Context context) {
		mContext = context;
	}

	
		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
		
			if (location instanceof GsmCellLocation)
			{
				System.out.println("prev cell info: "+gcLoc);
				try{
				
					/**prev loc is empty or same same as current loc then do not calculate distance*/	
					
					if(gcLoc.toString().isEmpty() || !(gcLoc.equals((GsmCellLocation)location)))
					{
						gcLoc = (GsmCellLocation) location;
						
						Log.i(LOG_TAG,"current cell info:" + gcLoc.toString());
						System.out.println("context val to gettower(): "+mContext);
							
							gettower_obj.gettower(gcLoc,mContext,mcc,mnc);
					}
					else
					{
						System.out.println("Still in same tower:"+gcLoc);	
					}
				}
				catch(Exception e)
				{
					System.out.println("prev cell location exception: "+e+"gcloc: "+gcLoc);
				}
			
			} 
			else {
				Log.i(LOG_TAG, "onCellLocationChanged:not a gsmcell " + location.toString());
				}
	 		}
	  	}
}
	