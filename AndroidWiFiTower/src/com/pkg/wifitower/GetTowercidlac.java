package com.pkg.wifitower; 
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.MonthDisplayHelper;
import android.widget.TextView;
import android.widget.Toast;
 
public class GetTowercidlac  implements AsyncResponse {
 
	int cid,lac,mcc=0,mnc=0;
	String sb;
	URL url;
	double curr_lat,curr_lng,distance;
	public static double home_lat,home_lng;
	public TelephonyManager tm;
	public static Location curr_loc=new Location("");
	public static Location home_loc=new Location("");
	GsmCellLocation towerlocation = new GsmCellLocation();
	ProximityActivity proximity_Activityobj=new ProximityActivity();
	boolean proximity_entering=false;
	
	public static Context mcontext2;
	
    public void gettower(GsmCellLocation towerloc,Context context,int mcountryc,int mnetworkc){
    	System.out.println("gettower() location..");
    	
    	mcontext2=context;
    	System.out.println("mcontext2: "+mcontext2);
    	System.out.println("tower getapplicationcontext():"+mcontext2.getApplicationContext());

    	towerlocation= towerloc ;
		try{
		 cid=towerlocation.getCid();
		 lac= towerlocation.getLac();
		 mcc=mcountryc;
		 mnc=mnetworkc;
		 System.out.println("mcc: "+mcc+"mnc: "+mnc);

		}
		catch(NullPointerException e){

			System.out.println("null pointer exception in gettower()"+e);
		}
		
		
		try {
			sb="http://cellphonetrackers.org/gsm/gsm-tracker.php";
			url=new URL(sb);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("url excep: "+e);
			e.printStackTrace();
		}
		
		/**http POST request to find lat/lng based on cell id,lac,mcc,mnc*/
		DoSampleHttpPostRequest httpreq_obj=new DoSampleHttpPostRequest(cid,lac,mcc,mnc,url);
		httpreq_obj.asyncobj=this;
		httpreq_obj.execute(url);
		
    }

    /**process result from http request*/
	@Override
	public void processFinish(Location output) {
		
		System.out.println("async finished for curr tower_loc.back in processfinish()");
		
		curr_loc=output;
		curr_lat=curr_loc.getLatitude();
		curr_lng=curr_loc.getLongitude();
		System.out.println("curr_loc:"+curr_loc);

		String  last_stored_homelatstr="0.000000"  ;
		String  last_stored_homelngstr="0.000000" ;
		/**read in home loc from storage*/
		try{
	         FileInputStream fin = mcontext2.openFileInput("loc.txt");
	         int c;
	         String temp="";
	         while( (c = fin.read()) != -1){
	            temp = temp + Character.toString((char)c);
	         }
	        last_stored_homelatstr=temp.substring(0,temp.indexOf("\n"));
	        last_stored_homelngstr=temp.substring(temp.indexOf("\n")+1,temp.length());
	        System.out.println("file read: "+last_stored_homelatstr+"lng:"+last_stored_homelngstr);
	         
	      }catch(Exception e){
	    	  System.out.println("file not found: "+e);
	      }
		
		double last_stored_homelat=Double.valueOf(last_stored_homelatstr);
		double last_stored_homelng=Double.valueOf(last_stored_homelngstr);
		
		System.out.println("last_stored_homelat:"+last_stored_homelat);
		System.out.println("last_stored_homelng:"+last_stored_homelng);
		
		home_loc.setLatitude(last_stored_homelat);
		home_loc.setLongitude(last_stored_homelng);
		
		/**if home location is not set or 
		 * current location is not found using cellid/lac then do not call getdist()*/
		if(!(home_loc.getLatitude()==0.000000) && !(curr_loc.getLatitude()==0.000000) )
		{	
			System.out.println("home_loc found so calculate dist"+home_loc);
			getdist();
		}

		else{
			System.out.println("home_loc not set or curr is 0.000");
		}
		
	}
    
 
	/**calculate distance*/
    public void getdist(){
    	  
    	
    	  System.out.println("getting distance..");
    	  System.out.println("stored home location .."+home_loc);
    	  System.out.println("current location .."+curr_loc);
    	 
    	distance=home_loc.distanceTo(curr_loc);
    	 System.out.println("distance:"+distance);
    	 System.out.println("to wifiactivity..");
    	 WifiActivity.changestate(mcontext2, distance);
//    	 Toast toast = Toast.makeText(mcontext2, "distance:"+distance, Toast.LENGTH_LONG);
//    	 toast.show();
//    	 Intent intent=new Intent();
//		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		 intent.setClass(this.mcontext2, WifiActivity.class); 
//		 intent.putExtra("distance", distance);
//		 mcontext2.startActivity(intent);
    	 
//    	 if(distance<=1000 && distance!=0){
// 		 
//    		 proximity_entering=true;
//    		 System.out.println("calling notification dis<=500...");
//    		 Intent intent=new Intent();
//    		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    		 intent.setClass(this.mcontext2, ProximityActivity.class); 
//    		 intent.putExtra("KEY_PROX_ENTERING", true);
//    		 mcontext2.startActivity(intent);
//    		 //proximity_Activityobj.notifi(proximity_entering);
//    	 }
//    	 else if (distance > 1000 && proximity_entering){
//    		 
//    		 proximity_entering=false;
//    		 System.out.println("calling notification dis>500...");
//    		 Intent intent=new Intent();
//    		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    		 intent.setClass(mcontext2, ProximityActivity.class); 
//    		 intent.putExtra("KEY_PROX_ENTERING", true);
//    		 mcontext2.startActivity(intent);
//    		 
//    		// proximity_Activityobj.notifi(proximity_entering);
//    	 }
//    	 else{
//    		 System.out.println("Always outside the proximity");
//    	 }
    }
       	

   
    
}