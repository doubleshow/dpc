package org.doubleshow.lucene_web.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.doubleshow.lucene_web.Dpc;
import org.doubleshow.lucene_web.model.DpcDao;
import org.doubleshow.lucene_web.model.DpcDaoException;
import org.doubleshow.lucene_web.model.DpcDoc;
import org.doubleshow.lucene_web.model.DpcSearchOpts;
import org.doubleshow.lucene_web.model.DpcSearchResult;


public class DpcDaoImpl implements DpcDao {
	public static final Version LUCENE_VERSION = Version.LUCENE_31;
	IndexSearcher searcher;
	Analyzer analyzer;
	String index;
	QueryParser parser;
	static final DpcSearchResultImpl EMPTY = new DpcSearchResultImpl(0, 0, Collections.EMPTY_LIST, 0);

	static public class DpcSearchResultImpl implements DpcSearchResult {
		private int totalHits;
		private int start;
		private long time;
		private List<DpcDocImpl> docs;

		public int getTotalHits() {
			return totalHits;
		}

		public int getStart() {
			return start;
		}

		public List<? extends DpcDoc> getDocs() {
			return docs;
		}

		public DpcSearchResultImpl(int totalHits, int start, List<DpcDocImpl> docs, long time) {
			super();
			this.totalHits = totalHits;
			this.start = start;
			this.docs = docs;
			this.time = time;
		}

		public long getSearchTime() {
			return time;
		}

	}
	public DpcDaoImpl(String index) throws DpcDaoException{
		this(index, false);
	}
	public DpcDaoImpl(String index, boolean useStemmer) throws DpcDaoException {
		this.index = index;
		try {
			searcher = new IndexSearcher(FSDirectory.open(new File(index)));
		} catch (Exception e) {
			throw new DpcDaoException("index searcher initialization", e);
		}
		analyzer = useStemmer?new EnglishAnalyzer(LUCENE_VERSION):new StandardAnalyzer(LUCENE_VERSION);
		parser = new QueryParser(LUCENE_VERSION, Dpc.F_TEXT, analyzer);
	}

	public DpcSearchResultImpl getTop(DpcSearchOpts opts) throws DpcDaoException {
		List<DpcDocImpl> docs = new ArrayList<DpcDocImpl>();
		Query q;
		TopDocs top;
		long searchTime;
		if (false) { // strict term query
			if (opts.getQ().equals(""))
				return EMPTY;
			if (opts.isAll()) {
				q = new TermQuery(new Term(Dpc.F_TEXT, opts.getQ()));
			} else {
				q = new BooleanQuery();
				if (opts.isF_description())
					((BooleanQuery) q).add(
							new TermQuery(new Term(Dpc.F_DESCRIPTION, opts.getQ()))
							, Occur.SHOULD);
				if (opts.isF_title())
					((BooleanQuery) q).add(
							new TermQuery(new Term(Dpc.F_TITLE, opts.getQ()))
							, Occur.SHOULD);
				if (opts.isF_comment())
					((BooleanQuery) q).add(
							new TermQuery(new Term(Dpc.F_COMMENT, opts.getQ()))
							, Occur.SHOULD);
			}
		} else
			// build extended query string and let QueryParser do the code representation 
			try {
				String t = "";
				if (opts.isAll()) {
					t = opts.getQ();
				} else {
					if (opts.isF_description()) {
						t += Dpc.F_DESCRIPTION + ':' + opts.getQ();
					}
					if (opts.isF_title()) {
						t += (opts.isF_description() ? " OR " : "") + Dpc.F_TITLE + ':' + opts.getQ();
					}
					if (opts.isF_comment()) {
						t += (opts.isF_description() || opts.isF_title() ? " OR " : "") + Dpc.F_COMMENT + ':' + opts.getQ();
					}
					if (opts.isF_categories()) {
						t += (opts.isF_description() || opts.isF_title() || opts.isF_comment() ? " OR " : "") + Dpc.F_CATEGORIES + ':'
								+ opts.getQ();
					}
				}
				if (t.equals(""))
					return EMPTY;
				q = parser.parse(t);
				//System.out.println(q.getClass() + " " + q);
			} catch (ParseException e) {
				throw new DpcDaoException("query parsing", e);
			}

		// search for hits
		try {
			searchTime = System.currentTimeMillis();
			top = searcher.search(q, opts.getStart() + opts.getNum());
			searchTime = System.currentTimeMillis() - searchTime;
		} catch (IOException e) {
			throw new DpcDaoException("searching", e);
		}
		// build DpcDoc models from results
		if (opts.getStart() < top.scoreDocs.length)
			for (ScoreDoc doc : Arrays.asList(top.scoreDocs).subList(opts.getStart(), top.scoreDocs.length - 1)) {
				Document d;
				try {
					d = searcher.doc(doc.doc);
				} catch (Exception e) {
					throw new DpcDaoException("fetching doc", e);
				}
				docs.add(createDpcDoc(doc.doc, doc.score, d));
			}
		return new DpcSearchResultImpl(top.totalHits, opts.getStart(), docs, searchTime);
	}

	public DpcDoc getDoc(int docId) throws DpcDaoException {
		Document d;
		try {
			d = searcher.doc(docId);
		} catch (Exception e) {
			throw new DpcDaoException("fetching one", e);
		}
		return createDpcDoc(docId, Dpc.NO_SCORE, d);
	}

	DpcDocImpl createDpcDoc(int docId, float score, Document doc) {
		return new DpcDocImpl(docId, Long.parseLong(doc.get(Dpc.F_ID)), score, doc.get(Dpc.F_TITLE),
				doc.get(Dpc.F_DESCRIPTION), doc.get(Dpc.F_CATEGORIES), doc.get(Dpc.F_COMMENT));
	}
}