package de.hsuhh.aut.skills.bpmn.delegates;

public class SkillMexWebsocketMessage <T> {

	private String type;
	private T body;
	
	public T getBody() {
		return this.body;
	}
	
	public String getType() {
		return this.type;
	}
}
