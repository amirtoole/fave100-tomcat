package com.caseware.fave100;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hashids.Hashids;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;

public class Index {

	public static void main(final String[] args) {
		final long startTime = new Date().getTime();		
		final File file = new File("C:\\Users\\yissachar.radcliffe\\dev\\EclipseWorkspace\\fave100-tomcat\\src\\main\\resources\\lucene-index");
						
		// Delete old index
		deleteIndex(file);	
						
		// Connect to Postgres database and build new index
		Connection connection = null;
		long sqlTime = 0;
		try {
			System.out.println("Connecting to SQL...");		
			final long sqlStartTime = new Date().getTime();	
			connection = getSqlConnection();
			
			final String statement = "SELECT * FROM autocomplete_search;";
			final Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(100000);
			connection.setAutoCommit(false);
			
			final ResultSet results = stmt.executeQuery(statement);
			sqlTime = new Date().getTime();
			System.out.println("Time to execute SQL: " + (sqlTime - sqlStartTime));
			
			buildIndex(file, results);
		}
		catch (final SQLException e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (final SQLException e) {
					e.printStackTrace();
				}
			}			
		}
		
		final long endTime = new Date().getTime();
		System.out.println("Time to build index: " + (endTime - sqlTime));
		printTotalTime(startTime, endTime);

	}
	
	/**
	 * Deletes all files in the directory
	 * @param file The directory containing the index files
	 */
	private static void deleteIndex(File file) {
		final long startTime = new Date().getTime();	
		
		final String[] myFiles = file.list();
		for (int i = 0; i < myFiles.length; i++) {
			final File subFile = new File(file, myFiles[i]);
			subFile.delete();
		}
		
		final long deleteTime = new Date().getTime();		
		System.out.println("Time to delete old index: " + (deleteTime - startTime));
	}
	
	private static Connection getSqlConnection() throws SQLException {
		final String url = "jdbc:postgresql://192.168.214.177/musicbrainz";
		final String user = "musicbrainz";
		final String password = "";
		return DriverManager.getConnection(url, user, password);
	}
	
	private static void buildIndex(File file, ResultSet results) throws SQLException {
		System.out.println("Building index...");
		int count = 0;
		
		try {
			final MusicbrainzAnalyzer analyzer = new MusicbrainzAnalyzer();
			final IndexWriterConfig config = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
			config.setMergePolicy(new LogDocMergePolicy());			
			final IndexWriter w = new IndexWriter(FSDirectory.open(file), config);
			
			// Create fields once only, for index build performance				
			final Field idField = new Field("id", "", idType());
			final StoredField songField = new StoredField("song", "");
			final StoredField artistField = new StoredField("artist", "");
			final Field searchField = new Field("searchable_song_artist", "", indexType());

			Hashids hashids = new Hashids("fave100salt");
			while (results.next()) {
				count++;
				
				idField.setStringValue(hashids.encrypt(results.getInt("id")));
				songField.setStringValue(results.getString("song"));
				artistField.setStringValue(results.getString("artist"));
				searchField.setStringValue(getNgrams(results.getString("searchable_song") + " " + results.getString("searchable_artist")));					
				searchField.setBoost(results.getInt("rank"));

				w.addDocument(buildDocument(idField, songField, artistField, searchField));

			}
			// Merge all indexes into one index
			// In general this is not advised since it adds time to building the index, 
			// but since we don't care that much about index build time (since it takes place offline)
			// we do this for the small performance we gain of not having to read multiple indexes
			w.forceMerge(1);
			w.close();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Index built: " + count + " entries");
	}
	
	/**
	 * Builds a string consisting of all ngrams of the input terms of size 2 or more.
	 * For example, the term "cars song" would become "ca car cars so son song"
	 * @param terms
	 * @return
	 */
	private static String getNgrams(String terms) {
		final StringBuilder ngramsBuilder = new StringBuilder();
		final String[] words = LuceneIndex.splitTerms(terms);
		
		for (final String word : words) {
			for (int i = 2; i < word.length(); i++) {
				ngramsBuilder.append(word.substring(0, i));
				ngramsBuilder.append(" ");
			}
			ngramsBuilder.append(word);
			ngramsBuilder.append(" ");
		}

		return ngramsBuilder.toString();
	}

	private static FieldType idType() {
		final FieldType idType = new FieldType();
		idType.setIndexed(true);
		idType.setStored(true);
		idType.setTokenized(false);
		return idType;
	}

	private static FieldType indexType() {
		final FieldType indexType = new FieldType();
		indexType.setIndexed(true);
		indexType.setStored(false);
		indexType.setTokenized(true);
		return indexType;
	}
	
	private static Document buildDocument(Field idField, StoredField songField, StoredField artistField, Field searchField) {
		final Document document = new Document();
		document.add(idField);
		document.add(songField);
		document.add(artistField);
		document.add(searchField);
		return document;
	}
	
	/**
	 * Prints the formatted time duration, e.g: 9m 37s 151ms
	 * @param startTime
	 * @param endTime
	 */
	private static void printTotalTime(long startTime, long endTime) {		
		final int totalMilli = (int)(endTime - startTime);
		final int minutes = totalMilli / 1000 / 60;
		final int seconds = (totalMilli / 1000) % 60;
		final int milli = totalMilli % 1000;
		System.out.println("Total time: " + minutes + "m " + seconds + "s " + milli + "ms ");
	}

}
