package org.minima.database.txpowdb.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.minima.database.txpowdb.TxPOWDBRow;
import org.minima.database.txpowdb.TxPowDB;
import org.minima.objects.TxPoW;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.system.Main;
import org.minima.system.brains.BackupManager;
import org.minima.utils.MinimaLogger;
import org.minima.utils.SQLHandler;
import org.minima.utils.json.JSONObject;

public class SQLTxPoWDB implements TxPowDB{

	SQLHandler mSQL = null;
	
	public SQLTxPoWDB() {
		BackupManager backup = Main.getMainHandler().getBackupManager();
		File txpowfolder     = backup.getTxPOWFolder();
		File db = new File(txpowfolder, "txpowdb");
		
		init(db.getAbsolutePath());
	}
	
	public SQLTxPoWDB(String zDataBase) {
		init(zDataBase);
	}
	
	private void init(String zDataBase) {
		try {
			mSQL = new SQLHandler(zDataBase);
		} catch (Exception e) {
			MinimaLogger.log(e);
			return;
		}
	
		//Now create some tables..
		String sql = "CREATE TABLE IF NOT EXISTS txpow ( "
				+ "txpowid VARCHAR(160) NOT NULL, "
//				+ "isblock INTEGER NOT NULL, "
//				+ "istransaction INTEGER NOT NULL,"
				+ "fulltxpow LONGVARCHAR  NOT NULL )";
		
		//Run it..
		mSQL.executeSQL(sql);
	}
	
	public void close(){
		try {
			mSQL.close(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean existsTxPow(MiniData zTxPOWID) {
		String sql = "SELECT COUNT(*) as tot FROM txpow WHERE txpowid='"+zTxPOWID.to0xString()+"'";
		
		JSONObject res = mSQL.executeSQL(sql);
		
		MinimaLogger.log(res.toString());
		
		return false;
	}
	
	@Override
	public TxPOWDBRow addTxPOWDBRow(TxPoW zTxPOW) {
		//Create the SQL
		String sql = "INSERT INTO txpow (txpowid, fulltxpow) "
		+ "VALUES ('"+zTxPOW.getTxPowID().to0xString()+"','"+zTxPOW.toJSON().toString()+"');";
		
		JSONObject res = mSQL.executeSQL(sql);
		MinimaLogger.log(res.toString());
		
		return null;
	}

	@Override
	public TxPOWDBRow findTxPOWDBRow(MiniData zTxPOWID) {
		String sql = "SELECT fulltxpow FROM txpow WHERE txpowid='"+zTxPOWID.to0xString()+"'";
		
		JSONObject res = mSQL.executeSQL(sql);
		
		MinimaLogger.log(res.toString());
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<TxPOWDBRow> getAllTxPOWDBRow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetAllInBlocks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetBlocksFromOnwards(MiniNumber zFromBlock) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTxPOW(MiniData zTxPOWID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<TxPOWDBRow> removeTxPOWInBlockLessThan(MiniNumber zBlockNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<TxPOWDBRow> getAllUnusedTxPOW() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<TxPOWDBRow> getChildBlocksTxPOW(MiniData zParent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<TxPOWDBRow> getAllBlocksMissingTransactions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ClearDB() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] zArgs) {
		//DO it..
		SQLTxPoWDB db = new SQLTxPoWDB("tester2.sql");
	
		//Create a random txpow..
		TxPoW tx = new TxPoW();
		tx.calculateTXPOWID();
		
		MinimaLogger.log(tx.toString());
		
		//Close the connection
		db.close();
	}
	
}
