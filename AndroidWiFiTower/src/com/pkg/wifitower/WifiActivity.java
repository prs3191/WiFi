package com.pkg.wifitower;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WifiActivity extends Activity {
	
/**depending on distance calculated and current wifi state flip wifi*/
	   public static void changestate(final Context context,double distance){
	    	
	    	if (distance <=1000){
	    	   
	    		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    		if(!wifiManager.isWifiEnabled())
	    		{ wifiManager.setWifiEnabled(true);
	    		Toast.makeText(context, "Proximity Entered.Turning ON WiFi", Toast.LENGTH_SHORT).show();
	    		}
	    	}
	    	else{
	    		
	    		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	    		if(wifiManager.isWifiEnabled()){
	    		   wifiManager.setWifiEnabled(false);
	    		   Toast.makeText(context, "Left Proximity.Turning OFF WiFi", Toast.LENGTH_SHORT).show();
	    		}
	    	}
	    }
}
