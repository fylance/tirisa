package de.fylance.tirisa.FormElements;

public interface TirisaFormElementInterface {
	public String getType();
	
	public String getIdentifier();
	public void setIdentifier(String i);
	
	public String getIdentificationMethod();
	public void setIdentificationMethod(String im);
	
	public String getGetterName();
	public void setGetterName(String gn);
	
	public String getValue();
	public void setValue(String v);
}
