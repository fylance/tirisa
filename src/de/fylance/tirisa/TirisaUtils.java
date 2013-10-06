package de.fylance.tirisa;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TirisaUtils {
	
	public static String camelize(String value, boolean startWithUpperCase) {
		char[] chars = value.toCharArray();
		String result = "";
		boolean capitalize = true;
		if(!startWithUpperCase) {
			capitalize = false;
		}
		for (int i=0; i<chars.length; i++) {
			if((Character.isDigit(chars[i]) || Character.isLetter(chars[i]))) {
				if(capitalize) {
					result += Character.toString(Character.toUpperCase(chars[i]));
					capitalize = false;
				}
				else {
					result += Character.toString(Character.toLowerCase(chars[i]));
				}
			}
			else {
				capitalize = true;
			}
		}
		return result;
	}
	
	public static String firstCharToUppercase(String string) {
		char first = Character.toUpperCase(string.charAt(0));
		return first + string.substring(1);
	}
	
	public static String firstCharToLowercase(String string) {
		char first = Character.toLowerCase(string.charAt(0));
		return first + string.substring(1);
	}
	
	public static String getXPathFromDomNode(HtmlPage page, DomNode node) {
		String xpath = "";
		String tagName = "";
		DomNode rootNode = page.getFirstByXPath("/"); // linearized recursion termination case
		DomNode parentNode = null;
		int counter = 0;
		int childIdentificationNumber = 0;
		while (!rootNode.equals(parentNode)) {
			tagName = node.getFirstByXPath("name()");
			parentNode = node.getFirstByXPath("..");
			List<?> children = parentNode.getByXPath("./"+tagName);
			for(int i=0; i<children.size(); i++) {
				counter++;
				if(node == children.get(i)) {
					childIdentificationNumber = counter;
					break;
				}
			}
			xpath = "/"+tagName+"["+childIdentificationNumber+"]"+xpath;
			counter = 0;
			node = parentNode;
		}
		
		return xpath;
	}

}
