package de.uni_koeln.spinfo.drc.mongodb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.uni_koeln.spinfo.drc.mongodb.data.document.Page;

public interface PageRepository extends CrudRepository<Page, String> {
	
	public Page findByUrl(String url);
	
	public List<Page> findByUserId(String userId);
	
	public List<Page> findByVolumeId(String volumeId);
	
	public List<Page> findByChapterIds(List<String> chapterIds);
	
	public List<Page> findByLanguageIds(List<String> LanguageIds);

}
