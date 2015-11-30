package wts;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class BlackmarketLanguage {
	
	Map<String, String> dictionary = new HashMap<>();
	
	public BlackmarketLanguage() throws IOException {
		File keywords = new File("keywords");
		File[] files = keywords.listFiles();
		for (File file : files) {
			List<String> lines = FileUtils.readLines(file);
			Map<String, String> map = lines.stream()
				.filter(s -> isNotBlank(s) && !s.startsWith(";"))
				.collect(Collectors.toMap(
						s -> substringBefore(s, "=").trim(),
						s -> substringAfter (s, "=").trim()));
			dictionary.putAll(map);
		}
	}

	public String parse(String input) {
		List<String> tokens = asList(StringUtils.split(input));
		// translate tokens using language dictionary
		List<String> translated = tokens.stream().filter(token -> !isSortToken(token)).map(this::processToken).collect(toList());
		String finalResult = translated.stream().collect(joining("&"));
		finalResult = asList(finalResult.split("&")).stream().map(this::encodeQueryParm).collect(joining("&"));
		return finalResult;
	}
	
	public String parseSortToken(String input) throws IllegalArgumentException {
		String finalResult = "price_in_chaos";
		List<String> tokens = asList(StringUtils.split(input));
		// translate tokens using language dictionary
		List<String> translated = tokens.stream().filter(token -> isSortToken(token)).map(this::processToken).collect(toList());
		if (translated.size() > 1) {
			throw new IllegalArgumentException("More than 1 sort token detected. Only one is allowed.");
		}
		if (translated.size() == 1) {
			finalResult = translated.get(0);
		}
		return finalResult;
	}

	String processToken(String token) {
		String result = null;
		for (Entry<String, String> entry : dictionary.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			// if token matches directly
			if (key.equalsIgnoreCase(token)) {
				result = value;
				break;
			}
			// if matches by regex
			Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(token);
			if (matcher.matches()) {
				result = value;
				// replace placeholder with values captured from regex
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String placeholder = "$GROUP" + i;
					if (result.contains(placeholder)) {
						result = result.replace(placeholder, matcher.group(i));
					}
				}
			}
		}
		return result;
	}
	
	boolean isSortToken(String token) {
		return token.toLowerCase().startsWith("sort");
	}
	
	String encodeQueryParm(String queryParam) {
		String key = substringBefore(queryParam, "=");
		String value = substringAfter(queryParam, "=");
		try {
			value = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); 
		}
		return key + "=" + value;
	}
}