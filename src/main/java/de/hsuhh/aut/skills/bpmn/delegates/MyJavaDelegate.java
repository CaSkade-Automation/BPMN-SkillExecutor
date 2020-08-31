package de.hsuhh.aut.skills.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.connect.Connectors;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.*;

import okhttp3.OkHttpClient;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.util.Random;

public class MyJavaDelegate implements JavaDelegate {
	
	// Hardcoded variables
	private static String SKILLIRI = "skillIri";
	private static String COMMAND = "commandTypeIri";
	private static String PARAMETERS = "parameters";
	private static String HOST_URL = "http://localhost:9090";
	private static String WEBSOCKET_TOPIC = "skills/state-changed";
	private static String CONNECTOR = "http-connector";
	private static String EXECUTION_URL = "http://localhost:9090/api/skill-executions";
	private static String COMMAND_TYPE_IRI_RESET = "http://www.hsu-ifa.de/ontologies/ISA-TR88#Reset_Command";
	private static String CONTENT_TYPE = "application/json";
	private static String NEW_STATE_TYPE_IRI = "newStateTypeIri";
	private static String ERROR_CODE = "TASK_EXECUTION_ERROR";
	private static String ISSELFRESETTING = "isSelfResetting";
	
	// Loading during runtime
	private String skillIri;
	private String commandTypeIri;
	private String parameters;	
	private String isSelfResetting = "false";
	private stateTypeIri status = stateTypeIri.Tba;
	
	
	public void execute(DelegateExecution execution) throws Exception {

		/***** Reading variables and saving them in json *****/
		// Put only one String here...
		System.out.println("Task " + execution.getCurrentActivityName() + " starting...");
		execution.getVariableNames().forEach((e) -> {
			System.out.println("Reading variable " + e +" ...");
		});
		
//		String msg = execution.getVariable("msg").toString();
//		System.out.println(msg);
//		JSONParser parser = new JSONParser();
//		JSONObject json = (JSONObject) parser.parse(msg);
//		System.out.println(json.toString());
		
//		JSONObject execution_msg = new JSONObject(msg);
//	    System.out.println("execution_msg: \n" + execution_msg.toString());
		
//		skillIri = execution.getVariable(SKILLIRI).toString();
//		commandTypeIri = execution.getVariable(COMMAND).toString();
//		parameters = execution.getVariable(PARAMETERS).toString();
//		isSelfResetting = execution.getVariable(ISSELFRESETTING).toString();
//		JSONArray para = new JSONArray("[" + parameters + "]");
//		JSONObject execution_msg = new JSONObject();
//		execution_msg.put(SKILLIRI, (String)skillIri);
//		execution_msg.put(COMMAND, (String)commandTypeIri);
//		execution_msg.put(PARAMETERS, (Object)para);
//		System.out.println(execution_msg.toString());
		
//		/***** WebSocket *****/
//		IO.Options options = new IO.Options();
//        options.forceNew = true;
//        OkHttpClient client = new OkHttpClient();
//        options.webSocketFactory = client;
//        options.callFactory = client;
//		final Socket socket = IO.socket(HOST_URL, options);
//		
//		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//		  @Override
//		  public void call(Object... args) {
//			System.out.println("Connection to WebSocket with URI " + HOST_URL + " successful");
//		  }
//		}).on(WEBSOCKET_TOPIC, new Emitter.Listener() {
//		  @Override
//		  public void call(Object... args) {
//			  /***** Get status *****/
//			  JSONObject json_reply = (JSONObject)args[0];
//			  try {
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Starting.toString())) {status = stateTypeIri.Starting;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Execute.toString())) {status = stateTypeIri.Execute;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Completing.toString())) {status = stateTypeIri.Completing;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Complete.toString())) {status = stateTypeIri.Complete;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Stopping.toString())) {status = stateTypeIri.Stopping;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Stopped.toString())) {status = stateTypeIri.Stopped;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Aborting.toString())) {status = stateTypeIri.Aborting;}
//				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Aborted.toString())) {status = stateTypeIri.Aborted;}
//				} catch (JSONException e) {
//					e.printStackTrace();
//					System.out.println("ERROR! Could not find " + NEW_STATE_TYPE_IRI + " in response-json. Listening to " + WEBSOCKET_TOPIC + " topic");
//				}
//				System.out.println("Status: " + status.toString());
//		  }
//		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
//		  @Override
//		  public void call(Object... args) {
//			  System.out.println("Disconnected from WebSocket");
//			  client.dispatcher().executorService().shutdown();
//		  }
//		});
//		
//		socket.connect();
//		Thread.sleep(5000);
//		if (!socket.connected()){System.out.println("ERROR! Socket is not connected");}
//		
//		/***** Send json *****/
//		HttpConnector http = Connectors.getConnector(CONNECTOR);
//		HttpResponse response = http.createRequest() 	// Retrieve Error Code if task can not be started...
//		        .post()
//		        .url(EXECUTION_URL)
//		        .contentType(CONTENT_TYPE)
//		        .payload(execution_msg.toString())
//	        .execute();
//		System.out.println("Sending http request complete");
//		
//		/***** Keep thread alive until complete or aborted *****/ 
//		while (true) {
//			if (status == stateTypeIri.Complete) {
//				System.out.println("Task " + execution.getCurrentActivityName() + " completed");
//				break;
//			} else if (status == stateTypeIri.Aborted || status == stateTypeIri.Aborting) {
//				System.out.println("ERROR! Could not complete Task " + execution.getCurrentActivityName());
//				throw new BpmnError(ERROR_CODE);
//			}
//		}
//		
//		/***** Return outputs *****/
//		Random rando = new Random();
//		execution.setVariable("output1","Zufällige Zahl " + rando.nextInt());
//		execution.setVariable("output2","Zufällige Zahl " + rando.nextInt());
//		execution.setVariable("output3","Zufällige Zahl " + rando.nextInt());
//		execution.setVariable("output4","Zufällige Zahl " + rando.nextInt());
		
		/***** Resetting Task *****/
//		if(isSelfResetting.equals("true")) { 
//			System.out.println("Self Resetting is activated. Resetting Task now");
//			execution_msg.put(COMMAND, (String)COMMAND_TYPE_IRI_RESET);
//			System.out.println("Resetting Task " + execution.getCurrentActivityName());
//			http.createRequest()
//			        .post()
//			        .url(EXECUTION_URL)
//			        .contentType(CONTENT_TYPE)
//			        .payload(execution_msg.toString())
//		        .execute();
//		}
		
		// Close WebSocket
//		socket.close();
	}

}
