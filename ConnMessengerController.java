

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConnMessengerController {

	public static final String PAGE_TOKEN = "YOUR_PAGE_TOKEN";
	public static final String VERIFY_TOKEN = "YOUR_VERIFY_TOKEN";

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/conn-messenger", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "application/json")
	@ResponseBody
	public String responseRequest(Model model, HttpServletRequest request, HttpServletResponse response) {
		String json;
		String mode = request.getParameter("hub.mode");
		String verify = request.getParameter("hub.verify_token");
		String challenge = request.getParameter("hub.challenge");
		JSONObject lineResp = new JSONObject();
		JSONObject messages = new JSONObject();
		HashMap<String, Integer> city = CityConfig.getInstanse().getProperty();
		try {
			if (mode != null && verify != null && mode.equals("subscribe") && verify.equals(VERIFY_TOKEN)) {
				return String.join("", challenge);
			}
			json = IOUtils.toString(request.getInputStream(), "UTF-8");
			if (json.isEmpty()) {
				throw new Exception("No request data");
			}
			JSONObject jsonData = new JSONObject(json);
			JSONArray entrys = jsonData.getJSONArray("entry");
			if (entrys == null || entrys.length() == 0) {
				throw new Exception("No entry data");
			}

			for (int entryIdx = 0; entryIdx < entrys.length(); entryIdx++) {
				JSONObject entry = ((JSONObject) entrys.get(entryIdx));
				JSONArray messagings = entry.getJSONArray("messaging");
				Map<String, Object> eventData = null;

				JSONObject messaging = ((JSONObject) messagings.getJSONObject(0));
				JSONObject sender = messaging.getJSONObject("sender");
				String messengerUUID = sender.getString("id");
				String fromText = null;
				JSONObject userProfile = null;

				if (messengerUUID != null)
					userProfile = MessengerActions.messengerUserProfile(PAGE_TOKEN, messengerUUID);
				
				if (userProfile != null)
					fromText = "Hi, " + userProfile.optString("first_name") + ", ";
				
				if (messaging.has("message")) {
					eventData = (Map<String, Object>) messaging.get("message");

					if (eventData != null && eventData.containsKey("text")) {
						String userInput = eventData.get("text").toString();
						fromText += "我已收到您的訊息「" + userInput + "」";
						userInput = userInput.toLowerCase();
						if(city.containsKey(userInput)){
							JSONObject weather = MessengerActions.weatherInfo((city.get(userInput)));
							fromText += "\r\nTemperature : " + weather.getJSONObject("main").get("temp");
							fromText += "\r\nDescription : " + weather.getJSONArray("weather").getJSONObject(0).get("description");
							MessengerActions.pushToMessenger(PAGE_TOKEN, messengerUUID, MessengerActions.imageToReplyMessage(weather.getJSONArray("weather").getJSONObject(0).getString(("icon"))));
							System.out.println(weather);
						} else {
							fromText += "\r\n不好意思沒找到相關城市訊息";
						}
						fromText += "\r\n\r\n我是天氣機器人, 請輸入一個城市，如Taipei";
						fromText += "\r\n(I am a WeatherBot, please input city name, ex.New York)";
					}
					messages = MessengerActions.textToReplyMessage(fromText);
					MessengerActions.pushToMessenger(PAGE_TOKEN, messengerUUID, messages);
				}
			}

		} catch (Exception e) {
			System.out.println("ERROR MSG : " + e.getMessage());
			return "{}";
		}
		return lineResp.toString();
	}

}
