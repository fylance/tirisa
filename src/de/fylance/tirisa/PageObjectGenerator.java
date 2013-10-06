package de.fylance.tirisa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import de.fylance.tirisa.FormElements.TirisaFormElementInterface;
import de.fylance.tirisa.FormElements.TirisaHtmlButtonButtonInput;
import de.fylance.tirisa.FormElements.TirisaHtmlButtonSubmitInput;
import de.fylance.tirisa.FormElements.TirisaHtmlCheckBoxInput;
import de.fylance.tirisa.FormElements.TirisaHtmlFileInput;
import de.fylance.tirisa.FormElements.TirisaHtmlImageInput;
import de.fylance.tirisa.FormElements.TirisaHtmlMultiSelect;
import de.fylance.tirisa.FormElements.TirisaHtmlPasswordInput;
import de.fylance.tirisa.FormElements.TirisaHtmlRadioButtonInput;
import de.fylance.tirisa.FormElements.TirisaHtmlSelect;
import de.fylance.tirisa.FormElements.TirisaHtmlSubmitInput;
import de.fylance.tirisa.FormElements.TirisaHtmlTextArea;
import de.fylance.tirisa.FormElements.TirisaHtmlTextInput;


// Doc: limitations of xpath-finder (namespaces etc.)
// Doc: not perfectly optimal for select boxes, especially multiple!!!
// Doc: details on webclient (does not close)
// Doc: exclude disabled elements
// Doc: no namespaces @xpath finder, no unique assurance c.f. htmlunit parses?? -> duplicates problems!!!
// Doc: precedence: elements with id != "" -> elements with name != "" -> relevant form elements by xpath from root node 
// Doc: does not generate submit method if submit button no child of form tag!!!
// Doc: generator properties -> paths etc.
// Doc: generate methods
// Doc: sample form on some server!
// Doc: bad idea though robust: duplicate ids (or names)
// Doc: does not work: forms with duplicate ids or names!!!
// Doc: NO HTML5 forms etc.

// ToDo: test duplicates error solution
// ToDo: refactor calcIdentifier calcGetterName !?
// ToDo: test duplicates storage and administration -> recognize duplicate form ids / names -> use getters from global table
// ToDo: optimize naming conventions in code etc.
// ToDo: in-code commenting
// ToDo: classes/methods javadoc
// ToDo: licencing etc (every file)
// ToDo: improve xpath canonicals: find parent id 
// ToDo: ability to add templates for over browsers/webdrivers
// Future Work: eclipse plugin -> generate + get camelized xpath etc. AND/OR check classes before compilation if objects need to be created
// Future Work: FF plugin -> get xpath or something like that

public class PageObjectGenerator {
	
	private HtmlPage page;
	private GeneratorProperties genProps;
	
	public PageObjectGenerator(Object obj) {
		this.genProps = new GeneratorProperties(obj);
	}
	
	public PageObjectGenerator(GeneratorProperties generatorProperties) {
		this.genProps = generatorProperties;
	}
	
	
	public HtmlPage generate(String pageObjectIdentifier, String url) {
		return generate(pageObjectIdentifier, url, false);
	}
	
	public HtmlPage generate(String pageObjectIdentifier, String url, WebClient webclient) {
		return generate(pageObjectIdentifier, url, webclient, false);
	}
	
	/**
     * {@inheritDoc}
     */
	public HtmlPage generate(String pageObjectIdentifier, String url, boolean forceGeneration) {
		WebClient webclient = new WebClient(); 
		webclient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		return generate(pageObjectIdentifier, url, webclient, forceGeneration);
	}
	
	/**
     * blablabla
     * @param pageObjectIdentifier a
     * @param url
     * @param webclient b
     * @param forceGeneration c
     */
	public HtmlPage generate(String pageObjectIdentifier, String url, WebClient webclient, boolean forceGeneration) {
		try {
			this.page = webclient.getPage(url);
			//webclient.closeAllWindows();
		} catch (FailingHttpStatusCodeException | IOException ioe) {
			ioe.printStackTrace();
		}
		return generate(pageObjectIdentifier, page, forceGeneration);
	}
	
	public HtmlPage generate(String pageObjectIdentifier, HtmlPage page) {
		return generate(pageObjectIdentifier, page, false);
	}
	
