package com.ans.cda.utilities.xdm;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConceptList {
	@JsonProperty("Concept")
	public ArrayList<ConceptJdv> concept = new ArrayList<>();
	/**
	 * system
	 */
	private String system;

	/**
	 * @return the concept
	 */
	public ArrayList<ConceptJdv> getConcept() {
		return concept;
	}

	/**
	 * @param concept the concept to set
	 */
	public void setConcept(ArrayList<ConceptJdv> concept) {
		this.concept = concept;
	}

	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * @param system the system to set
	 */
	public void setSystem(String system) {
		this.system = system;
	}
}
