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

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static wts.Main.config;
import static wts.Main.language;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import wts.util.Dialogs;
 
public class MainStage extends Application {

	SearchView browser = new SearchView();
	TextField searchTF = new TextField();
	Button searchButton = new Button("Search");
	ProgressIndicator progressIndicator;
	SearchService service;
	
    @Override
    public void start(Stage stage) {
    	StackPane root = new StackPane();
    	BorderPane container = new BorderPane();
    	
    	Region veilOfTheNight = new Region();
		veilOfTheNight.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7)");
		progressIndicator = new ProgressIndicator(-1.0f);
		progressIndicator.setMaxSize(150, 150);
        
        HBox controls = new HBox(5, searchButton, searchTF);
        HBox.setHgrow(searchTF, Priority.ALWAYS);
        browser.setMainState(this);
        container.setCenter(browser);
        container.setBottom(controls);
        controls.setPadding(new Insets(0, 5, 0, 5));
        root.getChildren().addAll(container, veilOfTheNight, progressIndicator);
        root.setStyle("-fx-background-color: transparent;"); 
        Scene scene = new Scene(root);
//        stage.getIcons().add(new Image("/48px-Durian.png"));
//        stage.titleProperty().bind(
//        		new SimpleStringProperty("poe.trade.assist v5 (Durian) - ")
//        			.concat(autoSearchService.messageProperty()) );
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setWidth(800);
        stage.setHeight(550);
//        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
//        searchPane.searchTable.requestFocus();
        
        searchButton.setOnAction(e -> runSearch());
        
    	String poelogpath = config.getProperty("poelogpath");
		File poelogfile = new File(poelogpath);
		if (!poelogfile.exists()) {
			Dialogs.showError("Your poelogpath is incorrect, correct it on your config.properties file: " + poelogpath);
		}
		Tailer tail = Tailer.create(poelogfile, new TailerListenerAdapter() {
			private AtomicBoolean toggle = new AtomicBoolean(false);
			@Override
			public void handle(String line) {
				System.out.println(line);
				String userinput = substringAfterLast(line, ":").trim();
				System.out.println("userinput: " + userinput);
				if(startsWith(userinput, "search") && toggle.compareAndSet(false, true)) {
					System.out.println("Showing stage");
					Platform.runLater(() -> {
						stage.show();
						String query = substringAfter(userinput, "search").trim();
						if (!query.isEmpty()) {
							searchTF.setText(query);
							searchButton.fire();
						}
					});
				}
				if (startsWith(userinput, "close") && toggle.compareAndSet(true, false)) {
					System.out.println("Hiding stage");
					stage.hide();
				}
			}
		}, 1000, true);
		Thread thread = new Thread(tail);
//	    thread.setDaemon(true); // optional
	    thread.start();
	    
	    stage.setOnCloseRequest(e -> tail.stop());
	    
	    service = new SearchService();
	    service.setOnSucceeded(e -> browser.reload(service.getValue()));
	    service.setOnFailed(	e -> Dialogs.showError(service.getException()));
	    progressIndicator.visibleProperty().bind(service.runningProperty());
	    veilOfTheNight.visibleProperty().bind(service.runningProperty());
    }

	void runSearch() {
		runSearch("price_in_chaos");
	}
	void runSearch(String sort) {
    	String queryPrefix = config.getProperty("queryprefix");
		String query = queryPrefix + " " + searchTF.getText();
		System.out.println("query: " + query);
		String payload = language.parse(query);
		service.payload = payload;
		service.sort = sort;
		service.restart();
	}
	
	private static class SearchService extends Service<String> {
		
		private String payload;
		private String sort;

		@Override
		protected Task<String> createTask() {
			return new Task<String>() {

				@Override
				protected String call() throws Exception {
					long start = System.currentTimeMillis();
					BackendClient backendClient = new BackendClient();
					String searchPage = backendClient.search(payload , "sort=" + sort + "&bare=true");
					long end = System.currentTimeMillis();
					System.out.println("Took " + (end - start) + " ms");
					return searchPage;
				}
			};
		}
		
	}

}