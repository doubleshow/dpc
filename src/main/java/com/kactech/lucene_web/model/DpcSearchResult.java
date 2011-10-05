package com.kactech.lucene_web.model;

import java.util.List;

public interface DpcSearchResult {

	public abstract int getTotalHits();

	public abstract int getStart();

	public abstract List<? extends DpcDoc> getDocs();

	public abstract long getSearchTime();

}