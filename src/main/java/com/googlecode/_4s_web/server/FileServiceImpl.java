package com.googlecode._4s_web.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.fileupload.util.Streams;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode._4s_web.client.FileService;
import com.googlecode._4s_web.client.entity.CharacterEntity;
import com.googlecode._4s_web.client.entity.EventEntity;
import com.googlecode._4s_web.client.entity.StoryEntity;

/**
 * The server side implementation of the RPC service(File upload).
 */
@SuppressWarnings("serial")
public class FileServiceImpl extends RemoteServiceServlet implements
FileService {

	@Override
	public String uploadAttachement(String caseId, String fieldName,
			boolean isNewCase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteAttachement(String filePath, int caseID,
			String fieldName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String updateFileName(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	private StoryServiceImpl storyService = new StoryServiceImpl();
	
	/**
	 * client에서 file을 보내면 여기서 처리한다
	 */
	@Override
	public void service(final HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String storyName = "";
		String content = "";
		System.out.println("FileServiceImpl.service started!");
		boolean isMultiPart = ServletFileUpload
				.isMultipartContent(new ServletRequestContext(request));
		if(isMultiPart) {
			try {
		        ServletFileUpload upload = new ServletFileUpload();
		        FileItemIterator iterator = upload.getItemIterator(request);
		        while(iterator.hasNext()){
		            FileItemStream item = iterator.next();
		            InputStream stream = item.openStream();
		            if(item.isFormField()){
		            	// POST로 넘어온 form field 처리
		                storyName = parseRequestFields(storyName, item, stream);
		            }else{
		            	// file content 처리
		            	content = parseRequestFile(stream);
		            }
		        }
		    } catch (FileUploadException e) {
		        e.printStackTrace();
		    }
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.getWriter().print("OK");
			response.flushBuffer();
		}
		else {
			super.service(request, response);
			return;
		}
		
		createStoryFromFileContent(storyName, content);
	}

	/**
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	private String parseRequestFile(InputStream stream) throws IOException {
		String content;
		StringBuffer contentBuffer = new StringBuffer();
		content = Streams.asString(stream, "KSC5601");
		List<String> lineList = new ArrayList<String>(Arrays.asList(content.split("\n")));
		// skip first line(=title)
		for(int i = 1; i < lineList.size(); i++) {
			String line = lineList.get(i);
			if(line.startsWith("E")) {
				contentBuffer.append("\n");
			}
			contentBuffer.append(line);
		}
		content = contentBuffer.toString().trim();
		return content;
	}

	/**
	 * @param storyName
	 * @param item
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private String parseRequestFields(String storyName, FileItemStream item,
			InputStream stream) throws IOException,
			UnsupportedEncodingException {
		if(item.getFieldName().equals("storyName")){
		    byte[] str = new byte[stream.available()];
		    stream.read(str);
		    storyName = new String(str,"UTF8");
		}
		return storyName;
	}

	/**
	 * @param storyName
	 * @param content
	 */
	private void createStoryFromFileContent(String storyName, String content) {
		List<String> eventLineList;
		Double eventWidth = 100.0;
		StoryEntity se = storyService.createNewStory(storyName, "");
		
		eventLineList = new ArrayList<String>(Arrays.asList(content.split("\n")));
		// create character first
		Set<String> characterNameCandidateSet = new HashSet<String>();
		for(int i = 0; i < eventLineList.size(); i++) {
			List<String> itemList;
			String eventLine = eventLineList.get(i);
			itemList = new ArrayList<String>(Arrays.asList(eventLine.split("\t")));
			String eventCharacterNames = itemList.get(6);
			eventCharacterNames = eventCharacterNames.replace("\"", "");
			List<String> characterNameList = new ArrayList<String>(Arrays.asList(eventCharacterNames.split(",")));
			for(String characterName: characterNameList) {
				characterNameCandidateSet.add(characterName.trim());
			}
			
		}
		for(String characterName: characterNameCandidateSet) {
			// need to trim white spaces
			if(!characterName.equals("")) {
				storyService.createNewCharacter(characterName);
			}
		}
		// process each line
		if(eventLineList.size() > 0) {
			eventWidth = 100.0/eventLineList.size();
		}
		for(int i = 0; i < eventLineList.size(); i++) {
			List<String> itemList;
			String eventLine = eventLineList.get(i);
			itemList = new ArrayList<String>(Arrays.asList(eventLine.split("\t")));
			
			String eventName = itemList.get(0);
			EventEntity ee = storyService.createNewEvent(eventName);
			Integer eventOrdinalStoryIn = Integer.parseInt(itemList.get(2));
			Integer eventOrdinalStoryOut = eventOrdinalStoryIn + 1;
			ee.setOrdinalStoryInOut(eventOrdinalStoryIn, eventOrdinalStoryOut);
			
			Double eventDiscourseIn = Double.parseDouble(itemList.get(1));
			eventDiscourseIn = (eventDiscourseIn - 1) * eventWidth;
			Double eventDiscourseOut = eventDiscourseIn + eventWidth; 
			ee.addToDiscourse(eventDiscourseIn, eventDiscourseOut);
			
			String eventActionDescription = itemList.get(9);
			ee.setActionDescription(eventActionDescription);
			
			String eventPlace = itemList.get(7);
			ee.setPlace(eventPlace);
			
			// only one main character assumed
			String eventMainCharacterName = itemList.get(5).trim();
			
			String eventCharacterNames = itemList.get(6);
			eventCharacterNames = eventCharacterNames.replace("\"", "");
			List<String> characterNameList = new ArrayList<String>(Arrays.asList(eventCharacterNames.split(",")));
			CharacterEntity[] storedCharacterList = (CharacterEntity[])storyService.loadEntities(CharacterEntity.class.getName());
			Collection<Long> involvedIds = new ArrayList<Long>();
			for(CharacterEntity storedCharacter: storedCharacterList) {
				String storedCharacterName = storedCharacter.getName();
				long storedCharacterId = storedCharacter.getId();
				for(String rawCharacterName: characterNameList) {
					String characterName = rawCharacterName.trim();
					if(characterName.equals(storedCharacterName)) {
						involvedIds.add(storedCharacterId);
					}
				}
				if(storedCharacterName.equals(eventMainCharacterName)) {
					ee.setMainCharacter(storedCharacterId);
				}
			}
			ee.setInvolvedCharacters(involvedIds);
			storyService.saveEntity(EventEntity.class.getName(), ee);
		}
		storyService.saveStoryHeader(se);
	}
}