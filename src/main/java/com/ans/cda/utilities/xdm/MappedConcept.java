package com.ans.cda.utilities.xdm;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MappedConcept {
	@JsonProperty("Concept")
	public ArrayList<Concept> concept;
}
