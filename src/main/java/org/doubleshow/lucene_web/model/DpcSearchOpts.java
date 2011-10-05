package org.doubleshow.lucene_web.model;

public interface DpcSearchOpts {

	public abstract String getQ();

	public abstract boolean isAll();

	public abstract boolean isF_title();

	public abstract boolean isF_description();

	public abstract boolean isF_comment();

	public abstract boolean isF_categories();

	public abstract int getStart();

	public abstract int getNum();
}