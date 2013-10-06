package de.fylance.tirisa;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class BasePageObject {
	
	protected HtmlPage page = null;

	public BasePageObject(HtmlPage page) {
		this.setPage(page);
	}

	public HtmlPage getPage() {
		return page;
	}

	public void setPage(HtmlPage page) {
		this.page = page;
	}

}
