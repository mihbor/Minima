package org.minima.database.mmr;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.minima.objects.base.MiniNumber;

/**
 * A single copy of each MMR Entry so that duplicates do not take up space
 * 
 * @author spartacusrex
 */
public class MMREntryDB {

	/**
	 * Create a static version for all MMRSets to access..
	 */
	private static MMREntryDB mDB = new MMREntryDB();
	public static MMREntryDB getDB() {
		return mDB;
	}
	
	/**
	 * Wipe the DB
	 */
	public static void clearDB() {
		mAllEntries.clear();
	}
	
	/**
	 * All the shared entries across al the MMRSets
	 */
	private static Hashtable<String, MMREntryDBRow> mAllEntries;
	
	/**
	 * Remove entries that are no  longer used.. past the cascade node
	 * @param mMinNumber
	 */
	public static void cleanUpDB(MiniNumber zMinNumber) {
		//The NEW Table..
		Hashtable<String, MMREntryDBRow> allEntries = new Hashtable<>();
		
		//Run through and keep the good ones..
		Enumeration<MMREntryDBRow> entries = mAllEntries.elements();
		while(entries.hasMoreElements()) {
			MMREntryDBRow row = entries.nextElement();
			MiniNumber block  = row.getMaxBloxk();
			
			if(block.isMoreEqual(zMinNumber)) {
				String loc = getTableString(row.getEntry());
				allEntries.put(loc, row);
			}
		}
		
		//And switch the 2..
		mAllEntries = allEntries;
	}
	
	private class MMREntryDBRow {
		MMREntry mEntry;
		MiniNumber mMaxBlock;
		
		public MMREntryDBRow(MMREntry zEntry, MiniNumber zMaxBlock) {
			mEntry    = zEntry;
			mMaxBlock = zMaxBlock;
		}
		
		public MMREntry getEntry() {
			return mEntry;
		}
		
		public MiniNumber getMaxBloxk(){
			return mMaxBlock;
		}
		
		public void setMaxBlock(MiniNumber zMax) {
			if(zMax.isMore(mMaxBlock)) {
				mMaxBlock = zMax;
			}
		}
	}
	
	private static String getTableString(MMREntry zEntry) {
		boolean dataonly  = zEntry.getData().isHashOnly();
		String 		 hash = zEntry.getHashValue().to0xString();
		
		//Convert this MMREntry into a unique string..
		return hash+":"+dataonly;
	}
	
	/**
	 * Private constructor as can only access via the static getDB()
	 */
	private MMREntryDB() {
		mAllEntries = new Hashtable<>();	
	}
	
	/**
	 * If we already have this entry return it..
	 * Else add it and return it..
	 * 
	 * @param zEntry
	 * @return
	 */
	public MMREntry getEntry(MMREntry zEntry, MiniNumber zBlock) {
		//Convert this MMREntry into a unique string..
		String loc = getTableString(zEntry);
		
		//Do we have it..
		MMREntryDBRow oldentry = mAllEntries.get(loc);
		
		//Valid.. ?
		if(oldentry != null) {
			oldentry.setMaxBlock(zBlock);
			return oldentry.getEntry();
		}
		
		//Create a new Entry
		MMREntryDBRow row = new MMREntryDBRow(zEntry, zBlock);
		
		//Else add it.. for next time..
		mAllEntries.put(loc, row);
		
		//And return it..
		return zEntry;
	}
}
