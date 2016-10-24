package com.googlecode._4s_web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("file")
public interface FileService extends RemoteService {
	/**
	 * Method that stores a file on the server. It computes the save path
	 * depending on the case type and the case id.
	 */
	public String uploadAttachement(String caseId, String fieldName,boolean isNewCase);
 
	/**
	 * Method that deletes a file from the server. It computes the file path
	 * depending on the case type and the case id.
	 */
	public boolean deleteAttachement(String filePath, int caseID,
			String fieldName);
 
	/**
	 * Update the name of the attachment
	 */
	public String updateFileName(final String name);
}
