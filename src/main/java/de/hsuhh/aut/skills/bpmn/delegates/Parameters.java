package de.hsuhh.aut.skills.bpmn.delegates;
import com.google.gson.annotations.SerializedName;

public class Parameters {
	private String name;
	private String type;
	private String required;
	@SerializedName("default")
	private String defaultValue; 
	private int value; // generic?
}
