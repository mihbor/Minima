package org.minima.system.network.minidapps.minilib;

import java.lang.reflect.InvocationTargetException;

import org.minima.utils.ProtocolException;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class BackEndDAPP {

	/**
	 * JavaScript
	 */
	String mScript;
	
	/**
	 * The Context it is running in
	 */
	Context mContext;
	
	/**
	 * The Script Scope
	 */
	Scriptable mScope;
	
	/**
	 * The MinimaEvent Function inside the scope
	 */
	Function mMinimEventJS;
	
	/**
	 * The MiniDAPP ID..
	 */
	String mMiniDAPPID;
	
	public BackEndDAPP(String zScriptJS, String zMiniDAPPID) throws ProtocolException, IllegalAccessException, InstantiationException, InvocationTargetException {
		//The MINIDAPP
		mMiniDAPPID = zMiniDAPPID;
		
		//The JavaScrit
		mScript = zScriptJS;
		
		//Context and scope of the JS backend
		mContext = Context.enter();
		mScope   = mContext.initStandardObjects();
	
		//Create a MinimaJS object for this scope..
		MinimaJS minimajs = new MinimaJS(this);
		
		//Add it to this environment
		Object wrappedMinima = Context.javaToJS(minimajs, mScope);
		ScriptableObject.putProperty(mScope, "Minima", wrappedMinima);
	
		//Define a special Class..
		ScriptableObject.defineClass(mScope, Counter.class);
		
//		ScriptableObject.defineClass(mScope, WebSocket.class);

		
		//Evaluate the script
		mContext.evaluateString(mScope, zScriptJS, "<cmd>", 1, null);
	
		//Get the function
		Object fObj = mScope.get("MinimaEvent", mScope);
		
		if (!(fObj instanceof Function)) {
		    throw new ProtocolException("BackEnd JS MinimaEvent is undefined or not a function.");
		} 
		
		//Store for later
		mMinimEventJS = (Function)fObj;
	}
	
	public Context getContext() {
		return mContext;
	}
	
	public Scriptable getScope() {
		return mScope;
	}
	
	public String getMiniDAPPID() {
		return mMiniDAPPID;
	}
	
	public void shutdown() {
		mContext.exit();
	}
	
	/**
	 * When an Event occurs send the message to the JS scope
	 * 
	 * - newblock
	 * - newtransction
	 * - newbalance
	 */
	public void MinimaEvent(String zJSONEvent) {
		//Create a JS JSONObject
		Object json = JSMiniLibUtil.makeJSONObject(zJSONEvent, mContext, mScope);
		
		//Make a function variable list
		Object functionArgs[] = { json };
	    
		//Call the Function..
		mMinimEventJS.call(mContext, mScope, mScope, functionArgs);
	}
	
	/**
	 * Weird little function to convert a Java JSONObject to a JS one..
	 * @param zJSON
	 * @param rhino
	 * @param scope
	 * 
	 * @return The JS JSONObect
	 */
		
	//tester
	public static void main(String[] zArgs) {
	
		String js = 
				"\n" + 
				"function MinimaEvent(evt){\n" + 
				"\n" + 
				"	var jsonstr = JSON.stringify(evt,null,2);\n" + 
				"	Minima.log(\"MinimaEvent : \"+jsonstr);\n" + 
				"	//tot++;\n" + 
				"	\n" + 
				"	//Open up a websocket to the main MINIMA proxy..\n" + 
				"	var WEBSOCK = new org.minima.system.network.minidapps.minilib.WebSocket(\"127.0.0.1:80\");\n" + 
				"	\n" + 
				"	WEBSOCK.run = function(){\n" + 
				"		Minima.log(\"onopen\");\n" + 
				"	};\n" + 
				"	\n" + 
				"\n" + 
				"	//obj = { run: function() { print(\"hi\"); } }\n" + 
				"	\n" + 
				"	//Minima.log(WEBSOCK.present());\n" + 
				"	\n" + 
				"	//WEBSOCK.onopen = function() {\n" + 
				"	//	Minima.log(\"onopen\");\n" + 
				"	//};\n" + 
				"	\n" + 
				"	\n" + 
				"	\n" + 
				"	//var cc = new Counter();\n" + 
				"	//Minima.log(cc.count);\n" + 
				"}\n" + 
				"";
		try {
		
			BackEndDAPP bdapp = new BackEndDAPP(js,"0x0001");

			JSONObject test = new JSONObject();
			test.put("tster", "hello");
			
			//And send a JSON msg..
			JSONObject newblock = new JSONObject();
			newblock.put("event", "newblock");
			newblock.put("status", "the status");
			newblock.put("time", 1020344);
			newblock.put("jsom", test);
			newblock.put("bool", true);
			newblock.put("dounble", 23.345);
			
			JSONArray arr = new JSONArray();
			arr.add("help");
			arr.add(new Integer(45));
			arr.add(new Long(45));
			
			newblock.put("array", arr);
			
			bdapp.MinimaEvent(newblock.toString());
		
			bdapp.shutdown();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}