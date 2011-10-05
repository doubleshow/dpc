package org.doubleshow.lucene_web;

public interface Dpc {
	static final String F_TEXT = "text";
	static final String F_TITLE = "title";
	static final String F_ID = "id";
	static final String F_DESCRIPTION = "description";
	static final String F_CATEGORIES = "categories";
	static final String F_COMMENT = "comment";

	static final float NO_SCORE = -1;

	static final String DEFAULT_INDEX = "index-dpc";
	static final String DEFAULT_DOCS = "dpc.tsv";

	static final String SP_PREFIX = "dpc_web.";
	static final String P_INDEX = "indexPath";
	static final String P_USE_STEMMER = "useStemmer";
}
