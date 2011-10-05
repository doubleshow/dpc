package org.doubleshow.lucene_web.model;

public interface DpcDao {
	public DpcSearchResult getTop(DpcSearchOpts opts) throws DpcDaoException;

	public DpcDoc getDoc(int docId) throws DpcDaoException;
}