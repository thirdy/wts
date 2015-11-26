/*
 * Copyright (C) 2015 thirdy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package wts;

import java.io.File;
import java.net.MalformedURLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import wts.util.SwingUtil;

/**
 * @author thirdy
 *
 */
public class SearchView extends StackPane {
	
	private WebView webView = new WebView();
	private WebEngine webEngine = webView.getEngine();
	private MainStage mainStage;
	
	public SearchView() {
		webEngine.getLoadWorker().stateProperty().addListener((observ, oldVal, newVal) -> {
			if (newVal.equals(Worker.State.SUCCEEDED)) {
				JSObject window = (JSObject) webEngine.executeScript("window");
				window.setMember("assistcallback", SearchView.this);
			}
		});
		getChildren().add(webView);
	}

	public void reload(String html) {
		System.out.println(html.substring(0, 100));
		if (html != null) {
			html = addHeadElements(html);
			webEngine.loadContent(html);
		}
	}

	private String addHeadElements(String html) {
		String htmlDirectory = htmlDirectory();
		Document doc = Jsoup.parse(html);
		Element head = doc.head();

		// Replace everthing in the <head>
		head.children().stream().forEach(e -> e.remove());
		head.appendElement("meta").attr("charset", "utf-8");
		head.appendElement("meta").attr("name", "viewport").attr("content", "width=device-width");
		head.appendElement("title").text("poe.trade.assist");
		head.appendElement("script").attr("type", "text/javascript").attr("src", htmlDirectory + "packed.js");
		head.appendElement("link").attr("rel", "stylesheet").attr("href", htmlDirectory + "packed_dark.css");
		
		doc.body().appendElement("script").attr("type", "text/javascript").attr("src", htmlDirectory + "assist.js");
		
		String cleanHTML = doc.toString();
//		try {
//			FileUtils.writeStringToFile(new File("test"), cleanHTML);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		return cleanHTML;
	}

	private String htmlDirectory() {
		try {
			return new File("html").toURI().toURL().toString();
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
	}

	public void sortClick(String sort) {
		System.out.println("SORT: " + sort);
		mainStage.runSearch(sort);
	}
	public void copyToClipboard(String s) {
		SwingUtil.copyToClipboard(s);
	}

	public void setMainState(MainStage mainStage) {
		this.mainStage = mainStage;
	}
}
