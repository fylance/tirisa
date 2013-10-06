package de.fylance.tirisa.FormElements;

import java.util.ArrayList;

public class TirisaHtmlMultiSelect implements TirisaFormElementInterface {
	
	private String type="multiselect";
	
	private String identifier="";
	private String identificationMethod="";
	private String getterName="";
	private String value="";
	
	private ArrayList<String> stringOptionsList = null;

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public void setIdentifier(String i) {
		this.identifier = i;
	}

	@Override
	public String getIdentificationMethod() {
		return this.identificationMethod;
	}

	@Override
	public void setIdentificationMethod(String im) {
		this.identificationMethod = im;
		
	}

	@Override
	public String getGetterName() {
		return this.getterName;
	}

	@Override
	public void setGetterName(String gn) {
		this.getterName = gn;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String v) {
		this.value = v;
	}
	
	public ArrayList<String> getStringOptionsList() {
		return this.stringOptionsList;
	}
	
	public void setStringOptionsList(ArrayList<String> list) {
		this.stringOptionsList = list;
	}
}
