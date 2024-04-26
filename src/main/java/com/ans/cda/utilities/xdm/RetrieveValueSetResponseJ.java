package com.ans.cda.utilities.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RetrieveValueSetResponseJ {
	@JsonProperty("xmlns")
	public String xmlns;
	@JsonProperty("xmlns:xsi")
	public String xmlnsxsi;
	@JsonProperty("ValueSet")
	public ValueSetJ valueSet;
}
