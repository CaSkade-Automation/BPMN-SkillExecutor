package de.hsuhh.aut.skills.bpmn.delegates;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.google.gson.Gson;
import com.google.gson.reflect.*;

@ClientEndpoint
public class CurrentStateSocket {

	private Session userSession = null;
	private Object lockObject;
	private boolean inFinalState;
	private StateTypeIri finalState;

	public CurrentStateSocket(URI endpointURI, Object lockObject) {
		this.lockObject = lockObject;
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
        System.out.println("Opened session");
    }
 
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }
 
   
    @OnMessage
    public void onMessage(String message) {
    	Gson gson = new Gson();
    	Type messageType = new TypeToken<SkillMexWebsocketMessage<StateChangeInfo>>(){}.getType();
    	SkillMexWebsocketMessage<StateChangeInfo> stateChangeMessage = gson.fromJson(message, messageType);
    	
    	if (!stateChangeMessage.getType().equals("StateChanged")) return;
    	
    	synchronized (lockObject) {
        	String newStateTypeIri = stateChangeMessage.getBody().getStateTypeIri();
        	
        	if(StateTypeIri.isFinalState(newStateTypeIri)) {
        		inFinalState = true;
        		finalState = StateTypeIri.fromString(newStateTypeIri);
            	lockObject.notify();	
        	}
		}
    }
    
    public boolean reachedFinalState() {
    	return this.inFinalState;
    }
    
    public StateTypeIri getCurrentStateTypeIri() {
    	return this.finalState;
    }
    
    public void close() {
    	try {
			this.userSession.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
