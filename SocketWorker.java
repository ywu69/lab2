import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SocketWorker implements Runnable {

	protected Socket clientSocket = null;
	private String YELP_API_KEY = null;
	private String YELP_API_SECRET = null;
	private String YELP_TOKEN = null;
	private String YELP_TOKEN_SECRET = null;
	private String TWITTER_API_KEY = null;
	private String TWITTER_API_SECRET = null;
	private String TWITTER_TOKEN = null;
	private String TWITTER_TOKEN_SECRET = null;
	private int limit;

	public SocketWorker(Socket clientSocket, String TWITTER_API_KEY,
			String TWITTER_API_SECRET, String TWITTER_TOKEN,
			String TWITTER_TOKEN_SECRET, String YELP_API_KEY,
			String YELP_API_SECRET, String YELP_TOKEN,
			String YELP_TOKEN_SECRET, int limit) {
		this.clientSocket = clientSocket;
		this.YELP_API_KEY = YELP_API_KEY;
		this.YELP_API_SECRET = YELP_API_SECRET;
		this.YELP_TOKEN = YELP_TOKEN;
		this.YELP_TOKEN_SECRET = YELP_TOKEN_SECRET;
		this.TWITTER_API_KEY = TWITTER_API_KEY;
		this.TWITTER_API_SECRET = TWITTER_API_SECRET;
		this.TWITTER_TOKEN = TWITTER_TOKEN;
		this.TWITTER_TOKEN_SECRET = TWITTER_TOKEN_SECRET;
		this.limit = limit;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			String line;
			while (!(line = br.readLine().trim()).equals("")) {

				if (line.startsWith("GET")) {

					String[] requestline = line.split(" ");
					//System.out.println("URI: " + requestline[1]);
					if (requestline[1].startsWith("/restaurantmashup?city=")) {

						String[] request = requestline[1].split("=");
						String cityName = request[1].replace("%20", " ")
								.replace("+", " ");
						//System.out.println(cityName + "\n");

						// search restaurants in yelp
						YelpSearcher ys = new YelpSearcher(YELP_API_KEY,
								YELP_API_SECRET, YELP_TOKEN, YELP_TOKEN_SECRET,
								limit);
						String yelpSearchResult = ys
								.searchForBusinessesByLocation("restaurants",
										cityName);

						// extract restaurants names and ratings from yelp's search result
						JsonParser yelp_JsonParser = new JsonParser(
								yelpSearchResult);
						JSONObject yelp_response = yelp_JsonParser
								.parserBegin();
						JSONArray restaurants = (JSONArray) yelp_response
								.get("businesses");

						// build JSON file
						JSONObject result_Object = new JSONObject();
						result_Object.put("city", cityName);
						JSONArray restaurant_array = new JSONArray();

						for (int i = 0; i < restaurants.size(); i++) {
							JSONObject restaurant = (JSONObject) restaurants
									.get(i);
							String nameBeforeFormat = restaurant.get("name")
									.toString();
							String each_rating = restaurant.get("rating")
									.toString();
							
							// get twitter reviews
							String restaurantName = nameBeforeFormat.replace(
									" ", "%20");
							String city_nameString = cityName.replace(" ",
									"%20");
							StringBuffer twitterUrl = new StringBuffer(
									"https://api.twitter.com/1.1/search/tweets.json?q=");
							twitterUrl.insert(49, restaurantName + "%20"
									+ city_nameString);
							TwitterSearcher ts = new TwitterSearcher(
									twitterUrl.toString(), TWITTER_API_KEY,
									TWITTER_API_SECRET, TWITTER_TOKEN,
									TWITTER_TOKEN_SECRET);
							String twitterContent = ts.searchInTwitter();

							// extract twitter reviews
							JSONArray twitterReviewArray = new JSONArray();

							// extract user's text from JSON
							JsonParser twitter_JsonParser = new JsonParser(
									twitterContent);
							JSONObject twitter_response = twitter_JsonParser
									.parserBegin();
							JSONArray userArray = null;
							userArray = (JSONArray) twitter_response
									.get("statuses");
							if (userArray == null || userArray.size() == 0) {
								// pw.println("  There is no twitter about this restaurant.");
							} else {
								for (int k = 0; k < userArray.size(); k++) {
									JSONObject userStatus = (JSONObject) userArray
											.get(k);
									String userText = (String) userStatus
											.get("text");
									String finalTextString = userText.replace(
											"\n", " ");
									twitterReviewArray.add(finalTextString);
								}
							}

							JSONObject each_restaurant = new JSONObject();
							each_restaurant.put("name", nameBeforeFormat);
							each_restaurant.put("rating", each_rating);
							each_restaurant.put("tweets", twitterReviewArray);

							restaurant_array.add(each_restaurant);
						}
						result_Object.put("restaurants", restaurant_array);

						String responsebody = result_Object.toJSONString();

						String responseheaders = "HTTP/1.1 200 OK\n" +
								"Content-Length: " + responsebody.getBytes().length + "\n\n";

						OutputStream out = clientSocket.getOutputStream();
						out.write(responseheaders.getBytes());
						out.write(responsebody.getBytes());
						out.write("\n".getBytes());
						out.flush();
						out.close();
					} else if (!requestline[1]
							.startsWith("/restaurantmashup?city=")) {
						
						String responsebody = "<html><body>It is a Bad Request!</body></html>";
						String responseheaders = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responsebody.getBytes().length + "\n\n";
						OutputStream out = clientSocket.getOutputStream();
						out.write(responseheaders.getBytes());
						out.write(responsebody.getBytes());
						out.flush();
						out.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}