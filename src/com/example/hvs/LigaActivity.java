package com.example.hvs;

import java.util.ArrayList;
import java.util.List;

import com.example.datahandling.DatabaseHelper;
import com.example.datahandling.Spiel;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LigaActivity extends ActionBarActivity {

	DatabaseHelper dbh;
	String TAG = "liga";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_liga);
		Intent intent = getIntent();
		TextView tv = (TextView) findViewById(R.id.textViewLiga);
		int ligaNr = intent.getIntExtra("nummer", 0);
		tv.setText("Liganummer: "+ligaNr);
		
		dbh = new DatabaseHelper(getApplicationContext());
		
		/*
		 * Wir brauchen hier einen Spinner als Auswahl für die Spieltage. Per Default wäre es der aktuelle
		 * Auf dieser Auswahl basierend wird unten die Liste der Spiele gebaut (Also deutlich weniger als alle)
		 */
		
		//Das muss weg, die Liste wird kleiner und abhängig vom Spieltag
		List<Spiel> alleLigaSpiele = dbh.getAllGames(ligaNr);
		Log.d("liga", "Size von alleLigaSpiele: "+alleLigaSpiele.size());
				
		TableLayout table = (TableLayout) findViewById(R.id.tableAlleSpiele);
		for(Spiel s : alleLigaSpiele){
			TableRow row = new TableRow(getApplicationContext());
			TextView field1 = new TextView(getApplicationContext());
			ArrayList<TextView> formatArray = new ArrayList<TextView>();
			field1.setText(s.getDateDay()+"."+s.getDateMonth()+"."+String.valueOf(s.getDateYear()).split("0")[1]);
			formatArray.add(field1);
			TextView field2 = new TextView(getApplicationContext());
			field2.setText(s.getTeamHeim());
			formatArray.add(field2);
			TextView field3 = new TextView(getApplicationContext());
			field3.setText(s.getTeamGast());
			formatArray.add(field3);
			TextView field4 = new TextView(getApplicationContext());
			field4.setText(s.getToreHeim()+":"+s.getToreGast());
			formatArray.add(field4);
			
			for(TextView t : formatArray){
				t.setTextColor(Color.BLACK);
				t.setPadding(5, 5, 5, 5);
				t.setGravity(Gravity.CENTER);
				row.addView(t);
			}
			row.setPadding(0, 0, 0, 10);
			row.setBackgroundResource(R.drawable.table_back);
			table.addView(row);
			
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.liga, menu);
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
}
