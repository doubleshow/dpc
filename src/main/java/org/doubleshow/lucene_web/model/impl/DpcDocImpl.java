package org.doubleshow.lucene_web.model.impl;

import org.doubleshow.lucene_web.model.DpcDoc;

public class DpcDocImpl implements DpcDoc {
	int docId;
	long id;
	float score;
	String title;
	String description;
	String comment;
	String categories;

	public int getDocId() {
		return docId;
	}

	public long getId() {
		return id;
	}

	public float getScore() {
		return score;
	}

	@Override
	public String toString() {
		return "DpcDocImpl [docId=" + docId + ", id=" + id + ", score=" + score + ", title=" + title + ", description=" + description
				+ ", comment=" + comment + "]";
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getComment() {
		return comment;
	}

	public DpcDocImpl(int docId, long id, float score, String title, String description, String categories, String comment) {
		super();
		this.docId = docId;
		this.id = id;
		this.score = score;
		this.title = title;
		this.description = description;
		this.categories = categories;
		this.comment = comment;
	}

	public String getCategories() {
		return categories;
	}
}