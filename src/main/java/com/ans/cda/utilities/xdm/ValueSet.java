package com.ans.cda.utilities.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValueSet {
	@JsonProperty("MappedConceptList")
	public MappedConceptList mappedConceptList;
	public String urlFichier;
	public String displayName;
	public String typeFichier;
	public String description;
	public String dateFin;
	public String id;
	public long dateValid;
	public long dateMaj;
}
