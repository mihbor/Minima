package org.minima.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import org.minima.objects.base.MiniData;

public abstract class LowMemHashTable {

	public static int MEM_ID_DEPTH = 8;
	
	Hashtable<String, ArrayList<LowMemHashTablable>> mTable;
	
	public LowMemHashTable() {
		mTable = new Hashtable<>();
	}
	
	public void add(MiniData zKey, LowMemHashTablable zObject) {
		//Keys
		String totkey = zKey.to0xString();
		String key    = totkey.substring(totkey.length()-MEM_ID_DEPTH);
	
		//Get the current list..
		ArrayList<LowMemHashTablable> curr = mTable.get(key);
		
		//Does it exist
		if(curr == null) {
			//Create..
			curr = new ArrayList<>();
		
			//Add to the table
			mTable.put(key, curr);
		}
		
		//Add object to the list
		curr.add(zObject);
	}
	
	public Object get(MiniData zKey) {
		//Keys
		String totkey = zKey.to0xString();
		String key    = totkey.substring(totkey.length()-MEM_ID_DEPTH);
	
		//Get the current list..
		ArrayList<LowMemHashTablable> curr = mTable.get(key);
			
		//No object
		if(curr == null) {
			return null;
		}
		
		//Find the one..
		for(LowMemHashTablable obj : curr) {
			if(obj.getHashtableIdentifier().equals(totkey)) {
				return obj;
			}
		}
		
		//Not Found..
		return null;
	}
}
