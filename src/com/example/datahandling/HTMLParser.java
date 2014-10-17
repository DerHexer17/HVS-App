package com.example.datahandling;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.example.hvs.StartActivity;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class HTMLParser {
	
	String TAG = "HTML";
	private ArrayList <Spiel> alleSpiele;
	private ArrayList <String> trimHTMLList;
	private ArrayList <Spiel> alleSpieleSource;
	DatabaseHelper dbh;
	
	public ArrayList<Spiel> getAlleSpiele() {
		return alleSpiele;
	}

	public void setAlleSpiele(ArrayList<Spiel> alleSpiele) {
		this.alleSpiele = alleSpiele;
	}

	public ArrayList<String> getTrimHTMLList() {
		return trimHTMLList;
	}

	public void setTrimHTMLList(ArrayList<String> trimHTMLList) {
		this.trimHTMLList = trimHTMLList;
	}

	public ArrayList<Spiel> getAlleSpieleSource() {
		return alleSpieleSource;
	}

	public void setAlleSpieleSource(ArrayList<Spiel> alleSpieleSource) {
		this.alleSpieleSource = alleSpieleSource;
	}

	/* Main Method for Parsing the HTML from "Liste aller Spiele" */
	public ArrayList<Spiel> initialHTMLParsing(String source, int ligaNr){
		
		//Split the Source into the Rows of the Table of all Games
		String[] trimHTML = source.split("</tr>");
		trimHTML = trimHTML[1].split("</TR>");
		trimHTMLList = new ArrayList<String>();
		alleSpieleSource = new ArrayList<Spiel>();
		
		//Need to delete just the last position of the array
		for(int i = 0; i<trimHTML.length-1; i++){
			trimHTMLList.add(trimHTML[i]);
		}
		
		//Create an Object for every game with the source code of the table row as an parameter
		for(String s : trimHTMLList){
			Spiel spiel = new Spiel(s);
			spiel.setLigaNr(ligaNr);
			this.alleSpieleSource.add(spiel);
		}

		alleSpiele = new ArrayList<Spiel>();
		
		//Call splitTableRow for every Game-Object
		for(Spiel s : alleSpieleSource){
			splitTableRow(s);
			
		}
		
		//splitTableRow(alleSpieleSource.get(15));
		
		Log.d(TAG, "Size von alleSpiele: "+alleSpiele.size());
		return getAlleSpiele();
		
	}
	
	/* Method for parsing one source table row into needed parameters of a game */
	public void splitTableRow(Spiel spiel){
		String[] tds = spiel.getSourceHTML().split("</TD");
				
		//A temporary Array for all the needed parsing
		String[] temp;
		
		//Parsing the game number
		temp = tds[0].split("</FONT");
		temp = temp[0].split(">");
		spiel.setSpielNr(Integer.parseInt(temp[temp.length-1]));
			
		//Parsing the date
		temp = tds[1].split("</FONT");
		temp = temp[0].split(">");
		String tempDate = temp[temp.length-1];
		temp = tempDate.split(" ");
		temp = temp[1].split("\\.");
		spiel.setDateDay(Integer.parseInt(temp[0]));
		spiel.setDateMonth(Integer.parseInt(temp[1]));
		spiel.setDateYear(Integer.parseInt(temp[2]));
				
		//Parsing the time
		temp = tds[2].split("</FONT");
		temp = temp[0].split(">");
		spiel.setTime(temp[temp.length-1]);
		
		//Parsing the home team
		temp = tds[3].split("</FONT");
		temp = temp[0].split("</a>");
		temp = temp[0].split(">");
		spiel.setTeamHeim(temp[temp.length-1]);
		
		//Parsing the guest team
		temp = tds[4].split("</FONT");
		temp = temp[0].split("</a>");
		temp = temp[0].split(">");
		spiel.setTeamGast(temp[temp.length-1]);
		
		int sr = 0;
		//Parsing the goals and points
		temp = tds[5].split("</FONT");
		temp = temp[0].split(">");
		String tempGoals = temp[temp.length-1];
		if(tempGoals.equals(":")){
			Log.d(TAG, "Keine Tore, Spiel wurde noch nicht gespielt");
			sr = 7;
		}else if(tempGoals.contains(":")){
			temp = tempGoals.split(":");
			spiel.setToreHeim(Integer.parseInt(temp[0].trim()));
			spiel.setToreGast(Integer.parseInt(temp[1].trim()));
			temp = tds[6].split("</FONT");
			temp = temp[0].split(">");
			String tempPoints = temp[temp.length-1];
			temp = tempPoints.split(":");
			spiel.setPunkteHeim(Integer.parseInt(temp[0].trim()));
			spiel.setPunkteGast(Integer.parseInt(temp[1].trim()));
			sr = 7;
		}else{
			Log.d(TAG, "Keine Tore, aber SR angesetzt");
			spiel.setSchiedsrichter(tempGoals);
			sr = 6;
		}
		
		/*Parsing the points
		temp = tds[6].split("</FONT");
		temp = temp[0].split(">");
		String tempPoints = temp[temp.length-1];
		if(tempPoints.equals(":")){
			Log.d(TAG, "Keine Punkte, Spiel wurde noch nicht gespielt");
		}else{
			temp = tempPoints.split(":");
			spiel.setPunkteHeim(Integer.parseInt(temp[0].trim()));
			spiel.setPunkteGast(Integer.parseInt(temp[1].trim()));
		}*/	
		
		//Parsing the hyperlink to field
		temp = tds[sr].split("</FONT>");
		temp = temp[0].split("<a href=");
		temp = temp[1].split(">");
		temp = temp[0].split("\\.\\.");
		spiel.setHalle("www.hvs-handball.de"+temp[1]);
		
		//We need the League Number as well!
		//spiel.setLigaNr(10007);

		alleSpiele.add(spiel);
				
	}
	
	public ArrayList<Spiel> updateHtmlParsing(String source, int ligaNr, Cursor c){
	
		
		//Split the Source into the Rows of the Table of all Games
		String[] trimHTML = source.split("</tr>");
		trimHTML = trimHTML[1].split("</TR>");
		trimHTMLList = new ArrayList<String>();
		alleSpieleSource = new ArrayList<Spiel>();
			
		//Need to delete just the last position of the array
		for(int i = 0; i<trimHTML.length-1; i++){
			trimHTMLList.add(trimHTML[i]);
		}
		
		/*
		 * So einfach gehts leider nicht!
		 * Ich brauche die jeweiligen Spielnummern. Also spielNr = Tore vorhanden kann weg
		 * Rest bleibt. Suche nach Updates dann Datumsabh�ngig machen!
		 */
		
		// looping through all rows and adding to list
		Log.d(TAG, "Cursor ist da, Size: "+c.getCount());
		
        if (c.moveToFirst()) {
            do {
                int gespielt = c.getInt(c.getColumnIndex("spiel_nr"));
                int durchlauf = 0;
                for(String s : trimHTMLList){
                	String[] tds = s.split("</TD");
    				
            		//A temporary Array for all the needed parsing
            		String[] temp;
            		
            		//Parsing the game number
            		temp = tds[0].split("</FONT");
            		temp = temp[0].split(">");
            		if(Integer.parseInt(temp[temp.length-1])==gespielt){
            			trimHTMLList.remove(durchlauf);
            			durchlauf--;
            		}
            		durchlauf++;
            		
                }
                
               
            } while (c.moveToNext());
        }
        
        Log.d(TAG, "Neue Size aller Spiele: "+trimHTMLList.size());
        
        int durchlauf2 = 0;
        for(String st : trimHTMLList){
        	String[] tdst = st.split("</TD");
			
    		//A temporary Array for all the needed parsing
    		String[] temp1;
    		
    		//Parsing the goals and points
    		temp1 = tdst[5].split("</FONT");
    		temp1 = temp1[0].split(">");
    		String tempGoals = temp1[temp1.length-1];
    		if(tempGoals.equals(":")){
    			trimHTMLList.remove(durchlauf2);
    		}
    		durchlauf2++;
        }
		
		Log.d(TAG, "Neue Size aller Spiele: "+trimHTMLList.size());
				
		//Create an Object for every game with the source code of the table row as an parameter
		for(String s : trimHTMLList){
			Spiel spiel = new Spiel(s);
			this.alleSpieleSource.add(spiel);
		}

		alleSpiele = new ArrayList<Spiel>();
		
		//Call splitTableRow for every Game-Object
		for(Spiel s : alleSpieleSource){
			String[] tds = s.getSourceHTML().split("</TD");
			
			//A temporary Array for all the needed parsing
			String[] temp;
					
		}
		return alleSpieleSource;
	}
		
	
	
}
