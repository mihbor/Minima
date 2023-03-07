package org.minima.database;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.minima.database.archive.ArchiveManager;
import org.minima.database.cascade.Cascade;
import org.minima.database.maxima.MaximaDB;
import org.minima.database.minidapps.MDSDB;
import org.minima.database.txpowdb.TxPoWDB;
import org.minima.database.txpowtree.TxPowTree;
import org.minima.database.userprefs.UserDB;
import org.minima.database.userprefs.txndb.TxnDB;
import org.minima.database.wallet.Wallet;
import org.minima.system.network.p2p.P2PDB;
import org.minima.system.params.GeneralParams;
import org.minima.utils.MiniFile;
import org.minima.utils.MinimaLogger;

public class MinimaDB {

	/**
	 * Static access to MAIN MinimaDB
	 */
	private static MinimaDB mMinimaDB = null;
	public static void createDB(String jdbcUrl) {
		mMinimaDB = new MinimaDB(jdbcUrl);
	}
	public static MinimaDB getDB() {return mMinimaDB;}

	String jdbcUrl;
	/**
	 * The individual DBs
	 */
	ArchiveManager 	mArchive;
	TxPoWDB 		mTxPoWDB;
	TxPowTree 		mTxPoWTree;
	Cascade			mCascade;
	UserDB			mUserDB;
	TxnDB			mTxnDB;
	Wallet			mWallet;
	MaximaDB	 	mMaximaDB;
	MDSDB			mMDSDB;
	
	/**
	 * For P2P Information
	 */
	P2PDB			mP2PDB;
	
	/**
	 * LOCKING the MinimaDB for read write operations..
	 */
	ReadWriteLock mRWLock;
	
	/**
	 * Main Constructor
	 */
	public MinimaDB(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		mArchive	= new ArchiveManager(jdbcUrl);
		mTxPoWDB	= new TxPoWDB(jdbcUrl);
		mTxPoWTree 	= new TxPowTree();
		mCascade	= new Cascade();
		mUserDB		= new UserDB();
		mWallet		= new Wallet(jdbcUrl);
		mMaximaDB	= new MaximaDB(jdbcUrl);
		mMDSDB   	= new MDSDB(jdbcUrl);
		
		mP2PDB		= new P2PDB();
		
		mRWLock = new ReentrantReadWriteLock();
	}
	
	/**
	 * Locking access to the database is CRUCIAL
	 * 
	 * You need to do this for the IBD and Greeting messages so they are consistent
	 */
	public void readLock(boolean zLock) {
		if(zLock) {
			mRWLock.readLock().lock();
		}else {
			mRWLock.readLock().unlock();
		}
	}
	
	public void writeLock(boolean zLock) {
		if(zLock) {
			mRWLock.writeLock().lock();
		}else {
			mRWLock.writeLock().unlock();
		}
	}
	
	/**
	 * Get the various different databases
	 */
	public TxPoWDB getTxPoWDB() {
		return mTxPoWDB;
	}
	
	public TxPowTree getTxPoWTree() {
		return mTxPoWTree;
	}
	
	public Cascade getCascade() {
		return mCascade;
	}
	
	public void setIBDCascade(Cascade zCascade) {
		mCascade = zCascade;
	}
	
	//Used when doing an archive resync
	public void resetCascadeAndTxPoWTree() {
		mCascade 	= new Cascade();
		mTxPoWTree 	= new TxPowTree();
	}
	
	public long getCascadeFileSize() {
		return getDBFileSie("cascade.db");
	}
	
	public long getUserDBFileSize() {
		return getDBFileSie("userprefs.db");
	}
	
	public long getTxPowTreeFileSize() {
		return getDBFileSie("chaintree.db");
	}
	
	public long getP2PFileSize() {
		return getDBFileSie("p2p.db");
	}
	
	public MDSDB getMDSDB() {
		return mMDSDB;
	}
	
	private long getDBFileSie(String zFilename) {
		//Get the base Database folder
		File basedb = getBaseDBFolder();
		
		//The File
		File file = new File(basedb,zFilename);
		if(file.exists()) {
			return file.length();
		}
		
		return 0;
	}
	
	public UserDB getUserDB() {
		return mUserDB;
	}
	
	public TxnDB getCustomTxnDB() {
		return mTxnDB;
	}
	
	public Wallet getWallet() {
		return mWallet;
	}
	
	public ArchiveManager getArchive() {
		return mArchive;
	}
	
	public MaximaDB getMaximaDB() {
		return mMaximaDB;
	}
	
