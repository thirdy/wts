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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import wts.SearchPageScraper.SearchResultItem;
import wts.util.CommandLine;

/**
 * @author thirdy
 *
 */
public class Main {
	
	public static Properties config;
	public static BlackmarketLanguage language;

	public static void main(String[] args) throws Exception {
		config = loadConfig();
		language = new BlackmarketLanguage();
		CommandLine cmd = new CommandLine(args);
		
		String query = cmd.getArguments()[0];
		String sort = cmd.getNumberOfArguments() == 2 ? cmd.getArguments()[1] : "price_in_chaos";
		
		String html = runSearch(query, sort);
		
		SearchPageScraper scraper = new SearchPageScraper(html);
		List<SearchResultItem> items = scraper.parse();
		items.stream().forEach(i -> System.out.println(i.getWTB()));
    }

    private static Properties loadConfig() throws IOException, FileNotFoundException {
		Properties config = new Properties();
		try (BufferedReader br = new BufferedReader(new FileReader(new File("config.properties")))) {
			config.load(br);
		}
		return config;
	}
    
//	public static String runSearch(String query) {
//		return runSearch(query, "price_in_chaos");
//	}
	public static String runSearch(String query, String sort) {
    	String queryPrefix = config.getProperty("queryprefix");
		String finalQuery = queryPrefix + " " + query;
		System.out.println("finalQuery: " + finalQuery);
		System.out.println("sort: " + sort);
		String payload = language.parse(finalQuery);
		long start = System.currentTimeMillis();
		BackendClient backendClient = new BackendClient();
		String searchPage = backendClient.search(payload , "sort=" + sort + "&bare=true");
		long end = System.currentTimeMillis();
		System.out.println("Took " + (end - start) + " ms");
		return searchPage;
	}
}
