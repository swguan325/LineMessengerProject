

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wink.json4j.JSONObject;

public class MessengerActions {

	public static final String API_VERSION = "v2.6";
	public static final String MESSENGER_SEND_URL = "https://graph.facebook.com/" + API_VERSION
			+ "/me/messages?access_token=";
	public static final String MESSENGER_GET_URL = "https://graph.facebook.com/" + API_VERSION + "/";
	public static final String MESSENGER_PASS_URL = "https://graph.facebook.com/" + API_VERSION
			+ "/me/pass_thread_control?access_token=";
	public static final String MESSENGER_PASSTOPRIMARY_URL = "https://graph.facebook.com/" + API_VERSION
			+ "/me/take_thread_control?access_token=";

	public static final String APPID = "YOUR_APPID";

	@SuppressWarnings("deprecation")
	public static JSONObject pushToMessenger(String pageToken, String userId, JSONObject message) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		JSONObject result = null;
		try {
			JSONObject json = new JSONObject();

			JSONObject recipient = new JSONObject();
			recipient.put("id", userId);

			json.put("recipient", recipient);
			json.put("message", message);

			HttpPost request = new HttpPost(MESSENGER_SEND_URL + pageToken);
			result = postRequest(httpClient, request, json);
		} catch (Exception ex) {
			// handle exception here
			System.out.println(ex.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	public static JSONObject postRequest(CloseableHttpClient httpClient, HttpPost request, JSONObject json)
			throws Exception {
		StringEntity params = new StringEntity(json.toString());
		request.addHeader("content-type", "application/json");
		request.setEntity(params);
		HttpResponse response = httpClient.execute(request);

		if (response != null) {
			System.out.println("StatusCode : " + response.getStatusLine().getStatusCode());

			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String inputLine;
			StringBuffer resultData = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				resultData.append(inputLine);
			}
			in.close();

			System.out.println(resultData.toString());
			JSONObject result = new JSONObject(resultData.toString());
			return result;
		}

		return null;
	}

	public static JSONObject getRequest(CloseableHttpClient httpClient, HttpGet request) throws Exception {
		HttpResponse response = httpClient.execute(request);

		if (response != null) {
			System.out.println("StatusCode : " + response.getStatusLine().getStatusCode());

			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String inputLine;
			StringBuffer resultData = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				resultData.append(inputLine);
			}
			in.close();

			JSONObject result = new JSONObject(resultData.toString());

			return result;
		}
		return null;
	}

	public static JSONObject textToReplyMessage(String text) {
		JSONObject message = new JSONObject();
		try {
			message.put("text", text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	public static JSONObject imageToReplyMessage(String url) {
		JSONObject message = new JSONObject();
		try {
			JSONObject attachment = new JSONObject();
			attachment.put("type", "image");
			attachment.put("payload", new JSONObject().put("url", "https://openweathermap.org/img/w/" + url + ".png")
					.put("is_reusable", false));
			message.put("attachment", attachment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	@SuppressWarnings("deprecation")
	public static JSONObject messengerUserProfile(String pageToken, String userId) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		JSONObject result = null;
		try {
			HttpGet request = new HttpGet(MESSENGER_GET_URL + userId + "?access_token=" + pageToken);
			result = getRequest(httpClient, request);
		} catch (Exception ex) {
			// handle exception here
			System.out.println(ex.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public static JSONObject weatherInfo(Integer cityId) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		JSONObject result = null;
		try {
			HttpGet request = new HttpGet("https://api.openweathermap.org/data/2.5/forecast?id=" + cityId + "&APPID="
					+ APPID + "&units=metric");
			result = getRequest(httpClient, request);
			if (result.getString("cod").equals("200"))
				result = result.getJSONArray("list").getJSONObject(0);
			else {
				return null;
			}
		} catch (Exception ex) {
			// handle exception here
			System.out.println(ex.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

}
