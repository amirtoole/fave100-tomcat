package com.caseware.fave100;

import static org.junit.Assert.assertEquals;

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
	public void search_service_should_not_search_for_single_letter_terms() {
		assertEquals(0, numResults(makeRequest("a")));
	}
	
	@Test
	public void search_service_should_not_search_for_single_letter_terms_even_if_special_char() {
		assertEquals(0, numResults(makeRequest("?")));
	}
	
	@Test
	public void search_service_should_find_existing_songs() {
		assertEquals(true, inResults(makeRequest("stairway"), "Stairway to Heaven", "Led Zeppelin"));
		assertEquals(true, inResults(makeRequest("led zep"), "Stairway to Heaven", "Led Zeppelin"));
		assertEquals(true, inResults(makeRequest("pangea kliq"), "Pangea", "Professor Kliq"));
		assertEquals(true, inResults(makeRequest("kliq pangea"), "Pangea", "Professor Kliq"));
		assertEquals(true, inResults(makeRequest("hard button to"), "The Hardest Button to Button", "The White Stripes"));
	}

	@Test
	public void search_service_should_not_find_non_existing_songs() {
		assertEquals(0, numResults(makeRequest("zxzxzxzxzxzxzxzxzxzxzxzxzxzxzx")));
	}
	
	@Test
	public void search_service_should_find_existing_songs_with_number_search() {
		assertEquals(true, inResults(makeRequest("1979"), "1979", "The Smashing Pumpkins"));
	}
	
	@Test
	public void search_service_should_find_existing_songs_regardless_of_letter_case() {
		assertEquals(makeRequest("PanGeA KLIQ"), makeRequest("pangea kliq"));
	}
	
	@Test
	public void search_service_should_ignore_accents_in_search() {
		assertEquals(true, inResults(makeRequest("sebastien tellier"), "La Ritournelle", "Sébastien Tellier"));
		assertEquals(true, inResults(makeRequest("uber legitimate"), "Über Legitimate", "Mates of State"));
	}
	
	@Test
	public void search_service_should_ignore_periods_in_search() {
		assertEquals(true, inResults(makeRequest("war resolution"), "W.A.R. // Resolution", "Lzn02"));
		assertEquals(true, inResults(makeRequest("W.A.R. resolution"), "W.A.R. // Resolution", "Lzn02"));
	}
	
	@Test
	public void search_service_should_ignore_apostrophe_in_search() {
		assertEquals(makeRequest("cant get enough"), makeRequest("can't get enough"));

	}
	
	@Test
	public void search_service_should_ignore_non_alphanumeric_characters_in_search() {
		assertEquals(true, inResults(makeRequest("depeche schizo"), "Just Can't Get Enough (Schizo mix)", "Depeche Mode"));
		assertEquals(true, inResults(makeRequest("blink-182"), "All the Small Things", "blink‐182"));
		assertEquals(true, inResults(makeRequest("blink 182"), "All the Small Things", "blink‐182"));
	}
	
	@Test
	public void search_service_should_find_ke$ha_from_kesha() {
		assertEquals(true, inResults(makeRequest("kesha"), "TiK ToK", "Ke$ha"));
	}
	
	@Test
	public void search_service_should_find_pnk_from_pink() {
		assertEquals(true, inResults(makeRequest("pink glass"), "Raise Your Glass", "P!nk"));
	}
	
	@Test
	public void search_service_should_find_even_with_escaped_strings() {
		assertEquals(true, inResults(makeRequest("brain stew / jaded"), "Brain Stew / Jaded", "Green Day"));
	}	

	@Test
	public void search_service_should_not_find_musicbrainz_test_artist() {
		assertEquals(0, numResults(makeRequest("Musicbrainz Test Artist")));
	}
	
	@Test
	public void search_service_should_treat_numbers_and_words_as_synonyms() {

		assertEquals(makeRequest("4th of july"), makeRequest("fourth of july"));
		assertEquals(makeRequest("third time"), makeRequest("3rd time"));
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
