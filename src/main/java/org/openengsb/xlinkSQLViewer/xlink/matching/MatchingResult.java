package org.openengsb.xlinkSQLViewer.xlink.matching;

import java.io.File;
import java.util.List;

import org.openengsb.xlinkSQLViewer.model.SQLCreateModel;

/**
 * ModelClass to transport a Matching
 */
public class MatchingResult {
	
	/**File the Matching was found in.*/
	private File file;
	/**List of contained SQL CreateStatements, of the file*/
	private List<SQLCreateModel> createStatements;
	/**ListIndex of the found match*/
	private int indexOfMatch;
	
	public MatchingResult(File fileName, List<SQLCreateModel> createStatements,
			int indexOfMatch) {
		super();
		this.file = fileName;
		this.createStatements = createStatements;
		this.indexOfMatch = indexOfMatch;
	}
	
	public File getFileName() {
		return file;
	}
	public void setFileName(File fileName) {
		this.file = fileName;
	}
	public List<SQLCreateModel> getCreateStatements() {
		return createStatements;
	}
	public void setCreateStatements(List<SQLCreateModel> createStatements) {
		this.createStatements = createStatements;
	}
	public int getIndexOfMatch() {
		return indexOfMatch;
	}
	public void setIndexOfMatch(int indexOfMatch) {
		this.indexOfMatch = indexOfMatch;
	}
	
	public SQLCreateModel getMatchingCreate(){
		return createStatements.get(indexOfMatch);
	}
	

}
