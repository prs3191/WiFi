package com.pkg.wifitower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

public final class DoSampleHttpPostRequest extends AsyncTask<URL,Void, String> {
	
	int cid, lac,mcc,mnc;
	String sb;
	URL url;
	double curr_lat,curr_lng;
	Location currentLocation=new Location("");
	public AsyncResponse asyncobj=null;
	
	public DoSampleHttpPostRequest(int cellid,int lacode,int mcountryc,int mnetworkc,URL link){
		cid=cellid;
		lac=lacode;
		url=link;
		mcc=mcountryc;
		mnc=mnetworkc;
		//System.out.println("httppost obj initialised");
	}
	
   	@Override
	protected String doInBackground(URL... params) {
		 //BufferedReader in = null;

           // Create data variable for sent values to server  
           String data="";
            try {
				 data = URLEncoder.encode("mcc", "UTF-8") + "=" + mcc + "&" +
							  URLEncoder.encode("mnc", "UTF-8") + "=" + mnc+ "&" + 
							  URLEncoder.encode("lac", "UTF-8") + "=" + lac+ "&" + 
							  URLEncoder.encode("cid", "UTF-8") + "=" + cid;
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            String text = "";
            BufferedReader reader=null;
          //  System.out.println("post param:"+data);
		 
		 
		 
            try {
            	
            	URLConnection conn = url.openConnection(); 
                conn.setDoOutput(true); 
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
                wr.write( data ); 
                wr.flush(); 
            
                // Get the server response 
                 
              reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
              StringBuilder sb = new StringBuilder();
              String line = null;
              
              // Read Server Response
              while((line = reader.readLine()) != null)
                  {
                         // Append server response in string
                         sb.append(line + "\n");
                  }
                  
                  
                  text = sb.toString();
              //    System.out.println("response:"+text);
             

               
            } catch (Exception e) {
                return "Exception happened: " + e.getMessage();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
			return text;
		
	}
   	
   	@Override
	public void onPostExecute(String result) {
		
	
		Document doc = Jsoup.parse(result);
		//doc.getElementsByAttributeValueContaining("a[href]", "Lat=");
		Elements links = doc.getElementsByTag("a");
		String href;
		for (Element elem : links) {
		    href = elem.text();
		    System.out.println("href...:"+href);
		    if(href.contains("Lat=")){
		    	String lat=href.substring(4,href.indexOf(" "));
		    	String lng=href.substring(href.indexOf("Lon=")+4,href.length());
		    	curr_lat=Double.valueOf(lat);
		    	curr_lng=Double.valueOf(lng);
		    	currentLocation.setLatitude(curr_lat);
		    	currentLocation.setLongitude(curr_lng);
		    	 
		    	break;
		    }
		    
		    	    		    
		}
		
		asyncobj.processFinish(currentLocation);
		
	}
   	
   	
}
	


