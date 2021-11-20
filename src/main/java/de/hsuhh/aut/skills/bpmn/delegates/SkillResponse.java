package de.hsuhh.aut.skills.bpmn.delegates;


// TODO: This should be aligned with the parameters class. There is no need to have two classes
public class SkillResponse {
	private String iri;
	private String name;
	private String type;
	private String required;
	private double value;
	
	
	public String getIri() {
		return iri;
	}
	public void setIri(String iri) {
		this.iri = iri;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	public double getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	

}
