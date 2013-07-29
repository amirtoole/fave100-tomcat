package com.caseware.fave100;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LookupTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void lookup() {
		// Lookup running and returning historic results
		assertEquals(true, inResults(lookupSong("547816"), "Pangea", "Professor Kliq"));
		assertEquals(true, inResults(lookupSong("1822565"), "Stairway to Heaven", "Led Zeppelin"));
	}

	private String lookupSong(final String id) {
		URL url;
		HttpURLConnection connection = null;
		String urlParameters = "";
		try {
			urlParameters = URLEncoder.encode(id, "UTF-8");
		}
		catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			//Create connection
			url = new URL("http://localhost:8080/fave100/lookup?id=" + urlParameters);
			connection = (HttpURLConnection)url.openConnection();

			//Get Response	
			final InputStream is = connection.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			final StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();

		}
		catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private boolean inResults(final String json, final String song, final String artist) {
		final JsonParser parser = new JsonParser();
		final JsonObject songObj = parser.parse(json).getAsJsonObject();
		if (songObj.get("song").getAsString().equals(song) && songObj.get("artist").getAsString().equals(artist))
			return true;
		return false;
	}

}
