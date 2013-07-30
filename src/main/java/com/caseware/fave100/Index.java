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
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;

public class Index {

	public static void main(final String[] args) {
		final long startTime = new Date().getTime();
		// Analyzer with no stopwords
		final File file = new File("/path/to/file");
		final MusicbrainzAnalyzer analyzer = new MusicbrainzAnalyzer();
		// Use a separate analyzer for id so that integer ids are not converted to word synonyms: 3456 -> threefourfivesix
		final Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		analyzerPerField.put("id", LuceneIndex.LOOKUP_ANALYZER);
		final PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerPerField);
		// Delete all old indexes
		final String[] myFiles = file.list();
		for (int i = 0; i < myFiles.length; i++) {
			final File subFile = new File(file, myFiles[i]);
			subFile.delete();
		}
		final long deleteTime = new Date().getTime();
		System.out.println("Time to delete old index: " + (deleteTime - startTime));
		Directory index = null;
		try {
			index = FSDirectory.open(file);
		}
		catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		final LogDocMergePolicy mergePolicy = new LogDocMergePolicy();
		final IndexWriterConfig config = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
		config.setMergePolicy(mergePolicy);

		int count = 0;
		Connection connection = null;
		long sqlTime = 0;
		try {
			System.out.println("Connecting to SQL...");
			// Make connection
			final String url = "url";
			final String user = "root";
			final String password = "password";

			connection = DriverManager.getConnection(url, user, password);
			final String statement = "SELECT * FROM table;";
			final Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(100000);
			connection.setAutoCommit(false);
			final ResultSet results = stmt.executeQuery(statement);
			sqlTime = new Date().getTime();
			System.out.println("Time to execute SQL: " + (sqlTime - deleteTime));
			System.out.println("Building index...");
			try {
				final IndexWriter w = new IndexWriter(index, config);
				// Create fields once only, to avoid GC				
				final Field idField = new Field("id", "", idType());
				final StoredField songField = new StoredField("song", "");
				final StoredField artistField = new StoredField("artist", "");
				final Field searchField = new Field("searchable_song_artist", "", indexType());

				while (results.next()) {
					count++;

					final Document document = new Document();
					idField.setStringValue(String.valueOf(results.getInt("id")));
					songField.setStringValue(results.getString("song"));
					artistField.setStringValue(results.getString("artist"));

					document.add(idField);
					document.add(songField);
					document.add(artistField);

					final StringBuilder ngramsBuilder = new StringBuilder();
					final String[] words = LuceneIndex.splitTerms(results.getString("searchable_song") + " " + results.getString("searchable_artist"));
					for (final String word : words) {
						for (int i = 2; i < word.length(); i++) {
							ngramsBuilder.append(word.substring(0, i));
							ngramsBuilder.append(" ");
						}
						ngramsBuilder.append(word);
						ngramsBuilder.append(" ");
					}

					searchField.setStringValue(ngramsBuilder.toString());
					searchField.setBoost(results.getInt("rank"));
					document.add(searchField);

					w.addDocument(document);

				}
				w.forceMerge(1);
				w.close();
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
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
			System.out.println("Index built: " + count + " entries");
		}
		final long endTime = new Date().getTime();
		System.out.println("Time to build index: " + (endTime - sqlTime));
		final int totalMilli = (int)(endTime - startTime);
		final int minutes = totalMilli / 1000 / 60;
		final int seconds = (totalMilli / 1000) % 60;
		final int milli = totalMilli % 1000;
		System.out.println("Total time: " + minutes + "m " + seconds + "s " + milli + "ms ");

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

}
