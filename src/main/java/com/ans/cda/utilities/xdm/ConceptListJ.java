package com.ans.cda.utilities.xdm;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConceptListJ {
	@JsonProperty("Concept")
	public ArrayList<ConceptJ> concept = new ArrayList<>();

}
