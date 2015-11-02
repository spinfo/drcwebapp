package de.uni_koeln.spinfo.drc.mongodb.data.document;

import java.util.List;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import de.uni_koeln.spinfo.drc.mongodb.data.core.RangeDocument;

@Document(collection = "volumes")
public class Volume extends RangeDocument {
	
	private String title;
	
	//private List<String> chapterIds = new ArrayList<>();

	@Transient private List<Chapter> chapters;
	@Transient private List<Page> pages;
	@Transient private List<Language> languages;
	
	// NEW 
	public Volume() {
		super();
	}
	
	@PersistenceConstructor
	public Volume(final String title, int start, int end, final String userId) {
		super();
		this.title = title;
		setStart(start);
		setEnd(end);
		setUserId(userId);
	}
	
//	public boolean addChapter(String chapterId) {
//		return chapterIds.add(chapterId);
//	}
//	
//	public boolean removeChapter(String chapterId) {
//		return chapterIds.remove(chapterId);
//	}
//	
//	public List<String> getChapters() {
//		return new ArrayList<>(chapterIds);
//	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "Volume {title: " + title + ", _id: " + getId() + "}";
	}

	public void setChapters(List<Chapter> chapters) {
		this.chapters = chapters;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public void setLanguages(List<Language> languages) {
		this.languages = languages;
	}
	
	
	public List<Language> getLanguages() {
		return languages;
	}
	
	public List<Page> getPages() {
		return pages;
	}
	
	public List<Chapter> getChapters() {
		return chapters;
	}
	

}