	public HtmlPage generate(String pageObjectIdentifier, HtmlPage page, boolean forceGeneration) {
		this.page = page;
		pageObjectIdentifier = TirisaUtils.camelize(pageObjectIdentifier, true);
		Map<String, String> map = new HashMap<String, String>();
		Map<String, Object> nodeGetterTable = new HashMap<String, Object>();
		String id= "";
		
		// prepare elements with id attribute
		List<?> idElements = null;
		ArrayList<HashMap<String, String>> listIdElements=new ArrayList<HashMap<String, String>>();
		HtmlOption option = null;
		DomNode idElement = null;
		DomAttr attr = null;
		int counter = 0;
		String getterMethodName = "";
		String xpath = "";
		idElements = page.getByXPath("//*[@id]");
		for (int i = 0; i < idElements.size(); i++) {
			idElement = (DomNode) idElements.get(i);
			attr = (DomAttr) idElement.getFirstByXPath("./@id");
			id = attr.getTextContent();
			counter = 1;
			xpath = "id('"+id+"')";
			getterMethodName = TirisaUtils.camelize(id, true)+"ById";
			if(nodeGetterTable.containsKey(getterMethodName)) {
				xpath = TirisaUtils.getXPathFromDomNode(page, idElement);
				while(nodeGetterTable.containsKey(getterMethodName)) {
					counter++;
					getterMethodName = TirisaUtils.camelize(id + String.valueOf(counter), true)+"ById";
				}
				nodeGetterTable.put(getterMethodName, idElement);
				System.out.println("WARNING: DUPLICATE FOUND: id='"+id+"'");
			}
			else {
				nodeGetterTable.put(getterMethodName, idElement);
			}
			
			map = new HashMap<String, String>();
			map.put("methodName", getterMethodName);
			map.put("xpath", xpath);
			map.put("id", id);
			listIdElements.add((HashMap<String, String>) map);
		}
		
		// prepare elements without id attribute, but with name attribute
		List<?> nameElements = null;
		ArrayList<HashMap<String, String>> listNameElements=new ArrayList<HashMap<String, String>>();
		DomNode nameElement = null;
		String name = "";
		String gettingMethod = "";
		nameElements = page.getByXPath("//*[@name and not(@id)]");
		for (int i = 0; i < nameElements.size(); i++) {
			nameElement = (DomNode) nameElements.get(i);
			attr = (DomAttr) nameElement.getFirstByXPath("./@name");
			name = attr.getTextContent();
			counter = 1;
			getterMethodName = TirisaUtils.camelize(name, true)+"ByName";
			gettingMethod = "getElementByName(\""+name+"\")";
			if(nodeGetterTable.containsKey(getterMethodName)) {
				
				while(nodeGetterTable.containsKey(getterMethodName)) {
					counter++;
					getterMethodName = TirisaUtils.camelize(name + String.valueOf(counter), true)+"ByName";
				}
				xpath = TirisaUtils.getXPathFromDomNode(page, nameElement);
				gettingMethod = "getFirstByXPath(\""+xpath+"\")";
				nodeGetterTable.put(getterMethodName, nameElement);
				System.out.println("WARNING: DUPLICATE FOUND: name='"+name+"'");
			}
			else {
				nodeGetterTable.put(getterMethodName, nameElement);
			}
			map = new HashMap<String, String>();
			map.put("methodName",getterMethodName);
			map.put("gettingMethod", gettingMethod);
			map.put("name", name);
			listNameElements.add((HashMap<String, String>) map);
		}
		
		
		// prepare form elements without id nor name attribute
		ArrayList<Map<String, String>> listXPathElements=new ArrayList<Map<String, String>>();
		
		// prepare forms
		List<HtmlForm> formElements = null;
		formElements = page.getForms();
		ArrayList<Map<String, Object>> listFormElements=new ArrayList<Map<String, Object>>();
		HtmlForm form = null;
		Map<String, Object> formMap = new HashMap<String, Object>();
		ArrayList<TirisaFormElementInterface> listFormInputElements=new ArrayList<TirisaFormElementInterface>();
		ArrayList<TirisaFormElementInterface> listFormSubmitElements=new ArrayList<TirisaFormElementInterface>();
		for (int i = 0; i < formElements.size(); i++) {
			form = formElements.get(i);
			DomAttr formIdAttr = form.getFirstByXPath("./@id");
			DomAttr formNameAttr = form.getFirstByXPath("./@name");
			String identifier = "";
			String fillerMethodName = "";
			if (null != formIdAttr) {
				identifier = "id = "+formIdAttr.getTextContent();
				fillerMethodName = TirisaUtils.camelize(formIdAttr.getTextContent(), true);
			}
			if(null != formNameAttr) {
				identifier = "name = "+formNameAttr.getTextContent();
				fillerMethodName = TirisaUtils.camelize(formNameAttr.getTextContent(), true);
			}
			if ("" != identifier) {
				formMap = new HashMap<String, Object>();
				formMap.put("identifier", identifier);
				formMap.put("fillerMethodName", fillerMethodName);
				
				List<?> formInputElements = null;
				formInputElements = form.getByXPath(".//*[self::input[((@type='text' or @type='password' or @type='checkbox' or @type='radio' or @type='file' or @type='submit' or @type='image') and not(@disabled))] | self::select[not(@disabled)] | self::textarea[not(@disabled)] | self::button[(@type='button' or @type='submit') and not(@disabled)]]");
				map = new HashMap<String, String>();
				listFormInputElements = null;
				listFormInputElements=new ArrayList<TirisaFormElementInterface>();
				listFormSubmitElements = null;
				listFormSubmitElements=new ArrayList<TirisaFormElementInterface>();
				for (int j = 0; j < formInputElements.size(); j++) {
					DomNode input = (DomNode) formInputElements.get(j);
					DomAttr inputIdAttr = input.getFirstByXPath("./@id");
					String inputId = "";
					if(null != inputIdAttr) {
						inputId = inputIdAttr.getTextContent();
					}
					DomAttr inputNameAttr = input.getFirstByXPath("./@name");
					String inputName = "";
					if(null != inputNameAttr) {
						inputName = inputNameAttr.getTextContent();
					}
					
					String inputXPath = "";
					if(inputId.equals("") && inputName.equals("")) {
						inputXPath = TirisaUtils.getXPathFromDomNode(this.page, input);
						map = new HashMap<String, String>();
						String getterName = TirisaUtils.camelize(inputXPath, false)+"ByXPath";
						map.put("methodName", TirisaUtils.camelize(inputXPath, true)+"ByXPath");
						map.put("xpath", inputXPath);
						listXPathElements.add(map);
						nodeGetterTable.put(getterName, input);
					}
					
					DomAttr inputTypeAttr = input.getFirstByXPath("./@type");
					String inputType = "";
					if(null != inputTypeAttr) {
						inputType = inputTypeAttr.getTextContent();
					}
					DomAttr inputMultipleAttr = input.getFirstByXPath("./@multiple");
					boolean inputMultiple = false;
					if(null != inputMultipleAttr) {
						inputMultiple = true;
					}
					String inputTag = input.getFirstByXPath("name()");
					
					// text + password
					if(inputType.equals("text")) {
						
						// name eventually wrong (counter!!!!)
						TirisaHtmlTextInput textInput = new TirisaHtmlTextInput();
						textInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						textInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						textInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						String inputValue = "";
						DomAttr inputValueAttr = input.getFirstByXPath("./@value");
						if(null != inputValueAttr) {
							inputValue = inputValueAttr.getTextContent();
						}
						textInput.setValue(inputValue);
						listFormInputElements.add(textInput);
					}
					
					if(inputType.equals("password")) {
						TirisaHtmlPasswordInput passwordInput = new TirisaHtmlPasswordInput();
						passwordInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						passwordInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						passwordInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						String inputValue = "";
						DomAttr inputValueAttr = input.getFirstByXPath("./@value");
						if(null != inputValueAttr) {
							inputValue = inputValueAttr.getTextContent();
						}
						passwordInput.setValue(inputValue);
						listFormInputElements.add(passwordInput);
					}
					
					// checkbox
					if(inputType.equals("checkbox")) {
						TirisaHtmlCheckBoxInput checkboxInput = new TirisaHtmlCheckBoxInput();
						checkboxInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						checkboxInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						checkboxInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						checkboxInput.setValue("");
						if(((HtmlCheckBoxInput)input).isDefaultChecked()) {
							checkboxInput.setValue("checked");
						}
						listFormInputElements.add(checkboxInput);
					}
					
					// radio
					if(inputType.equals("radio")) {
						TirisaHtmlRadioButtonInput radioInput = new TirisaHtmlRadioButtonInput();
						radioInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						radioInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						radioInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						radioInput.setValue("");
						if(((HtmlRadioButtonInput)input).isDefaultChecked()) {
							radioInput.setValue("checked");
						}
						listFormInputElements.add(radioInput);
					}
					
					// select + multiselect
					if(inputTag.equals("select")) {
						if(inputMultiple) {
							TirisaHtmlMultiSelect multiSelectInput = new TirisaHtmlMultiSelect();
							String selectInputIdentifier = this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable);
							multiSelectInput.setIdentifier(selectInputIdentifier);
							multiSelectInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
							multiSelectInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
							HtmlSelect select = (HtmlSelect) input;
							List<HtmlOption> selectHtmlOptionsList = select.getOptions();
							ArrayList<String> selectStringOptionsList = new ArrayList<String>();
							for (int k = 0; k < selectHtmlOptionsList.size(); k++) {
								option = selectHtmlOptionsList.get(k);
								selectStringOptionsList.add(selectInputIdentifier+TirisaUtils.camelize(option.getValueAttribute(), true));
							}
							multiSelectInput.setStringOptionsList(selectStringOptionsList);
							listFormInputElements.add(multiSelectInput);
						}
						else {
							TirisaHtmlSelect selectInput = new TirisaHtmlSelect();
							selectInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
							selectInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
							selectInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
//							HtmlSelect select = (HtmlSelect) page.getElementById(mySelectId);
//							HtmlOption option = select.getOptionByValue(desiredOptionValue);
//							select.setSelectedAttribute(option, true);
							listFormInputElements.add(selectInput);
						}
					}
					
					// + textarea
					if(inputTag.equals("textarea")) {
						TirisaHtmlTextArea textareaInput = new TirisaHtmlTextArea();
						textareaInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						textareaInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						textareaInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						listFormInputElements.add(textareaInput);
					}
					
					// + file upload
					if(inputType.equals("file")) {
						TirisaHtmlFileInput fileInput = new TirisaHtmlFileInput();
						fileInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						fileInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						fileInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						listFormInputElements.add(fileInput);
					}
					
					
					// submit
					if(inputTag.equals("input") && inputType.equals("submit")) {
						TirisaHtmlSubmitInput submitInput = new TirisaHtmlSubmitInput();
						submitInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						submitInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						submitInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable)+"");
						listFormSubmitElements.add(submitInput);
					}
					
					// image
					if(inputType.equals("image")) {
						TirisaHtmlImageInput imageInput = new TirisaHtmlImageInput();
						imageInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						imageInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						imageInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						listFormSubmitElements.add(imageInput);
					}
					
					// button type button
					if(inputTag.equals("button") && inputType.equals("button")) {
						TirisaHtmlButtonButtonInput buttonButtonInput = new TirisaHtmlButtonButtonInput();
						buttonButtonInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						buttonButtonInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						buttonButtonInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						listFormSubmitElements.add(buttonButtonInput);
					}
					
					// button type input
					if(inputTag.equals("button") && inputType.equals("submit")) {
						TirisaHtmlButtonSubmitInput buttonSubmitInput = new TirisaHtmlButtonSubmitInput();
						buttonSubmitInput.setIdentifier(this.calcIdentifier(input, inputId, inputName, inputXPath, nodeGetterTable));
						buttonSubmitInput.setIdentificationMethod(this.calcIdentificationMethod(inputId, inputName));
						buttonSubmitInput.setGetterName(this.calcGetterName(input, inputId, inputName, inputXPath, nodeGetterTable));
						listFormSubmitElements.add(buttonSubmitInput);
					}
					
				}
				formMap.put("inputElements", listFormInputElements);
				formMap.put("submitElements", listFormSubmitElements);
				listFormElements.add(formMap);
			}
		}
		
