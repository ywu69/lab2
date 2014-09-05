import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpServer implements Runnable {

	private static final String filePath = "keys.json";

	private static String YELP_API_KEY = null;
	private static String YELP_API_SECRET = null;
	private static String YELP_TOKEN = null;
	private static String YELP_TOKEN_SECRET = null;
	private static String TWITTER_API_KEY = null;
	private static String TWITTER_API_SECRET = null;
	private static String TWITTER_TOKEN = null;
	private static String TWITTER_TOKEN_SECRET = null;
	private static int limit = 11;

	protected ServerSocket serverSocket = null;
	protected Thread currentThread = null;
	protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
	protected int serverPort;

	public String JSONExtract(String key) {
		String value = null;
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(filePath));
			JSONObject jsonObject = (JSONObject) obj;

			value = (String) jsonObject.get(key);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return value;
	}

	public HttpServer(int port) {
		this.serverPort = port;
	}

	public void run() {
		synchronized (this) {
			this.currentThread = Thread.currentThread();
		}
		createServerSocket();
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				throw new RuntimeException("Error accepting client connection",
						e);
			}
			this.threadPool.execute(new SocketWorker(clientSocket,
					TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_TOKEN,
					TWITTER_TOKEN_SECRET, YELP_API_KEY, YELP_API_SECRET,
					YELP_TOKEN, YELP_TOKEN_SECRET, limit));
		}
		//this.threadPool.shutdown();
	}

	private void createServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port", e);
		}
	}

	public static void main(String[] args) {

	    int PORT = Integer.parseInt(args[0]);

		HttpServer hs = new HttpServer(PORT);

		YELP_API_KEY = hs.JSONExtract("yelpconsumerkey");
		YELP_API_SECRET = hs.JSONExtract("yelpconsumersecret");
		YELP_TOKEN = hs.JSONExtract("yelptoken");
		YELP_TOKEN_SECRET = hs.JSONExtract("yelptokensecret");
		TWITTER_API_KEY = hs.JSONExtract("twitterapikey");
		TWITTER_API_SECRET = hs.JSONExtract("twitterapisecret");
		TWITTER_TOKEN = hs.JSONExtract("twittertoken");
		TWITTER_TOKEN_SECRET = hs.JSONExtract("twittertokensecret");

		new Thread(hs).start();

		try {
			Thread.sleep(40 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}