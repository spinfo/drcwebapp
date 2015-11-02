package de.uni_koeln.spinfo.drc.mongodb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.uni_koeln.spinfo.drc.mongodb.data.document.Word;

public interface WordRepository extends CrudRepository<Word, String> {
	
	public List<Word> findByVolumeId(String volumeId);
	
	public List<Word> findByPageId(String pageId);
	
	public List<Word> findByChapterId(String chapterId);
	
	public List<Word> findByLanguageId(String languageId);
	
	public Word findByIndex(int index);

}
