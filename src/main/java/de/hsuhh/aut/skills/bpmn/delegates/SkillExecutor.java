package de.hsuhh.aut.skills.bpmn.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.connect.Connectors;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.HttpResponse;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;


//import okhttp3.OkHttpClient;
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import io.socket.emitter.Emitter;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SkillExecutor implements JavaDelegate {


	public void execute(DelegateExecution execution) throws Exception {

		// Hard coded variables
		final String HOST_URL = "http://localhost:9090";
		final String WEBSOCKET_TOPIC = "skills/state-changed";
		final String NEW_STATE_TYPE_IRI = "newStateTypeIri";
		final String CONNECTOR = "http-connector";
		final String EXECUTION_URL = "http://localhost:9090/api/skill-executions";
		final String COMMAND_TYPE_IRI_RESET = "http://www.hsu-ifa.de/ontologies/ISA-TR88#ResetCommand";
		final String COMMAND_TYPE_IRI_GETOUTPUTS = "http://www.w3id.org/hsu-aut/cask#GetOutputs";
		final String CONTENT_TYPE = "application/json";
		
		Object lockObject = new Object();

		// Error codes
		final String ERROR_ABORTING = "Error_Status_Aborting";
		final String ERROR_STOPPING = "Error_Status_Stopping";


		/***** Reading variables and deserialize them *****/
		System.out.println("Task " + execution.getCurrentActivityName() + " starting...");
		execution.getVariableNames().forEach((e) -> {
			System.out.println("Reading variable " + e + " ...");
		});

		Gson gson = new Gson();
		ExecutionRequest executionRequest = gson.fromJson(execution.getVariable("executionRequest").toString(), ExecutionRequest.class);
		System.out.println("Input msg: \n" + gson.toJson(executionRequest));

		Boolean selfResetting = executionRequest.isSelfResetting();

		/***** Create websocket listening to state changes *****/
		CurrentStateSocket socket = new CurrentStateSocket(new URI("ws://localhost:9091/skills"), lockObject);

		/***** Send actual execution request *****/
		HttpConnector http = Connectors.getConnector(CONNECTOR);
		HttpResponse response = http.createRequest() // Retrieve Error Code if task can not be started...
				.post().url(EXECUTION_URL).contentType(CONTENT_TYPE).payload(gson.toJson(executionRequest)).execute();
		System.out.println("Sending http request complete");


		/***** Keep thread alive and wait for socket to signal final state  *****/
		try {
			synchronized (lockObject) {
				while (!socket.reachedFinalState()) {
					lockObject.wait();
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Thread was interrupted");
		}
		
		System.out.println("Reached final state");
		StateTypeIri state = socket.getCurrentStateTypeIri();
		System.out.println(state);
						

		/***** Ask for outputs of the skill and return them to the process *****/
		ExecutionRequest getOutputs = new ExecutionRequest();
		getOutputs.setCommandTypeIri(COMMAND_TYPE_IRI_GETOUTPUTS);
		getOutputs.setSkillIri(executionRequest.getSkillIri());
		
		HttpResponse outputs = http.createRequest().post().url(EXECUTION_URL).contentType(CONTENT_TYPE).payload(gson.toJson(getOutputs)).execute();
		System.out.println("Request getOutputs: \n" + gson.toJson(getOutputs));
		System.out.println("Response getOutputs: \n" + outputs.getResponse());
		Type skillResponseType = new TypeToken<ArrayList<SkillResponse>>(){}.getType();
		List<SkillResponse> skillResponses = gson.fromJson(outputs.getResponse(), skillResponseType);
		
		
		// Every output gets set as "<activityID>_<output local name>"
		for (SkillResponse skillResponse : skillResponses) {
			String activityId = execution.getCurrentActivityId();
			String variableId = activityId + "_" + skillResponse.getName();
			execution.setVariable(variableId, skillResponse.getValue());
		}
		
		/***** Error handling *****/
		switch (state) {
		case Complete:
			System.out.println("Task ID" + execution.getCurrentActivityId() + " completed");
			break;
		case Stopped:
			System.out.println("STOPPING ERROR! Could not complete Task " + execution.getCurrentActivityName());
			socket.close();
			throw new BpmnError(ERROR_STOPPING);
		case Aborted:
			System.out.println("ABORTING ERROR! Could not complete Task " + execution.getCurrentActivityName());
			socket.close();
			throw new BpmnError(ERROR_ABORTING);
		default:
			System.out.println("A non final state was reached. Something went terribly wrong, it seems");
			break;
		}
		

		/***** Reset task if selfResetting *****/
		if (selfResetting) {
			System.out.println("Self Resetting is activated. Resetting Task " + execution.getCurrentActivityName());
			executionRequest.setCommandTypeIri(COMMAND_TYPE_IRI_RESET);
			http.createRequest().post().url(EXECUTION_URL).contentType(CONTENT_TYPE).payload(gson.toJson(executionRequest)).execute();
		}
		socket.close();
	}

}
