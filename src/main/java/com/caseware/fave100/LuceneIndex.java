package com.caseware.fave100;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndex {
	public static StandardAnalyzer ANALYZER;
	public static Directory INDEX;

	static {
		ANALYZER = new StandardAnalyzer(Version.LUCENE_41, new CharArraySet(Version.LUCENE_41, 0, true));

		final File file = new File(Thread.currentThread().getContextClassLoader().getResource("lucene-index").getPath());

		try {
			INDEX = FSDirectory.open(file);
		}
		catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

}
