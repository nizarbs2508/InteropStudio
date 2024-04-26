package com.ans.cda.utilities.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValueSetJdv {
	@JsonProperty("ConceptList")
	public ConceptList conceptList;
	public String urlFichier;
	public String displayName;
	public String typeFichier;
	public String description;
	public String dateFin;
	public String id;
	public long dateValid;
	public long dateMaj;
}
