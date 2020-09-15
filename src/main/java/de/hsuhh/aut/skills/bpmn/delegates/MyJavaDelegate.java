package de.hsuhh.aut.skills.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.connect.Connectors;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.HttpResponse;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

//import org.json.JSONArray;
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
	
	int check = 0;
	
	public void execute(DelegateExecution execution) throws Exception {
		
		// Hard coded variables
		final String HOST_URL = "http://localhost:9090";
		final String WEBSOCKET_TOPIC = "skills/state-changed";
		final String NEW_STATE_TYPE_IRI = "newStateTypeIri";
		final String CONNECTOR = "http-connector";
		final String EXECUTION_URL = "http://localhost:9090/api/skill-executions";
		final String COMMAND_TYPE_IRI_RESET = "http://www.hsu-ifa.de/ontologies/ISA-TR88#Reset_Command";
		final String COMMAND_TYPE_IRI_GETOUTPUTS = "http://www.hsu-ifa.de/ontologies/capability-model#GetOutputs";
		final String CONTENT_TYPE = "application/json";
		
		// Error codes
		final String ERROR_ABORTING = "Error_Status_Aborting";
		final String ERROR_STOPPING = "Error_Status_Stopping";

		Boolean isSelfResetting = false;
		

		/***** Reading variables and saving them in Class *****/
		System.out.println("Task " + execution.getCurrentActivityName() + " starting...");
		execution.getVariableNames().forEach((e) -> {
			System.out.println("Reading variable " + e +" ...");
		});
		
		Gson gson = new Gson();
		SkillParameter skillParameter = gson.fromJson(execution.getVariable("input_msg").toString(), SkillParameter.class);
		System.out.println("Input msg: \n" + gson.toJson(skillParameter));

		isSelfResetting = Boolean.parseBoolean(execution.getVariable("isSelfResetting").toString());
		
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
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains("Complete")) {
					  check = 1;
				  }
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains("Stopping")) {
					  check = 2;
				  }
				  if(json_reply.getString(NEW_STATE_TYPE_IRI).contains("Aborting")) {
					  check = 3;
				  }
				} catch (JSONException e) {
					e.printStackTrace();
					System.out.println("ERROR! Could not find " + NEW_STATE_TYPE_IRI + " in response-json. Listening to " + WEBSOCKET_TOPIC + " topic");
				}
		  }
		}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
		  @Override
		  public void call(Object... args) {
			  System.out.println("Disconnected from WebSocket");
			  client.dispatcher().executorService().shutdown();
		  }
		});
		socket.connect();
		
		/***** Send json *****/
		HttpConnector http = Connectors.getConnector(CONNECTOR);
		HttpResponse response = http.createRequest() 	// Retrieve Error Code if task can not be started...
		        .post()
		        .url(EXECUTION_URL)
		        .contentType(CONTENT_TYPE)
		        .payload(gson.toJson(skillParameter))
	        .execute();
		System.out.println("Sending http request complete");
		
		if (!socket.connected()){System.out.println("ERROR! Socket is not connected");}
		
		/***** Keep thread alive until completed, stopping or aborting *****/ 
		while (check == 0) {
				Thread.sleep(2000);
				System.out.println("Waiting for completion of task...");
		}
		
		/***** Return outputs *****/
		SkillParameter getOutputs = new SkillParameter();
		getOutputs.setCommandTypeIri(COMMAND_TYPE_IRI_GETOUTPUTS);
		getOutputs.setSkillIri(skillParameter.getSkillIri());
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
		for (int i=0; i<skillResponse.size(); i++) {
			execution.setVariable(skillResponse.get(i).getIri(), skillResponse.get(i).getValue());
		}
		
		/***** Error handling *****/
		if (check == 1) {
			System.out.println("Task " + execution.getCurrentActivityName() + " completed");
		}
		if (check == 2) {
			System.out.println("STOPPING ERROR! Could not complete Task " + execution.getCurrentActivityName());
			socket.close();
			throw new BpmnError(ERROR_STOPPING);
		}
		if (check == 3) {
			System.out.println("ABORTING ERROR! Could not complete Task " + execution.getCurrentActivityName());
			socket.close();
			throw new BpmnError(ERROR_ABORTING);
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
		socket.close();
		Thread.sleep(2000);
	}
	
}
