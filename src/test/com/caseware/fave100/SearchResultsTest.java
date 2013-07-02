package com.caseware.fave100;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearchResultsTest {

	@Test
	public void correctSearchResults() {
		// Don't search for single letter terms
		assertEquals(0, numResults(makeRequest("a")));
		assertEquals(0, numResults(makeRequest("?")));
		// Basic
		assertEquals(true, inResults(makeRequest("stairway"), "Stairway to Heaven", "Led Zeppelin"));
		assertEquals(true, inResults(makeRequest("led zep"), "Stairway to Heaven", "Led Zeppelin"));
		assertEquals(true, inResults(makeRequest("pangea kliq"), "Pangea", "Professor Kliq"));
		assertEquals(true, inResults(makeRequest("kliq pangea"), "Pangea", "Professor Kliq"));
		assertEquals(true, inResults(makeRequest("hard button to"), "The Hardest Button to Button", "The White Stripes"));
		// Ignore case
		assertEquals(makeRequest("PanGeA KLIQ"), makeRequest("pangea kliq"));
		// Ignore accents
		assertEquals(true, inResults(makeRequest("sebastien tellier"), "La Ritournelle", "Sébastien Tellier"));
		assertEquals(true, inResults(makeRequest("uber legitimate"), "Über Legitimate", "Mates of State"));
		// Ignore apostrophe in word
		assertEquals(makeRequest("cant get enough"), makeRequest("can't get enough"));
		// But don't ignore apostrophe alone
		assertNotEquals(0, numResults(makeRequest("''")));
		// Ignore non-alphanumeric at beginning or end of word
		assertEquals(true, inResults(makeRequest("depeche schizo"), "Just Can't Get Enough (Schizo mix)", "Depeche Mode"));
		// But leave non-alphanumeric in middle of word untouched
		assertEquals(true, inResults(makeRequest("ke$ha"), "Animal", "Ke$ha"));
		// Escaped strings
		assertEquals(true, inResults(makeRequest("brain stew / jaded"), "Brain Stew / Jaded", "Green Day"));
		// Special case Musicbrainz Test Artist
		assertEquals(0, numResults(makeRequest("Musicbrainz Test Artist")));
	}

	private String makeRequest(final String searchTerm) {
		URL url;
		HttpURLConnection connection = null;
		String urlParameters = "";
		try {
			urlParameters = URLEncoder.encode(searchTerm, "UTF-8");
		}
		catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			//Create connection
			url = new URL("http://localhost:8080/fave100/search?callback=callback&searchTerm=" + urlParameters);
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

	private int numResults(final String jsonp) {
		final String json = jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
		final JsonParser parser = new JsonParser();
		final JsonObject jsonObject = parser.parse(json).getAsJsonObject();
		return jsonObject.get("total").getAsInt();
	}

	private boolean inResults(final String jsonp, final String song, final String artist) {
		final String json = jsonp.substring(jsonp.indexOf("(") + 1, jsonp.lastIndexOf(")"));
		final JsonParser parser = new JsonParser();
		final JsonObject jsonObject = parser.parse(json).getAsJsonObject();
		final JsonArray jsonArray = jsonObject.get("results").getAsJsonArray();
		for (final JsonElement elem : jsonArray) {
			final JsonObject songObj = elem.getAsJsonObject();
			if (songObj.get("song").getAsString().equals(song) && songObj.get("artist").getAsString().equals(artist))
				return true;
		}
		return false;
	}

}