//		Properties props = new Properties();
//		props.put("resource.loader", "jar");
//		props.put("jar.resource.loader.class", "org.apache.velocity.runtime.resource.loader.JarResourceLoader");
//		props.put("jar.resource.loader.path", "tirisa-0.1.jar");
		VelocityEngine ve = new VelocityEngine();
//		ve.init(props);
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        
        final String templatePathBPO = "BasePageObject.vm";
        InputStream inputBPO = this.getClass().getClassLoader().getResourceAsStream(templatePathBPO);
        if (inputBPO == null) {
            System.out.println("Template file doesn't exist");
        }
	    Template templateBPO = ve.getTemplate(templatePathBPO, "UTF-8");
	    
	    
	    VelocityContext contextBasePageObject = new VelocityContext();
	    contextBasePageObject.put("package", genProps.getTargetPackage());
	    contextBasePageObject.put("classname", "Base"+pageObjectIdentifier+"PageObject");
	    contextBasePageObject.put("superclassname", "BasePageObject");
	    contextBasePageObject.put("ids", listIdElements);
	    contextBasePageObject.put("names", listNameElements);
	    contextBasePageObject.put("xpaths", listXPathElements);
	    contextBasePageObject.put("forms", listFormElements);
	    
	    VelocityContext contextCustomPageObject = new VelocityContext();
	    contextCustomPageObject.put("package", genProps.getTargetPackage());
	    contextCustomPageObject.put("classname", pageObjectIdentifier+"PageObject");
	    contextCustomPageObject.put("superclassname", "Base"+pageObjectIdentifier+"PageObject");
	    
		try {
			// generate base page object file if hash value of input changed
			boolean generateBasePageObject = false;
			String basePageObjectHashFilename = genProps.getTargetPath()+"/.Base"+pageObjectIdentifier+"PageObject.crc";
			int pageHash = page.asXml().hashCode();
			
			File basePageObjectHashFile = new File(basePageObjectHashFilename);
			if(basePageObjectHashFile.exists()) {
				FileReader input = new FileReader(basePageObjectHashFilename);
				BufferedReader bufRead = new BufferedReader(input);
				String line = bufRead.readLine();
				int fileHash = Integer.parseInt(line);
				if(pageHash != fileHash) {
					generateBasePageObject = true;
				}
			}
			else {
				generateBasePageObject = true;
			}
			String basePageObjectFilename = genProps.getTargetPath()+"/Base"+pageObjectIdentifier+"PageObject.java";
			File basePageObjectFile = new File(basePageObjectFilename);
			if(!basePageObjectFile.exists()) {
				generateBasePageObject = true;
			}
			if(forceGeneration) {
				generateBasePageObject = true;
			}
			
			if(generateBasePageObject) {
				BufferedWriter writerBasePageObject;
				writerBasePageObject = new BufferedWriter(new FileWriter(basePageObjectFilename));
				templateBPO.merge(contextBasePageObject, writerBasePageObject);
				writerBasePageObject.flush();
				writerBasePageObject.close();
				
				// generate base page object hash file
				BufferedWriter writerBasePageObjectHash;
				writerBasePageObjectHash = new BufferedWriter(new FileWriter(basePageObjectHashFilename));
				writerBasePageObjectHash.write(String.valueOf(pageHash));
				writerBasePageObjectHash.flush();
				writerBasePageObjectHash.close();
				System.out.println(basePageObjectFilename + " written.");
			}
			
			// generate custom page object file if it does not exist
			String customPageObjectFilename = genProps.getTargetPath()+"/"+pageObjectIdentifier+"PageObject.java";
			File f = new File(customPageObjectFilename);
			if(!f.exists()) {
		        final String templatePathCPO = "CustomPageObject.vm";
		        InputStream inputCPO = this.getClass().getClassLoader().getResourceAsStream(templatePathCPO);
		        if (inputCPO == null) {
		            System.out.println("Template file doesn't exist");
		        }
			    Template templateCPO = ve.getTemplate(templatePathCPO, "UTF-8");
				
				BufferedWriter writerCustomPageObject;
				writerCustomPageObject = new BufferedWriter(new FileWriter(customPageObjectFilename));
				templateCPO.merge(contextCustomPageObject, writerCustomPageObject);
				writerCustomPageObject.flush();
				writerCustomPageObject.close();
				System.out.println(customPageObjectFilename + " written.");
			}
			else {
				System.out.println(customPageObjectFilename + " exists and is up to date.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("finished!");
		return page;
	}
	
	private String calcIdentifier(DomNode node, String inputId, String inputName, String xPath, Map<String, Object> nodeGetterTable) {
		if(inputId.equals("") && inputName.equals("")) {
			return TirisaUtils.camelize(xPath, false);
		}
//		if(inputId.equals("")) {
//			return TirisaUtils.camelize(inputName, false);
//		}
//		return TirisaUtils.camelize(inputId, false);
		return TirisaUtils.firstCharToLowercase(this.getKeyForDomNode(nodeGetterTable, node));
	}
	
	private String calcIdentificationMethod(String inputId, String inputName) {
		if(inputId.equals("") && inputName.equals("")) {
			return "ByXPath";
		}
		if(inputId.equals("")) {
			return "ByName";
		}
		return "ById";
	}
	
	private String calcGetterName(DomNode node, String inputId, String inputName, String xPath, Map<String, Object> nodeGetterTable) {
		if(inputId.equals("") && inputName.equals("")) {
			return TirisaUtils.camelize(xPath, true)+"ByXPath";
		}
//		if(inputId.equals("")) {
//				return TirisaUtils.camelize(inputName, true);
//		}
//		return TirisaUtils.camelize(inputId, true);
		return TirisaUtils.firstCharToUppercase(this.getKeyForDomNode(nodeGetterTable, node));
	}
	
	private String getKeyForDomNode(Map<String, Object> nodeGetterTable, DomNode object ) {
		Set<String> keys = nodeGetterTable.keySet();
		Iterator<String> iter = keys.iterator();
	    while (iter.hasNext()) {
	    	String iterString = iter.next();
	    	if(nodeGetterTable.get(iterString).equals(object)) {
	    		return iterString;
	    	}
	    }
		return null;
	}
}
