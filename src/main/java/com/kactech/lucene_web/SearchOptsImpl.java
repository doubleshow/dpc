package com.kactech.lucene_web;

import com.kactech.lucene_web.model.DpcSearchOpts;

public class SearchOptsImpl implements DpcSearchOpts {
	private String q;
	private boolean all, f_title, f_description, f_comment, f_categories;
	private int start, num;

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	public boolean isF_title() {
		return f_title;
	}

	public void setF_title(boolean f_title) {
		this.f_title = f_title;
	}

	public boolean isF_description() {
		return f_description;
	}

	public void setF_description(boolean f_description) {
		this.f_description = f_description;
	}

	public boolean isF_comment() {
		return f_comment;
	}

	public void setF_comment(boolean f_comment) {
		this.f_comment = f_comment;
	}

	public SearchOptsImpl(String q, boolean all, boolean f_title, boolean f_description, boolean f_categories, boolean f_comment,
			int start, int num) {
		this.q = q;
		this.all = all;
		this.f_title = f_title;
		this.f_description = f_description;
		this.f_categories = f_categories;
		this.f_comment = f_comment;
		this.start = start;
		this.num = num;
	}

	public int getStart() {
		return start;
	}

	public int getNum() {
		return num;
	}

	public boolean isF_categories() {
		return f_categories;
	}

	public void setF_categories(boolean f_categories) {
		this.f_categories = f_categories;
	}

}