package com.ans.cda.utilities.xdm;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MappedConceptList {
	@JsonProperty("MappedConcept")
	public ArrayList<MappedConcept> mappedConcept;
}
