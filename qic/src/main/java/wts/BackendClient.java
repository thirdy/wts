package wts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


public class BackendClient {

//    private String cookies;
    private HttpClient client = HttpClientBuilder.create().build();
    
    int timeout = 30;
	int CONNECTION_TIMEOUT = timeout  * 1000; // timeout in millis
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
        .setConnectTimeout(CONNECTION_TIMEOUT)
        .setSocketTimeout(CONNECTION_TIMEOUT)
        .build();

    public BackendClient() {
	}

    public String post(String payload)
            throws Exception {
    	return post("http://poe.trade/search", payload);
    }
    
    public String post(String url, String payload)
    			throws Exception {
    	System.out.println("post() payload: " + payload);
    	System.out.println("post() url: " + url);
    	
        HttpPost post = new HttpPost(url);
        post.setConfig(requestConfig);

        // add header
        post.setHeader("Host", "poe.trade");
        post.setHeader("User-Agent", USER_AGENT);
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        post.setHeader("Accept-Language", "en-US,en;q=0.5");
        post.setHeader("Accept-Encoding", "gzip, deflate");
        post.setHeader("Referer", "http://poe.trade/");
    	post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        post.setHeader("Cookie", "_ga=GA1.2.750449977.1440808734; league=Warbands; _gat=1; mb_uid2=6130147680410288830"); // _ga=GA1.2.750449977.1440808734; league=Warbands; _gat=1; mb_uid2=6130147680410288830
        post.setHeader("Connection", "keep-alive");
//        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

//        post.setEntity(new UrlEncodedFormEntity(postParams));
        post.setEntity(new StringEntity(payload));

        System.out.println("Sending 'POST' request to URL : " + url);
        // bombs away!
        HttpResponse response = client.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        
        rd.close();
        
        String location = null;
        
//        System.out.println("Response Headers:");
        final Header[] allHeaders = response.getAllHeaders();
        for (Header header : allHeaders) {
//            System.out.println(header.toString());
            if (header.getName().equalsIgnoreCase("Location")) {
                location = header.getValue();
            }
        }
        
        return location;
//	 System.out.println(result.toString());
    }
    
    public String postXMLHttpRequest(String url, String payload)
    		throws Exception {
    	System.out.println("postXMLHttpRequest() payload: " + payload);
    	System.out.println("postXMLHttpRequest() url: " + url);
    	StringEntity entity = new StringEntity(payload);
    	
    	HttpPost post = new HttpPost(url);
    	post.setConfig(requestConfig);
    	
    	// add header
    	post.setHeader("Accept", "*/*");
    	post.setHeader("Accept-Encoding", "gzip, deflate");
    	post.setHeader("Accept-Language", "en-US,en;q=0.5");
    	post.setHeader("Cache-Control", "no-cache");
    	post.setHeader("Connection", "keep-alive");
//    	post.setHeader("Content-Length", String.valueOf(entity.getContentLength()));
    	post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        post.setHeader("Cookie", "_ga=GA1.2.750449977.1440808734; league=Warbands; _gat=1; mb_uid2=6130147680410288830"); // _ga=GA1.2.750449977.1440808734; league=Warbands; _gat=1; mb_uid2=6130147680410288830
    	post.setHeader("Host", "poe.trade");
    	post.setHeader("Pragma", "no-cache");
    	post.setHeader("Referer", url);
    	post.setHeader("User-Agent", USER_AGENT);
    	post.setHeader("X-Requested-With", "XMLHttpRequest");
    	
//        post.setEntity(new UrlEncodedFormEntity(postParams));
		post.setEntity(entity);
    	
    	System.out.println("Sending 'POST' request to URL : " + url);
    	// bombs away!
    	HttpResponse response = client.execute(post);
    	
    	int responseCode = response.getStatusLine().getStatusCode();
    	
    	System.out.println("Response Code : " + responseCode);
    	
    	BufferedReader rd = new BufferedReader(
    			new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    	
    	StringBuilder result = new StringBuilder();
    	String line = "";
    	while ((line = rd.readLine()) != null) {
    		result.append(line);
    	}
    	
    	rd.close();
    	
    	String location = null;
    	
//    	System.out.println("Response Headers:");
    	final Header[] allHeaders = response.getAllHeaders();
    	for (Header header : allHeaders) {
//    		System.out.println(header.toString());
    		if (header.getName().equalsIgnoreCase("Location")) {
    			location = header.getValue();
    		}
    	}
    	
    	return result.toString();
//	 System.out.println(result.toString());
    }
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0";

    public String get(String url) throws Exception {

        HttpGet get = new HttpGet(url);
        get.setConfig(requestConfig);
        
        get.setHeader("Host", "poe.trade");
        get.setHeader("User-Agent", USER_AGENT);
        get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.setHeader("Accept-Language", "en-US,en;q=0.5");
        get.setHeader("Accept-Encoding", "gzip, deflate");
        get.setHeader("Referer", "http://poe.trade/");
        get.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        post.setHeader("Cookie", "_ga=GA1.2.750449977.1440808734; league=Warbands; _gat=1; mb_uid2=6130147680410288830"); // _ga=GA1.2.750449977.1440808734; league=Warbands; _gat=1; mb_uid2=6130147680410288830
        
        HttpResponse response = client.execute(get);
        int responseCode = response.getStatusLine().getStatusCode();

        System.out.println("Sending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        
        rd.close();

        // set cookies
//        setCookies(response.getFirstHeader("Set-Cookie") == null ? ""
//                : response.getFirstHeader("Set-Cookie").toString());

        return result.toString();
    }

//    public String getCookies() {
//        return cookies;
//    }
//
//    public void setCookies(String cookies) {
//        this.cookies = cookies;
//    }

}