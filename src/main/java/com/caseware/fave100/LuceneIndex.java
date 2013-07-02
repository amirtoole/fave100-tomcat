package com.caseware.fave100;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;

public class LuceneIndex {
	public static MusicbrainzAnalyzer ANALYZER;
	public static IndexSearcher SEARCHER;

	static {
		ANALYZER = new MusicbrainzAnalyzer();

		final File file = new File(Thread.currentThread().getContextClassLoader().getResource("lucene-index").getPath());

		try {
			final Directory index = FSDirectory.open(file);
			final IndexReader reader = DirectoryReader.open(index);
			SEARCHER = new IndexSearcher(reader);
		}
		catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

}
