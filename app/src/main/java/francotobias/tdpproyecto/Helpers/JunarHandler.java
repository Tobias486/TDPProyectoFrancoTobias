package francotobias.tdpproyecto.Helpers;

import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JunarHandler {
	private static final String JUNAR_URL = "http://api.datos.bahiablanca.gob.ar/api/v2/datastreams/";
	//URL DE DATO PEDIDO
	private static final String STOPS_URL = "PARAD-DE-COLEC/data.csv/?auth_key=";
	private static final String LINES_URL = "LINEA-DE-COLEC/data.csv/?auth_key=";
	private static final String ROUTES_URL = "RECOR-DE-COLEC/data.csv/?auth_key=";
	private static final String API_KEY = "2defe16a55c65963b8212e4f9a558dfa692d443d";
	private static final String LIMIT_URL = "&limit=10000";
	private static String FILTER_URL = "";

// REQUEST: http://api.datos.bahiablanca.gob.ar/api/v2/datastreams/PARAD-DE-COLEC/data.json/?auth_key=YOUR_API_KEY&limit=50

	private static String makeRequest(String DATA_URL) {

		String urlText = JUNAR_URL + DATA_URL + API_KEY + FILTER_URL + LIMIT_URL;

		Log.d("D", urlText);

		//Estas dos lineas son superrequetecontra necesarias
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		URL url;
		HttpURLConnection connection;
		StringBuffer response = new StringBuffer();

		try {
			url = new URL(urlText);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
				response.append("\n"); //Esto se agrega para ayudar a reconocer el formato mas adelante
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response.toString();
	}
	// A partir de aca, están todos los tipos de requests que se pueden hacer
	public static CSVWizard requestStopsGo(String line) {
		return requestStops(line, "i");
	}

	public static CSVWizard requestStopsRet(String line) {
		return requestStops(line, "v");
	}

	private static CSVWizard requestStops(String line, String direction) {
		FILTER_URL = "";

		if (line != null)
			FILTER_URL = "&filter0=column0[==]" + line;
		if (direction != null)
			FILTER_URL += "&filter1=column1[==]" + direction + "&where=(filter0)+and+(filter1)";

		return new CSVWizard(makeRequest(STOPS_URL));
	}

	public static CSVWizard requestRoutes() {
		FILTER_URL = "";
		return new CSVWizard(makeRequest(ROUTES_URL));
	}

	public static CSVWizard requestLines() {
		FILTER_URL = "";
		return new CSVWizard(makeRequest(LINES_URL));
	}

}