	public P2PDB getP2PDB() {
		return mP2PDB;
	}
	
	public File getBaseDBFolder() {
		return new File(GeneralParams.DATA_FOLDER,"databases");
	}
	
	public void loadAllDB() {
		
		//We need read lock 
		writeLock(true);
		
		try {
			
			//Get the base Database folder
			File basedb = getBaseDBFolder();
			
			//Load the wallet
			File walletsqlfolder = new File(basedb,"walletsql");
			if(!GeneralParams.IS_MAIN_DBPASSWORD_SET) {
				mWallet.loadDB(new File(walletsqlfolder,"wallet"));
			}else {
				MinimaLogger.log("Using Encrypted SQL DB");
				mWallet.loadEncryptedSQLDB(new File(walletsqlfolder,"wallet"),GeneralParams.MAIN_DBPASSWORD);
			}
			
			//Set the Archive folder
			File archsqlfolder = new File(basedb,"archivesql");
			try {
				
				//Try and load the Archive DB
				mArchive.loadDB(new File(archsqlfolder,"archive"));
				
			}catch(Exception exc) {
				
				//Log the complete error
				MinimaLogger.log(exc);
				
				//There wqas an issue.. wipe it.. and resync..
				MinimaLogger.log("ERROR loading ArchiveDB.. WIPE and RESYNC.. ");
				
				//Close the DB
				mArchive.hardCloseDB();
				
				//Delete the ArchiveDB folder
				MiniFile.deleteFileOrFolder(archsqlfolder.getAbsolutePath(), archsqlfolder);
				
				//And reload..
				mArchive = new ArchiveManager(jdbcUrl);
				mArchive.loadDB(new File(archsqlfolder,"archive"));
			}
			
			//Load the SQL DB
			File txpowsqlfolder = new File(basedb,"txpowsql");
			try {
				
				//Try and load the Archive DB
				mTxPoWDB.loadSQLDB(new File(txpowsqlfolder,"txpow"));
				
			}catch(Exception exc) {
				
				//Log the complete error
				MinimaLogger.log(exc);
				
				//There wqas an issue.. wipe it.. and resync..
				MinimaLogger.log("ERROR loading TxPoWSQLDB.. WIPE and RESYNC.. ");
				
				//Close the DB
				mTxPoWDB.hardCloseSQLDB();
				
				//Delete the ArchiveDB folder
				MiniFile.deleteFileOrFolder(txpowsqlfolder.getAbsolutePath(), txpowsqlfolder);
				
				//And reload..
				mTxPoWDB	= new TxPoWDB(jdbcUrl);
				mTxPoWDB.loadSQLDB(new File(txpowsqlfolder,"txpow"));
			}
			
			//Load the MaximaDB
			File maxsqlfolder = new File(basedb,"maximasql");
			if(!GeneralParams.IS_MAIN_DBPASSWORD_SET) {
				mMaximaDB.loadDB(new File(maxsqlfolder,"maxima"));
			}else {
				mMaximaDB.loadEncryptedSQLDB(new File(maxsqlfolder,"maxima"),GeneralParams.MAIN_DBPASSWORD);
			}
			
			//Load the MDS DB
			File mdssqlfolder = new File(basedb,"mdssql");
			if(!GeneralParams.IS_MAIN_DBPASSWORD_SET) {
				mMDSDB.loadDB(new File(mdssqlfolder,"mds"));
			}else {
				mMDSDB.loadEncryptedSQLDB(new File(mdssqlfolder,"mds"),GeneralParams.MAIN_DBPASSWORD);
			}
			
			//Load the User Prefs
//			mUserDB.loadEncryptedDB(GeneralParams.MAIN_DBPASSWORD, new File(basedb,"userprefs.db"));
			mUserDB.loadDB(new File(basedb,"userprefs.db"));
			
			//Load the custom Txns..
			mTxnDB = new TxnDB();
			mTxnDB.loadDB();
			
			//Load the Cascade
			mCascade.loadDB(new File(basedb,"cascade.db"));
			
			//Load the TxPoWTree
			mTxPoWTree.loadDB(new File(basedb,"chaintree.db"));
			
			//And finally..
//			mP2PDB.loadEncryptedDB(GeneralParams.MAIN_DBPASSWORD, new File(basedb,"p2p.db"));
			mP2PDB.loadDB(new File(basedb,"p2p.db"));
			
			//Do we need to store the cascade in the ArchiveDB
			getArchive().checkCascadeRequired(getCascade());
			
		}catch(Exception exc) {
			MinimaLogger.log("SERIOUS ERROR loadAllDB ");
			MinimaLogger.log(exc);
			
			//At this point.. STOP..
			Runtime.getRuntime().halt(0);
//			System.exit(1);
		}
		
		//Release the krakken
		writeLock(false);
	}
	
