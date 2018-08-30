

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CityConfig {
	//Get city.list.json from openweathermap
	private static String CITY_JSON = System.getProperty("city.json");
	private static CityConfig config = new CityConfig();
	private HashMap<String, Integer> properties = new HashMap<String, Integer>();

	public static CityConfig getInstanse() {
		if (config == null) {
			synchronized (CityConfig.class) {
				config = new CityConfig();
			}
		}
		return config;
	}

	private CityConfig() {
		JSONParser parser = new JSONParser();
		try {
			File file = new File(CITY_JSON);
			FileReader reader = new FileReader(file);
			JSONArray citys = (JSONArray) parser.parse(reader);
			for (int i = 0; i < citys.size(); i++) {
				JSONObject city = (JSONObject) citys.get(i);
				properties.put(city.get("name").toString().toLowerCase(),
						Integer.valueOf(city.get("id").toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Integer> getProperty() {
		return properties;
	}
}
