

import java.util.ArrayList;
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
public class ConnLineController {

	public static final String CHANNEL_SECRET = "YOUR_CHANNEL_SECRET";
	public static final String ACCESS_TOKEN = "YOUR_ACCESS_TOKEN";
	public static final HashMap<String, Integer> CITY = CityConfig.getInstanse().getProperty();
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/conn-line", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "application/json")
	@ResponseBody
	public String responseRequest(Model model, HttpServletRequest request, HttpServletResponse response) {
		String json;
		JSONObject lineResp = new JSONObject();
		JSONArray messages = new JSONArray();
		try {
			json = IOUtils.toString(request.getInputStream(), "UTF-8");
			if (json.isEmpty()) {
				throw new Exception("No request data");
			}
			JSONObject jsonData = new JSONObject(json);
			ArrayList<Map<String, Object>> events = (ArrayList<Map<String, Object>>) jsonData.opt("events");
			if (events == null) {
				throw new Exception("No events data");
			}

			for (Map<String, Object> event : events) {
				Map<String, Object> sourceData = (Map<String, Object>) event.get("source");
				if (sourceData == null) {
					System.out.println("No sourceData data");
					continue;
				}
				String typeData = event.containsKey("type") ? (String) event.get("type") : null;
				Map<String, Object> eventData = null;
				if (typeData != null && event.containsKey(typeData)) {
					eventData = (Map<String, Object>) event.get(typeData);
				}

				String replyToken = event.containsKey("replyToken") ? (String) event.get("replyToken") : null;
				String lineUUID = sourceData.containsKey("userId") ? (String) sourceData.get("userId") : null;
				JSONObject userProfile = null;

				if (lineUUID != null)
					userProfile = LineActions.lineUserProfile(ACCESS_TOKEN, lineUUID);

				String signature = request.getHeader("X-Line-Signature");
				if (signature == null || signature.length() == 0) {
					throw new Exception("No X-Line-Signature data");
				}

				if (!LineActions.verifySignature(signature, json, CHANNEL_SECRET)) {
					throw new Exception("X-Line-Signature data not match");
				}

				if (typeData == null || eventData == null) {
					throw new Exception("No typeData/eventData data");
				}

				String fromText = null;
				if (userProfile != null)
					fromText = "Hi, " + userProfile.optString("displayName") + ", ";

				switch (typeData) {
				case "message":
					if (eventData.containsKey("type")) {
						switch (eventData.get("type").toString()) {
						case "text":
							String userInput = eventData.get("text").toString();
							fromText += "我已收到您的訊息「" + userInput + "」";
							userInput = userInput.toLowerCase().trim();
							if(CITY.containsKey(userInput)){
								JSONObject weather = MessengerActions.weatherInfo((CITY.get(userInput)));
								//fromText += "\r\nTemperature : " + weather.getJSONObject("main").get("temp");
								//fromText += "°C\r\nDescription : " + weather.getJSONArray("weather").getJSONObject(0).get("description");
								System.out.println(weather);
								messages.add(0, LineActions.weatherInfoToFlexMessage(weather));
							} else {
								fromText += "\r\n不好意思沒找到相關城市訊息";
							}
							
							break;
						case "sticker":
							fromText += "我已收到您的貼圖";
							break;
						}
						fromText += "\r\n\r\n我是天氣機器人, 請輸入一個城市，如Taipei";
						fromText += "\r\n(I am a WeatherBot, please input city name, ex.New York)";
						break;
					}
				}

				if (!fromText.isEmpty())
					messages.add(0, LineActions.textToReplyMessage(fromText));
				//Radom sticker in packageId 2, 501~527
				//messages.add(1, LineActions.stickerToReplyMessage());

				if (messages.size() > 0) {
					LineActions.replyTokenReply(ACCESS_TOKEN, replyToken, messages);
				}

			}
		} catch (Exception e) {
			System.out.println("ERROR MSG : " + e.getMessage());
			return "{}";
		}
		return lineResp.toString();
	}

}
