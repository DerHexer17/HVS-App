package com.example.internet;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.datahandling.DatabaseHelper;
import com.example.datahandling.HTMLParser;
import com.example.datahandling.Spiel;
import com.example.datahandling.Spieltag;
import com.example.hvs.R;
import com.example.hvs.StartActivity;
import com.example.hvs.TempResultActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
	//Context wird ben�tigt, kann aber nur in Activity erzeugt werden. Daher Mitgabe als Paramter
	private Context mContext;
	//Liste der gezogenen Spiele, die dann in die Datenbank eingetr�gen bzw. ge�ndert werden
	ArrayList<Spiel> spiele;
	ProgressDialog mDialog;
	//Spiele geh�ren zu einer bestimmten Liga, die passende Nummer brauchen wir als Variable
	int ligaNr;
	//Unterschied zwischen Initialem Daten-Download oder nur Update wird hier festgehalten
	boolean update;
	
	DatabaseHelper dbh;
	
	public AsyncHttpTask(Context context, int ligaNr, boolean update, DatabaseHelper dbh){
		mContext = context;
		this.ligaNr = ligaNr;
		this.update = update;
		this.dbh = dbh;
	}
	
	
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		//mDialog = new ProgressDialog(mContext);
        //mDialog.setMessage("Please wait...");
        //mDialog.show();
	}



	@Override
    protected Integer doInBackground(String... params) {
        InputStream inputStream = null;

        HttpURLConnection urlConnection = null;

        int result = 0;
        String response = "Keine Daten";
                
        try {
            /* forming th java.net.URL object */
            URL url = new URL(params[0]);

            urlConnection = (HttpURLConnection) url.openConnection();

             /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json");

            /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");
            
            urlConnection.setRequestProperty("Accept-Charset", "iso-8859-1"); 

            /* for Get request */
            urlConnection.setRequestMethod("GET");

            int statusCode = urlConnection.getResponseCode();


            /* 200 represents HTTP OK */
            if (statusCode ==  200) {

                inputStream = new BufferedInputStream(urlConnection.getInputStream());

                response = convertInputStreamToString(inputStream);
                
                //Wenn update == false, sind wir in der initalen Phase, holen also daten komplett
                //Wenn update == true f�hren wir nur ein Ergebnis Update durch, brauchen also einen anderen HTML Parser
                if(update==false){
                    HTMLParser htmlparser = new HTMLParser();
	                spiele = htmlparser.initialHTMLParsing(response, ligaNr);
                }else{
                	Cursor c = dbh.getAllPlayedGames(ligaNr);
                	HTMLParser htmlparser = new HTMLParser();
                	spiele = htmlparser.updateHtmlParsing(response, ligaNr, c);
                }
                result = 1; // Successful

            }else{
                result = 0; //"Failed to fetch data!";
            }

        } catch (Exception e) {
            result=1000;
        	String TAG = "Hauptmethode";
			Log.d(TAG, e.getLocalizedMessage());
        }
        //StartActivity.setTestDataResult(result);

        return result; //"Failed to fetch data!";
    }

    @Override
    protected void onPostExecute(Integer result) {
        /* Download complete. Lets update UI */

        if(result == 1){

            //arrayAdapter = new ArrayAdapter(MyActivity.this, android.R.layout.simple_list_item_1, blogTitles);

            //listView.setAdapter(arrayAdapter);
        	
        	//Jetzt schreiben wir die Daten in die interne SQLite Datenbank. Bei initial also Neuanlage
        	//Dabei kreieren wir auch gleich die Spieltage
        	if(update==false){
	        	String TAG = "db";
	        	int z = 0;
	        	int ligaNr = spiele.get(2).getLigaNr();
	        	String saison = dbh.getLiga(ligaNr).getSaison();
	        	
	        	//Hier die Suche nach den Spieltagen
	        	Spieltag spieltag = new Spieltag();
	        	int spieltagsNr = 0;
	        	GregorianCalendar aktuell = new GregorianCalendar(1970, 1, 1);
	        	GregorianCalendar pr�fung = new GregorianCalendar();
	        	for(Spiel s : this.spiele){
	        			pr�fung.set(s.getDateYear(), s.getDateMonth(), s.getDateDay());
	        			int datePr�fung = pr�fung.compareTo(aktuell);
	        			if(pr�fung.after(aktuell)){
	        				if(pr�fung.get(Calendar.DAY_OF_MONTH)-aktuell.get(Calendar.DAY_OF_MONTH)==1){
	        					spieltag.setDatumEnde(s.getDateYear()+"-"+s.getDateMonth()+"-"+s.getDateDay());
	        				}else if(pr�fung.get(Calendar.DAY_OF_MONTH)==1
	        						&& pr�fung.get(Calendar.MONTH)-aktuell.get(Calendar.MONTH)==1
	        						&& aktuell.get(Calendar.DAY_OF_MONTH)>26){
	        					spieltag.setDatumEnde(s.getDateYear()+"-"+s.getDateMonth()+"-"+s.getDateDay());
	        				}else{
		        				if(spieltagsNr>0){
		        					dbh.createSpieltag(spieltag);
		        				}
		        				spieltagsNr++;
		        				spieltag.setLigaNr(ligaNr);
		        				spieltag.setSpieltags_Nr(spieltagsNr);
		        				spieltag.setSpieltags_Name(spieltagsNr+". Spieltag");
		        				spieltag.setDatumBeginn(s.getDateYear()+"-"+s.getDateMonth()+"-"+s.getDateDay());
		        				spieltag.setDatumEnde(s.getDateYear()+"-"+s.getDateMonth()+"-"+s.getDateDay());
		        				spieltag.setSaison(saison);
		        				aktuell = pr�fung;	        				
	        				}
	        			
	        			}
	        			s.setSpieltagsNr(spieltagsNr);
	        			dbh.createSpiel(s);
	        	}
	        	dbh.createSpieltag(spieltag);
	        	Log.d(TAG, "Alle Spiele in die Datenbank geschrieben, insgesamt "+z+" Datens�tze neu hinzu");
        	}
        	//setProgressBarIndeterminateVisibility(false);
        	//mDialog.dismiss();
        	
        	
        }else{
            String TAG = "PostExecute";
			Log.e(TAG, "Failed to fetch data!");
        }
    }
    
    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));

        String line = "";
        String result = "";

        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

            /* Close Stream */
        if(null!=inputStream){
            inputStream.close();
        }

        return result;
    }
    
    /* Brauche ich JSON Parsing?
    private void parseResult(String result) {

        try{
            JSONObject response = new JSONObject(result);

            JSONArray posts = response.optJSONArray("posts");

            blogTitles = new String[posts.length()];

            for(int i=0; i< posts.length();i++ ){
                JSONObject post = posts.optJSONObject(i);
                String title = post.optString("title");

                blogTitles[i] = title;
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    */
    
    
}
