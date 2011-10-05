package org.doubleshow.lucene_web.model;

public interface DpcDoc {

	public abstract int getDocId();

	public abstract long getId();

	public abstract float getScore();

	public abstract String getTitle();

	public abstract String getDescription();

	public abstract String getComment();

	public abstract String getCategories();
}