package com.caseware.fave100;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

@Path("/search")
public class SearchService {

	@GET
	@Produces("text/plain")
	public String getClichedMessage(@QueryParam("callback") final String callback, @QueryParam("searchTerm") final String searchTerm, @QueryParam("limit") int limit, @QueryParam("page") final int page, @QueryParam("allWild") final boolean allWild) {
		if (callback == null || callback.isEmpty()) {
			return "Callback required";
		}

		if (limit == 0)
			limit = 5;
		else
			limit = Math.min(25, limit);

		try {
			final long startTime = System.currentTimeMillis();
			final BooleanQuery q = new BooleanQuery();
			final String[] searchTerms = searchTerm.split(" ");
			// Add all search terms to boolean query
			for (int i = 0; i < searchTerms.length; i++) {
				// Don't add terms that are only 1 letter - they make for bad query results
				String searchString = searchTerms[i];
				if (searchString.length() > 1) {
					if(allWild) {
						// Add a wildcard to end of each search term
						searchString += "*";
					} else if (i == searchTerms.length - 1) {
						// Otherwise just add to the last search term
						searchString += "*";
					}
					final QueryParser parser = new QueryParser(Version.LUCENE_41, "searchable_song_artist", LuceneIndex.ANALYZER);
					// Special case for one word search to prevent wildcard messing up scoring
					// Search terms of length 3 or less can break the search engine with SCORING_BOOLEAN_QUERY_REWRITE set
					if(searchTerms.length == 1 && searchTerms[0].length() > 3) {
						parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
					}
					parser.setDefaultOperator(QueryParser.AND_OPERATOR);
					final Query query = parser.parse(searchString);
					q.add(query, Occur.MUST);
				}
			}

			final IndexReader reader = DirectoryReader.open(LuceneIndex.INDEX);
			final IndexSearcher searcher = new IndexSearcher(reader);

			final int searchLimit = (page + 1) * limit;
			final TopDocs results = searcher.search(q, searchLimit, new Sort(SortField.FIELD_SCORE));

			final long endTime = System.currentTimeMillis();
			final long executionTime = endTime - startTime;
			System.out.println("Found " + results.totalHits + " hits in " + executionTime + " ms");

			final ScoreDoc[] hits = results.scoreDocs;
			final int offset = page * limit;
			final int count = Math.min(results.totalHits - offset, limit);

			// No results found and we haven't tried all allWild yet, try allWild
			if(count == 0 && allWild == false) {
				return getClichedMessage(callback, searchTerm, limit, page, true);
			}

			//			// Check if page number is invalid
			//			if (results.totalHits + limit < page * limit)
			//				return;

			final StringBuilder sb = new StringBuilder();
			sb.append(callback);
			sb.append("({\"results\":[");

			for (int i = 0; i < count; ++i) {
				if (i > 0)
					sb.append(",");
				final int docId = hits[i + offset].doc;
				final Document d = searcher.doc(docId);
				sb.append("{\"id\":\"" + d.get("id") + "\"");
				sb.append(",\"song\":\"" + d.get("song").replaceAll("\"", "\\\"") + "\"");
				sb.append(",\"artist\":\"" + d.get("artist").replaceAll("\"", "\\\"") + "\"");
				sb.append("}");
				//				sb.append((i + 1) + ". " + d.get("song") + "\n\t\t" + d.get("artist") + "\n");
			}

			sb.append("],\"total\":" + results.totalHits);
			sb.append("});");

			return sb.toString();

		}
		catch (final ParseException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		catch (final IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}

}
