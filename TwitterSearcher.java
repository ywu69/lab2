import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.OAuthService;
import org.scribe.builder.api.*;
import org.scribe.model.*;

public class TwitterSearcher {

	String TWITTER_URL;
	String TWITTER_API_KEY;
	String TWITTER_API_SECRET;
	String TWITTER_TOKEN;
	String TWITTER_TOKEN_SECRET;
	Token accessToken;

	public TwitterSearcher(String twitter_url, String api_key,
			String api_secret, String token, String token_secret) {
		this.accessToken = new Token(token, token_secret);
		this.TWITTER_API_KEY = api_key;
		this.TWITTER_API_SECRET = api_secret;
		this.TWITTER_URL = twitter_url;
	}

	public String searchInTwitter() {

		OAuthService service = new ServiceBuilder().provider(TwitterApi.class)
				.apiKey(TWITTER_API_KEY).apiSecret(TWITTER_API_SECRET).build();

		OAuthRequest request = new OAuthRequest(Verb.GET, TWITTER_URL);
		service.signRequest(accessToken, request);
		Response response = request.send();

		return response.getBody();
	}
}