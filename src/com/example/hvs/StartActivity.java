package com.example.hvs;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.datahandling.DatabaseHelper;
import com.example.datahandling.Spiel;
import com.example.datahandling.Liga;
import com.example.internet.AsyncHttpTask;
import com.example.internet.DataHVS;

import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class StartActivity extends ActionBarActivity {

	DatabaseHelper dbh;
	String TAG = "Initial";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		//Wir brauchen von Beginn an unsere Datenbank
		dbh = new DatabaseHelper(getApplicationContext());
		
		//Erster Logeintrag. Vorerst rudimentär, aber wichtig, um zu prüfen, ob wir die Daten vom HVS einmal komplett holen müssen
		dbh.createLog("App gestartet");
		Log.d(TAG, "Log Eintrag in DB geschrieben");
		
		if(dbh.getAllLogs().size()>1){
			callAlleLigen(findViewById(R.id.button1));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void dataTest(View view){
		TextView textview = (TextView) findViewById(R.id.textView1);
		textview.setText("Button gedrückt!");
		textview.setText("Networkcheck: "+isNetworkAvailable());
		if(isNetworkAvailable()==false){
			return;
		}
		
		//DataHVS testhandler = new DataHVS();
		//textview.setText(testhandler.getTestData());
				
        if(dbh.getAllLogs().size()>1){
        	TextView textview2 = (TextView) findViewById(R.id.textView2);
        	Log.d(TAG, "App öfter als einmal gestartet, kein Datenabgleich");
        	Time time = new Time();
        	time.setToNow();
        	String tempdate = time.toString().substring(0,4)+"-"+time.toString().substring(4,6)+"-"+time.toString().substring(6,8);
        	textview2.setText("Größe von allLogs: "+dbh.getAllLogs().size()+" Kalender: "+tempdate);
        	return;
        }else{
			initial();
        }
        
        Button bt = (Button) findViewById(R.id.button2);
        bt.setText("Jetzt zur nächsten Activity");
	}
	
	//Einfacher Check, ob das Internet zur Verfügung steht
	public boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    // if no network is available networkInfo will be null
    // otherwise check if we are connected
    if (networkInfo != null && networkInfo.isConnected()) {
        return true;
    }
    return false;
	} 
	
	public void callAlleLigen(View view){
		Intent intent = new Intent(getApplicationContext(), AlleLigenActivity.class);
		Bundle bundle = new Bundle();
		
		startActivity(intent);
	}
	
	public void initial(){
		TextView textview2 = (TextView) findViewById(R.id.textView2);
		List<Liga> initialLigen = new ArrayList<Liga>();
		
		Liga sac = new Liga();
		sac.setLigaNr(10000);
		sac.setLink("http://www.hvs-handball.de/_stdVerband/Liga_Spiele.asp?M_lfdNr=10000&titel=Sachsenliga%20%20M%E4nner%20%202014/15");
		sac.setName("Sachsenliga");
		sac.setEbene("HVS");
		sac.setGeschlecht("männlich");
		sac.setSaison("2014/2015");
		sac.setPokal(0);
		initialLigen.add(sac);
		
		/*
		Liga sacw = new Liga();
		sacw.setLigaNr(10001);
		sacw.setLink("http://www.hvs-handball.de/_stdVerband/Liga_Spiele.asp?M_lfdNr=10001&titel=Sachsenliga%20%20Frauen%20%202014/15");
		sacw.setName("Sachsenliga");
		sacw.setEbene("HVS");
		sacw.setGeschlecht("weiblich");
		sacw.setSaison("2014/2015");
		sacw.setPokal(0);
		initialLigen.add(sacw);
		
		Liga vbo = new Liga();
		vbo.setLigaNr(10007);
		vbo.setLink("http://www.hvs-handball.de/_stdVerband/Liga_Spiele.asp?M_lfdNr=10007&titel=Verbandsliga%20Sachsen%20M%E4nner%20Staffel%20Ost%202014/15");
		vbo.setName("Verbandsliga Sachsen-Ost");
		vbo.setEbene("HVS");
		vbo.setGeschlecht("männlich");
		vbo.setSaison("2014/2015");
		vbo.setPokal(0);
		initialLigen.add(vbo);
		
		Liga vbw = new Liga();
		vbw.setLigaNr(10008);
		vbw.setLink("http://www.hvs-handball.de/_stdVerband/Liga_Spiele.asp?M_lfdNr=10008&titel=Verbandsliga%20Sachsen%20M%E4nner%20Staffel%20West%202014/15");
		vbw.setName("Verbandsliga Sachsen-West");
		vbw.setEbene("HVS");
		vbw.setGeschlecht("männlich");
		vbw.setSaison("2014/2015");
		vbw.setPokal(0);
		initialLigen.add(vbw);
		
		Liga vbow = new Liga();
		vbow.setLigaNr(10009);
		vbow.setLink("http://www.hvs-handball.de/_stdVerband/Liga_Spiele.asp?M_lfdNr=10009&titel=Verbandsliga%20Sachsen%20Frauen%20Staffel%20Ost%202014/15");
		vbow.setName("Verbandsliga Sachsen-Ost");
		vbow.setEbene("HVS");
		vbow.setGeschlecht("weiblich");
		vbow.setSaison("2014/2015");
		vbow.setPokal(0);
		initialLigen.add(vbow);
		
		Liga vbww = new Liga();
		vbww.setLigaNr(10010);
		vbww.setLink("http://www.hvs-handball.de/_stdVerband/Liga_Spiele.asp?M_lfdNr=10010&titel=Verbandsliga%20Sachsen%20Frauen%20Staffel%20West%202014/15");
		vbww.setName("Verbandsliga Sachsen-West");
		vbww.setEbene("HVS");
		vbww.setGeschlecht("weiblich");
		vbww.setSaison("2014/2015");
		vbww.setPokal(0);
		initialLigen.add(vbww);
		*/
		
		for(Liga l : initialLigen){
			dbh.createLiga(l);
			new AsyncHttpTask(getApplicationContext(), l.getLigaNr(), false, dbh).execute(l.getLink());
		}
		
		
        textview2.setText("AsyncTask executed!");
	}
}
