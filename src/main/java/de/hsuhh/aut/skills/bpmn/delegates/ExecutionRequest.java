package de.hsuhh.aut.skills.bpmn.delegates;

//import de.hsuhh.aut.skills.bpmn.delegates.Parameters;

public class ExecutionRequest {
	// variables
	private String skillIri;
	private String commandTypeIri;
	private Parameters[] parameters;
	private Boolean selfResetting;
	
	
	// Getters & Setters
	public String getCommandTypeIri() {
		return commandTypeIri;
	}
	
	public void setCommandTypeIri(String commandTypeIri) {
		this.commandTypeIri = commandTypeIri;
	}
	
	public String getSkillIri() {
		return skillIri;
	}
	
	public void setSkillIri(String skillIri) {
		this.skillIri = skillIri;
	}
	
	public Parameters[] getParameters() {
		return parameters;
	}
	
	public void setParameters(Parameters[] parameters) {
		this.parameters = parameters;
	}

	public Boolean isSelfResetting() {
		return this.selfResetting;
	}
	
	public void setSelfResetting(Boolean selfResetting) {
		this.selfResetting = selfResetting;
	}

}
