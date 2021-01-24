package com.caseware.fave100;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class LuceneIndex {

	private static final Logger logger = LoggerFactory.getLogger(LuceneIndex.class);

	public static MusicbrainzAnalyzer SEARCH_ANALYZER;
	public static WhitespaceAnalyzer LOOKUP_ANALYZER;
	public static IndexSearcher SEARCHER;

	static {
		logger.info("starting lucene index");
		SEARCH_ANALYZER = new MusicbrainzAnalyzer();
		LOOKUP_ANALYZER = new WhitespaceAnalyzer(LuceneVersion.LUCENE_VERSION);

		S3BucketDownload s3BucketDownload = new S3BucketDownload(); // Comment this out when building index locally and replace file below
		final File file = new File(S3BucketDownload.FOLDER_PATH);

		try {
			final Directory index = FSDirectory.open(file);
			final IndexReader reader = DirectoryReader.open(index);
			SEARCHER = new IndexSearcher(reader);
		}
		catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	// Removes most non-alphnumeric Unicode characters, and splits on the remaining
	public static String[] splitTerms(final String terms) {
		// Remove control characters, symbols, and most punctuation 
		final String removed = terms.replaceAll("[\\p{C}\\p{S}\\p{Ps}\\p{Pe}\\p{Pi}\\p{Pf}]+", "");
		// Split on separators, hypen or dash, and underscore		
		final String[] results = removed.split("[\\p{Z}\\p{S}\\p{Pd}\\p{Pc}]+");
		return results;
	}
}
