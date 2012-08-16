package org.openengsb.xlinkSQLViewer.model;

/**
 * Program intern Model, to manage SQL Createstatements
 */
public class SQLCreateModel {
	
	/**Name of the created Table*/
	private String tableName;
	/**Body of the create Statement*/
	private String createBody;

	public SQLCreateModel(String tableName, String createBody) {
		super();
		this.tableName = tableName;
		this.createBody = createBody;
	}
	
	public String getTableName() {
		return tableName;
	}
	public String getCreateBody() {
		return createBody;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public void setCreateBody(String createBody) {
		this.createBody = createBody;
	}

	@Override
	public String toString() {
		return "create Table " + tableName;
	}
	
	

}
