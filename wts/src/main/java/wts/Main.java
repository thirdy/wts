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

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.startsWithAny;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import wts.SearchPageScraper.SearchResultItem;
import wts.util.SwingUtil;

/**
 * @author thirdy
 *
 */
public class Main {
	
	public static Properties config;
	public static BlackmarketLanguage language;
	static String ahkPath;
	static String logPath;
	static String ahkScript;
	static int pageSize;
	
	List<SearchResultItem> items = Collections.emptyList();
	int currentPage = 0;
	
	private long lastKnownPosition = 0;
	
	public static void main(String[] args) throws Exception {
		try {
			reloadConfig();
			new Main();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error occured: " + e.getMessage());
			throw e;
		}
    }

	private static void reloadConfig() throws IOException, FileNotFoundException {
		config = loadConfig();
		language = new BlackmarketLanguage();
		ahkPath = config.getProperty("ahkpath");
		if(!new File(ahkPath).exists()) JOptionPane.showMessageDialog(null, "Your AHK path is incorrect: " + ahkPath + ". Update your config.properties file.");
		logPath = config.getProperty("poelogpath");
		if(!new File(logPath).exists()) JOptionPane.showMessageDialog(null, "Your Path of Exile Logs path is incorrect: " + logPath + ". Update your config.properties file.");
		ahkScript = config.getProperty("ahkscript", "wts.ahk");
		pageSize = Integer.parseInt(config.getProperty("pageSize", "5"));
	}
	
	public Main() throws IOException, InterruptedException {
		
//		CommandLine cmd = new CommandLine(args);
//		String query = cmd.getArguments()[0];
//		String sort = cmd.getNumberOfArguments() == 2 ? cmd.getArguments()[1] : "price_in_chaos";
		
		File logFile = new File(logPath);
		lastKnownPosition = logFile.length();
		exit: while (true) {
			Thread.sleep(100);
			long fileLength = logFile.length();
			if (fileLength > lastKnownPosition) {
				RandomAccessFile readWriteFileAccess = new RandomAccessFile(logFile, "rw");
				readWriteFileAccess.seek(lastKnownPosition);
				String line = null;
				while ((line = readWriteFileAccess.readLine()) != null) {
					System.out.println(line);
					boolean exit = processLine(line);
					if (exit) {
						break exit;
					}
				}
				lastKnownPosition = readWriteFileAccess.getFilePointer();
				readWriteFileAccess.close();
			}
		}
	}
	
	private boolean processLine(String line) throws IOException {
		line = substringAfterLast(line, ":").trim();
		
		if (!startsWithAny(line, new String[]{"#", "@", "$"})) {
			if (line.equalsIgnoreCase("searchexit")) {
				setDisplayMessage("$EXIT");
				return true; 
			}
			if (line.equalsIgnoreCase("searchend") || line.equalsIgnoreCase("se")) {
				setDisplayMessage("$EXIT");
				items = Collections.emptyList();
			} else if (isNumeric(line) && !items.isEmpty()) {
				int idx = Integer.parseInt(line);
				String wtb = items.get(idx).getWTB();
				SwingUtil.copyToClipboard(wtb);
			} else if (line.equalsIgnoreCase("n") && !items.isEmpty()) {
				updateDisplay(++currentPage);
			} else if (line.equalsIgnoreCase("p") && !items.isEmpty() && currentPage >= 1) {
				updateDisplay(--currentPage);
			} else if (line.equalsIgnoreCase("reload")) {
				reloadConfig();
			} else if (line.toLowerCase().matches("pagesize\\d+")) {
				String strPageSize = substringAfter(line.toLowerCase(), "pagesize");
				if (isNumeric( strPageSize )) {
					pageSize = Integer.valueOf( strPageSize );
				}
			} else if (line.toLowerCase().matches("view\\d+") && items.size() > 0) {
				String strIdx = substringAfter(line.toLowerCase(), "view");
				if (isNumeric( strIdx )) {
					int idx = Integer.valueOf( strIdx );
					String msg = items.get(idx).toString().replace(System.lineSeparator(), "$LF");
					setDisplayMessage(msg);
				}
			} else if (line.startsWith("search")) {
				String terms = substringAfter(line, "search").trim();
				if (!terms.isEmpty()) {
					runSearch(terms);
				}
			} else if (line.startsWith("s ")) {
				String terms = substringAfter(line, "s ").trim();
				if (!terms.isEmpty()) {
					runSearch(terms);
				}
			}
		}
		return false;
	}

	private void runSearch(String terms) throws IOException {
		// TODO, handle sorting terms
		String query = terms;
		String sort  = language.parseSortToken(terms);
		String html;
		try {
			html = downloadHtml(query, sort);
			SearchPageScraper scraper = new SearchPageScraper(html);
			items = scraper.parse();
			System.out.println("items found: " + items.size());
			currentPage = 0;
			updateDisplay(currentPage);
		} catch (Exception e) {
			setDisplayMessage(("Error: " + e.getMessage()));
		}
	}

	private void updateDisplay(int page) throws IOException {
		int skip = (pageSize * page) % items.size();
		if (skip >= items.size()) {
			--currentPage;
			return;
		}
		String result = items.stream().skip(skip).limit(pageSize).map(i -> i.toDisplay("$LF")).collect(joining("$LF$LF"));
		setDisplayMessage(result);
	}

	private void setDisplayMessage(String msg) throws IOException {
		Process p = new ProcessBuilder(ahkPath, ahkScript, msg).start();
	}

	public String downloadHtml(String query, String sort) throws Exception {
    	String queryPrefix = config.getProperty("queryprefix");
		String finalQuery = queryPrefix + " " + query;
		System.out.println("finalQuery: " + finalQuery);
		System.out.println("sort: " + sort);
		String payload = language.parse(finalQuery);
		long start = System.currentTimeMillis();
		String searchPage = backendDownload(sort, payload);
		long end = System.currentTimeMillis();
		System.out.println("Took " + (end - start) + " ms");
		return searchPage;
	}

	private String backendDownload(String sort, String payload) throws Exception {
		BackendClient backendClient = new BackendClient();
		String searchPage = "";
		sort = URLEncoder.encode(sort, "UTF-8");
		sort = "sort=" + sort + "&bare=true";
		String url = "http://poe.trade/search";
		String location = backendClient.post(url, payload);
		// Add a bit of delay, just in case
		Thread.sleep(30);
		searchPage = backendClient.postXMLHttpRequest(location, sort);
		return searchPage;
	}
	
    private static Properties loadConfig() throws IOException, FileNotFoundException {
		Properties config = new Properties();
		try (BufferedReader br = new BufferedReader(new FileReader(new File("config.properties")))) {
			config.load(br);
		}
		return config;
	}
}