	public void loadArchiveAndTxPoWDB(boolean zResetWallet) {
		
		//We need read lock 
		writeLock(true);
		
		try {
			
			//Get the base Database folder
			File basedb = getBaseDBFolder();
			
			//Wallet
			if(zResetWallet) {
				mWallet					= new Wallet(jdbcUrl);
				File walletsqlfolder 	= new File(basedb,"walletsql");
				if(!GeneralParams.IS_MAIN_DBPASSWORD_SET) {
					mWallet.loadDB(new File(walletsqlfolder,"wallet"));
				}else {
					mWallet.loadEncryptedSQLDB(new File(walletsqlfolder,"wallet"),GeneralParams.MAIN_DBPASSWORD);
				}
			}
			
			//Set the Archive folder
			mArchive			= new ArchiveManager(jdbcUrl);
			File archsqlfolder 	= new File(basedb,"archivesql");
			mArchive.loadDB(new File(archsqlfolder,"archive"));
			
			//Load the SQL DB
			mTxPoWDB			= new TxPoWDB(jdbcUrl);
			File txpowsqlfolder = new File(basedb,"txpowsql");
			mTxPoWDB.loadSQLDB(new File(txpowsqlfolder,"txpow"));
			
		}catch(Exception exc) {
			MinimaLogger.log("SERIOUS ERROR loadArchiveAndTxPoWDB");
			MinimaLogger.log(exc);
		}
		
		//Release the krakken
		writeLock(false);
	}
	
	public void saveAllDB() {
		saveAllDB(false);
	}
	
	public void saveAllDB(boolean zCompact) {
		MinimaLogger.log("Saving State..");
		saveState();
		
		MinimaLogger.log("Saving SQL..");
		saveSQL(zCompact);
		
		MinimaLogger.log("All saved..");
	}
	
	public void saveSQL(boolean zCompact) {
		
		//We need lock 
		writeLock(true);
		
		try {
			//Clean shutdown of SQL DBs
			MinimaLogger.log("Wallet shutdown..");
			mWallet.saveDB(zCompact);
			MinimaLogger.log("Maxima shutdown..");
			mMaximaDB.saveDB(zCompact);
			MinimaLogger.log("MDSDB shutdown..");
			mMDSDB.saveDB(zCompact);
			MinimaLogger.log("TxPowDB shutdown..");
			mTxPoWDB.saveDB(zCompact);
			MinimaLogger.log("ArchiveDB shutdown..");
			mArchive.saveDB(zCompact);
			
		}catch(Exception exc) {
			MinimaLogger.log(exc);
		}
		
		//Release the krakken
		writeLock(false);
	}
	
	public void saveState() {
		
		//We need read lock 
		readLock(true);
		
		try {
			//Get the base Database folder
			File basedb = getBaseDBFolder();
			
			//JsonDBs
			mTxnDB.saveDB();
//			mUserDB.saveEncryptedDB(GeneralParams.MAIN_DBPASSWORD, new File(basedb,"userprefs.db"));
//			mP2PDB.saveEncryptedDB(GeneralParams.MAIN_DBPASSWORD, new File(basedb,"p2p.db"));
			mUserDB.saveDB(new File(basedb,"userprefs.db"));
			mP2PDB.saveDB(new File(basedb,"p2p.db"));
			
			//Custom
			mCascade.saveDB(new File(basedb,"cascade.db"));
			mTxPoWTree.saveDB(new File(basedb,"chaintree.db"));
			
		}catch(Exception exc) {
			MinimaLogger.log(exc);
		}
		
		//Release the krakken
		readLock(false);
	}
	
	public void saveUserDB() {
		
		//We need read lock 
		readLock(true);
		
		try {
			//Get the base Database folder
			File basedb = getBaseDBFolder();
			
			//JsonDBs
//			mUserDB.saveEncryptedDB(GeneralParams.MAIN_DBPASSWORD, new File(basedb,"userprefs.db"));
			mUserDB.saveDB(new File(basedb,"userprefs.db"));
			
		}catch(Exception exc) {
			MinimaLogger.log(exc);
		}
		
		//Release the krakken
		readLock(false);
	}
}
