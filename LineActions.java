

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;

public class LineActions {

	public static final String HASH_ALGORITHM = "HmacSHA256";

	@SuppressWarnings("deprecation")
	public static void replyTokenReply(String accessToken, String replyToken, JSONArray messages) {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		try {
			JSONObject json = new JSONObject();
			json.put("replyToken", replyToken);
			json.put("messages", messages);

			HttpPost request = new HttpPost("https://api.line.me/v2/bot/message/reply");
			StringEntity params = new StringEntity(json.toString());
			request.addHeader("content-type", "application/json");
			request.addHeader("Authorization", "Bearer " + accessToken);
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
			}
		} catch (Exception ex) {
			// handle exception here
			System.out.println(ex.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	@SuppressWarnings("deprecation")
	public static JSONObject lineUserProfile(String accessToken, String uid) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpGet request = new HttpGet("https://api.line.me/v2/bot/profile/" + uid);
			request.addHeader("content-type", "application/json");
			request.addHeader("Authorization", "Bearer " + accessToken);
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

				System.out.println("Line User Profile:" + resultData.toString());

				return new JSONObject(resultData.toString());
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	public static JSONObject textToReplyMessage(String msg) {
		JSONObject msgObj = new JSONObject();
		try {
			msgObj.put("type", "text");
			msgObj.put("text", msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgObj;
	}

	public static JSONObject stickerToReplyMessage() {
		JSONObject msgObj = new JSONObject();
		try {
			msgObj.put("type", "sticker");
			msgObj.put("packageId", "2");
			msgObj.put("stickerId", (new Random().nextInt(27) + 501) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgObj;
	}

	public static boolean verifySignature(String signature, String body, String channelSecret) throws Exception {
		try {
			SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), HASH_ALGORITHM);
			Mac mac = Mac.getInstance(HASH_ALGORITHM);
			mac.init(key);
			byte[] source = body.getBytes(StandardCharsets.UTF_8);
			String createdSignature = Base64.encodeBase64String(mac.doFinal(source));
			return createdSignature.equals(signature);
		} catch (Exception e) {
			return false;
		}
	}
}
