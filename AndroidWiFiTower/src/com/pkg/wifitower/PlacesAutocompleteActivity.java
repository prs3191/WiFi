package com.pkg.wifitower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import static android.location.Criteria.ACCURACY_FINE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.pkg.wifitower.R.id;

import android.support.v4.app.FragmentActivity;


public class PlacesAutocompleteActivity extends Activity 
							implements OnItemClickListener
							{
	

	double curr_lat,curr_lng,dummy_lat=12.9456099,dummy_long=80.2400009,home_lat,home_lng,distance;
    
    TextView loc,homeloctxt;
    Location location,latestLocation;
    Location latestLocation2=new Location("");
    Location currentLocation=new Location("");
    Location home_loc=new Location("");

    public HashMap<String,String> map_acti=new HashMap<String, String>();
	public ArrayList<HashMap<String,String>> map_acti_list=new ArrayList<HashMap<String, String>>();
    public static String country="IN"; 
	public String homeloc_id=null; 
    public String sb=null;
    public URL url=null;
    public StringBuilder jsonResults = new StringBuilder();
    
    public int cid,lac;
    LocationManager locationManager;
    public SharedPreferences sharedPreferences;
    public static boolean app_Destroyed=false;
    public static Context appcontext;
    
    public GetTowercidlac gettower_obj=new GetTowercidlac();
    TelephonyManager tm ;
    
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api";
    private static final String TYPE_PLACE = "/place/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "xxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String LOG_TAG = "WiFiTower";

   

    
	/** Called when the activity is first created. */
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

       System.out.println("oncreate()"); 
       try
       {
    	// RadioButton btn = (RadioButton)findViewById(R.id.radioButton1);
    	 //btn.setChecked(true);
   		RadioGroup radioGroup = (RadioGroup)findViewById(R.id.country_rg);
   		//radioGroup.check(R.id.radioButton1);
			radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
	            public void onCheckedChanged(RadioGroup rg, int checkedId) {
	                // for(int i=0; i<rg.getChildCount(); i++) {
	                      RadioButton btn = (RadioButton)findViewById(checkedId);
	                     // if(btn.getId() == checkedId) {
	                      country = btn.getText().toString();
	                      
	          /**save selected country  in shared prefs*/
	          Utils.savePreferences(PlacesAutocompleteActivity.this,"country_name",country);
	          Utils.savePreferences(PlacesAutocompleteActivity.this,"radio_id",Integer.toString(checkedId));
	          System.out.println("radio_id saved in shared prefs."+checkedId); 
	            }
	       });
			
   	   }
   	catch(Exception e){
   		System.out.println("Radio button: "+ e);
   	}

       
    /** AutoCompleteTextView using adapter */    
       AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
       ArrayAdapter<HashMap<String,String>> adapter=new PlacesAutoCompleteAdapter(this,R.layout.list_item, R.id.textView1,map_acti_list);
        
        
       autoCompView.setAdapter(adapter);
        
       autoCompView.setOnItemClickListener(this);
        
       /**button listener to set home location */
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
            	Editable str=autoCompView.getText();
            	loc = (TextView)findViewById(R.id.editText3);
                loc.setText(str); 
                
                System.out.println("autocomp text:"+str);
             if(!str.toString().isEmpty()){
            	 System.out.println("entering lat lng for home loc");
            	 sethomegeo(v);
             }
             else{
            	 Toast.makeText(PlacesAutocompleteActivity.this, "Enter Home Loc", Toast.LENGTH_SHORT).show();            	 
             }
            	
            	
		    }
        });

        /**Buttom to clear shared prefs and remove stored location file */
        final Button remove_prox_button = (Button) findViewById(R.id.button3);
        remove_prox_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	removeproximity(v);
            	
            	
		    }

			
        });
        
        
        

    }

	/**fill in txt values from shared prefs */
	@Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume()...");
        
        
        String checkedId=Utils.readPreferences(PlacesAutocompleteActivity.this,"radio_id","0"); 
        try{
        	//RadioButton btn = (RadioButton)findViewById(Integer.parseInt(checkedId));
        	//btn.setChecked(true);
        	System.out.println("checked id: "+checkedId);
        	RadioGroup radioGroup = (RadioGroup)findViewById(R.id.country_rg);
        	//int id=((RadioButton)radioGroup.getChildAt(Integer.parseInt(checkedId))).getId();
        	//System.out.println("button id: "+id);
        	if(!checkedId.contains("0"))
        		radioGroup.check(Integer.parseInt(checkedId));
        }
        catch(Exception e){
        	Toast.makeText(PlacesAutocompleteActivity.this,"No radio button was clicked:"+e,Toast.LENGTH_LONG).show();
        }
        
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        autoCompView.setText(Utils.readPreferences(PlacesAutocompleteActivity.this,"home_txt",""));
        
        
        loc = (TextView)findViewById(R.id.editText3);
        loc.setText(Utils.readPreferences(PlacesAutocompleteActivity.this,"home_txt","")); 
        
        
        TextView home_latlng_txtvw = (TextView)findViewById(R.id.editText1);
        home_latlng_txtvw.setText("home_lat:"
        							+ Utils.readPreferences(PlacesAutocompleteActivity.this,"home_lat","")
        						    +" home_lng:"
        							+ Utils.readPreferences(PlacesAutocompleteActivity.this,"home_lng","")); 
        
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause()...");  
    }
	@Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop()...");  
    }

	@Override
    protected void onDestroy() {
        super.onDestroy();

      System.out.println("onDestroy()...");
      System.out.println("app destroyed");

	}
	
	/** listener for auto complete text view*/
    public void onItemClick(AdapterView<?> adapterView,View view, int position, long id) {
    	
        TextView home_latlng_txtvw = (TextView)findViewById(R.id.editText1);
		 home_latlng_txtvw.setText(" ");
		 
         map_acti = (HashMap<String, String>) adapterView.getItemAtPosition(position);
         System.out.println("key,value:"+map_acti);
         AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
         autoCompView.setText(map_acti.keySet().toString());
        
         /**save selected location in shared prefs*/
         Utils.savePreferences(PlacesAutocompleteActivity.this,"home_txt",map_acti.keySet().toString());
         
         /** get place id from HashMap<String,String>*/
         homeloc_id=map_acti.values().toString();
         homeloc_id=homeloc_id.substring(1, homeloc_id.length()-1);
        //Toast.makeText(this, map_acti.values().toString(), Toast.LENGTH_SHORT).show();
    }
    
    

    
    public void sethomegeo(View view){

    	System.out.println("placeid: "+homeloc_id);
    	/**Google API to get geolocation for selected place*/
    	sb = PLACES_API_BASE + TYPE_PLACE + OUT_JSON+"?key=" + API_KEY+"&placeid=" + homeloc_id;
        try {
        	/**http request in async task since this is in main activity*/
			url = new URL(sb);
			if(!homeloc_id.isEmpty())
			 new HttpTask().execute(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

    }

    public final class HttpTask extends AsyncTask<URL , Boolean /* Progress */, String /* Result */> {

    	private HttpClient mHc = new DefaultHttpClient();

    	@Override
    	protected String doInBackground(URL...urls) {
    		
    		// Do the usual httpclient thing to get the result
    		HttpURLConnection conn = null;
    		
            try {
            	 
                conn = (HttpURLConnection) url.openConnection();
                System.out.println("home_loc conn established?:"+conn);
                jsonResults.delete(0,jsonResults.length());
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
                
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
                
            } finally {
                if (conn != null) {
                	
                    conn.disconnect();
                }
            }
    		return jsonResults.toString();
    	}

    	@Override
    	protected void onProgressUpdate(Boolean... progress) {

    	}

    	@Override
    	protected void onPostExecute(String result) {
    		publishProgress(false);
    		// Do something with result in your activity
    		try {
    			 
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                System.out.println("jsonobj:"+jsonObj);
                JSONObject predsJsonArray = jsonObj.optJSONObject("result").optJSONObject("geometry").optJSONObject("location");
                 
                
                 String lat= predsJsonArray.getString("lat");
                 String lng= predsJsonArray.getString("lng");
                 
                

                 System.out.println("lat: "+lat);
                 System.out.println("lan: "+lng);
                    
                 home_lat=Double.valueOf(lat);
                 home_lng=Double.valueOf(lng);
                 
                 TextView home_latlng_txtvw = (TextView)findViewById(R.id.editText1);
                 home_latlng_txtvw.setText("home_lat:"+home_lat+" home_lng:"+home_lng); 
                 
                 home_loc.setLatitude(home_lat);
                 home_loc.setLongitude(home_lng);
                
                 /**save lat,lng of home location using shared prefs and
                  *  store in file->useful to get lat/lng while app is not running.
                  * */
                 Utils.savePreferences(PlacesAutocompleteActivity.this,"home_lat", Double.toString(home_lat));
             	 Utils.savePreferences(PlacesAutocompleteActivity.this,"home_lng",Double.toString(home_lng));
                 
             	 try {
                     FileOutputStream fOut = openFileOutput("loc.txt",0);
                     fOut.flush();
                     
                     String data=Double.toString(home_lat)+"\n"+Double.toString(home_lng);
                     fOut.write(data.getBytes());
                     fOut.close();
                     File dir=getFilesDir();
                     System.out.println("data: "+data);
                     System.out.println("file dir: "+dir);
                     Toast.makeText(getBaseContext(),"file saved",Toast.LENGTH_SHORT).show();
                  } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                
                 
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot process JSON results", e);
            }
           
    	}

		

}

/** clear persistent storage. */    
    public void removeproximity(View v){
  
    		Utils.clearPreferences(PlacesAutocompleteActivity.this);
    		File dir = getFilesDir();
    		File file = new File(dir, "loc.txt");
    		boolean deleted = file.delete();
    		Toast.makeText(this,"Saved home location removed: "+deleted, Toast.LENGTH_LONG).show();
    		
    		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
            autoCompView.setText("");
            
    		
    		TextView txtvw = (TextView)findViewById(R.id.editText3);
    		txtvw.setText("");
    		txtvw=(TextView)findViewById(R.id.editText1);
    		txtvw.setText("");
  
    }
    
    
    
}

