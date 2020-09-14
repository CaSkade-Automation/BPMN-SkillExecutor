package de.hsuhh.aut.skills.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.connect.Connectors;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.HttpResponse;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyJavaDelegate implements JavaDelegate {
	
	// Hard coded variables
	private static String HOST_URL = "http://localhost:9090";
	private static String WEBSOCKET_TOPIC = "skills/state-changed";
	private static String NEW_STATE_TYPE_IRI = "newStateTypeIri";
	private static String CONNECTOR = "http-connector";
	private static String EXECUTION_URL = "http://localhost:9090/api/skill-executions";
	private static String COMMAND_TYPE_IRI_RESET = "http://www.hsu-ifa.de/ontologies/ISA-TR88#Reset_Command";
	private static String COMMAND_TYPE_IRI_GETOUTPUTS = "http://www.hsu-ifa.de/ontologies/capability-model#GetOutputs";
	private static String CONTENT_TYPE = "application/json";
	
	// Error codes
	private static String ERROR_ABBORTING = "Error_Status_Abborting";
	private static String ERROR_STOPPING = "Error_Status_Stopping";
	
	// Loading during runtime
	private Boolean isSelfResetting = false;
	private stateTypeIri status = stateTypeIri.Tba;
	
	
	public void execute(DelegateExecution execution) throws Exception {

		/***** Reading variables and saving them in Class *****/
		System.out.println("Task " + execution.getCurrentActivityName() + " starting...");
		execution.getVariableNames().forEach((e) -> {
			System.out.println("Reading variable " + e +" ...");
		});
		
		Gson gson = new Gson();
		SkillParameter skillParameter = gson.fromJson(execution.getVariable("input_msg").toString(), SkillParameter.class);
		System.out.println("Execution msg: \n" + gson.toJson(skillParameter));

		isSelfResetting = Boolean.parseBoolean(execution.getVariable("isSelfResetting").toString());
		System.out.println("Self resetting activated: " + isSelfResetting);
		
		/***** WebSocket *****/
		IO.Options options = new IO.Options();
        options.forceNew = true;
        OkHttpClient client = new OkHttpClient();
        options.webSocketFactory = client;
        options.callFactory = client;
		final Socket socket = IO.socket(HOST_URL, options);
		
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
		  @Override
		  public void call(Object... args) {
			System.out.println("Connection to WebSocket with URI " + HOST_URL + " successful");
		  }
		}).on(WEBSOCKET_TOPIC, new Emitter.Listener() {
		  @Override
		  public void call(Object... args) {
			  /***** Get status *****/
			  JSONObject json_reply = (JSONObject)args[0];
			  try {
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Idle.toString())) {status = stateTypeIri.Idle;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Starting.toString())) {status = stateTypeIri.Starting;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Execute.toString())) {status = stateTypeIri.Execute;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Completing.toString())) {status = stateTypeIri.Completing;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Complete.toString())) {status = stateTypeIri.Complete;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Held.toString())) {status = stateTypeIri.Held;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Holding.toString())) {status = stateTypeIri.Holding;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Stopping.toString())) {status = stateTypeIri.Stopping;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Stopped.toString())) {status = stateTypeIri.Stopped;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Aborting.toString())) {status = stateTypeIri.Aborting;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Aborted.toString())) {status = stateTypeIri.Aborted;}
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains(stateTypeIri.Resetting.toString())) {status = stateTypeIri.Resetting;}
				} catch (JSONException e) {
					e.printStackTrace();
					System.out.println("ERROR! Could not find " + NEW_STATE_TYPE_IRI + " in response-json. Listening to " + WEBSOCKET_TOPIC + " topic");
				}
				System.out.println("Status: " + status.toString());
		  }
		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
		  @Override
		  public void call(Object... args) {
			  System.out.println("Disconnected from WebSocket");
			  client.dispatcher().executorService().shutdown();
		  }
		});
		
		socket.connect();
		Thread.sleep(5000);
		if (!socket.connected()){System.out.println("ERROR! Socket is not connected");}
		
		/***** Send json *****/
		HttpConnector http = Connectors.getConnector(CONNECTOR);
		HttpResponse response = http.createRequest() 	// Retrieve Error Code if task can not be started...
		        .post()
		        .url(EXECUTION_URL)
		        .contentType(CONTENT_TYPE)
		        .payload(gson.toJson(skillParameter))
	        .execute();
		System.out.println("Response execution: \n" + response);
		System.out.println("Sending http request complete");
		
		/***** Keep thread alive until complete or aborted *****/ 
		SkillParameter getOutputs = new SkillParameter();
		getOutputs.setCommandTypeIri(COMMAND_TYPE_IRI_GETOUTPUTS);
		getOutputs.setSkillIri(skillParameter.getSkillIri());
		
		while (true) {
			if (status == stateTypeIri.Complete || status == stateTypeIri.Aborting || status == stateTypeIri.Stopping) {
				socket.close();
				HttpResponse outputs = http.createRequest()
				        .post()
				        .url(EXECUTION_URL)
				        .contentType(CONTENT_TYPE)
				        .payload(gson.toJson(getOutputs))
			        .execute();
				System.out.println("Request getOutputs: \n" + gson.toJson(getOutputs));
				System.out.println("Response getOutputs: \n" + outputs.getResponse());
				Type skillResponseType = new TypeToken<ArrayList<SkillResponse>>(){}.getType();
				List<SkillResponse> skillResponse = gson.fromJson(outputs.getResponse(), skillResponseType);
				
				/***** Return outputs *****/
				for (int i=0; i<skillResponse.size(); i++) {
					execution.setVariable(skillResponse.get(i).getIri(), skillResponse.get(i).getValue());
				}
				
				/***** Error handling *****/
				if (status == stateTypeIri.Complete) {
					System.out.println("Task " + execution.getCurrentActivityName() + " completed");
					break;
				} else if (status == stateTypeIri.Aborting) {
					System.out.println("ERROR! Could not complete Task " + execution.getCurrentActivityName());
					throw new BpmnError(ERROR_ABBORTING);
				} else if (status == stateTypeIri.Stopping) {
					System.out.println("ERROR! Could not complete Task " + execution.getCurrentActivityName());
					throw new BpmnError(ERROR_STOPPING);
				} 	
			}
			Thread.sleep(1000);
		}
		
		/***** Resetting Task *****/
		if(isSelfResetting) { 
			System.out.println("Self Resetting is activated. Resetting Task " + execution.getCurrentActivityName());
			skillParameter.setCommandTypeIri(COMMAND_TYPE_IRI_RESET);
			http.createRequest()
			        .post()
			        .url(EXECUTION_URL)
			        .contentType(CONTENT_TYPE)
			        .payload(gson.toJson(skillParameter))
		        .execute();
		}
	}
	
}
