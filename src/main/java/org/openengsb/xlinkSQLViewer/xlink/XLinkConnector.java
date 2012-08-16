package org.openengsb.xlinkSQLViewer.xlink;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.events.RegisteredToolsUpdateEvent;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.domain.SQLCode.SQLCodeDomain;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingLogic;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingResult;


/**
 * Connector implementation of the ExampleDomain 'SQLCode'
 */
public class XLinkConnector implements SQLCodeDomain, Connector {
	
	private static Logger logger = Logger.getLogger(XLinkConnector.class.getName());
	
	/**Reference to the GUI*/
	private SqlViewerGUI gui;
	
	/**Class to process potential Matches*/
	private MatchingLogic matchLogic;
	
	private String domainId;
	private String connectorId;

	public XLinkConnector(SqlViewerGUI gui) {
		super();
		this.gui = gui;
		this.matchLogic = new MatchingLogic(gui.getWorkingDirectory());
	}

	/**
	 * Updates the internal managed Datamodel
	 */
	@Override
	public void updateData(SQLCreate model) {
		logger.debug("'updateData' was triggered from the OpenEngSB");
		gui.addData(model);
	}

	/**
	 * Updates the Registered ToolList
	 */
	@Override
	public void onRegisteredToolsUpdateEvent(RegisteredToolsUpdateEvent e) {
		logger.debug("'onRegisteredToolsUpdateEvent' was triggered from the OpenEngSB");
		fetchTemplate().setRegisteredTools(e.getRegisteredTools());
	}

	/**
	 * Processes incomming, potential matches.<br/>
	 * Local matches are searched and displayed.
	 */
	@Override
	public void openXLinks(Object[] potentialMatch, String viewId) {
		if(!SqlViewerGUI.viewId.equals(viewId)){
			logger.warn("An XLink matching was triggerd with an unknown viewId.");
			gui.showXLinkMissMatch("An XLink matching was triggerd with an unknown viewId.");
			return;
		}
		ModelDescription modelInformation = fetchTemplate().getViewToModels().get(viewId);
		/*The Model 'SQLCreate' is the only accepted Model here*/
		if(modelInformation.getModelClassName().equals(SQLCreate.class.getName())){
			for(Object match : potentialMatch){
				SQLCreate currentMatch = convertToCreate(match.toString());
				if(currentMatch != null){
					MatchingResult foundMatch = matchLogic.findMostPotentialLocalMatch(currentMatch);
					if(foundMatch != null){
						gui.openXLinkMatch(foundMatch);
						return;
					}
				}
			}
		}else{
			logger.warn("An XLink matching was triggered, but the defined model is not supported");
			gui.showXLinkMissMatch("An XLink matching was triggered, but the defined model is not supported");
		}
		logger.warn("An XLink matching was triggered, but no match was found.");
		gui.showXLinkMissMatch("An XLink matching was triggered, but no match was found.");
	}
	
	/**
	 * Converts the received match to the local used SQLCreate object.
	 * Returns null, if the conversion fails
	 */
	private SQLCreate convertToCreate(String object){
        ObjectMapper mapper = new ObjectMapper();
        SQLCreate result;
		try {
			result = mapper.readValue(object, SQLCreate.class);
			return result;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public AliveState getAliveState() {
		return AliveState.ONLINE;
	}

	@Override
	public String getInstanceId() {
		return null;
	}

	@Override
	public String getConnectorId() {
		return connectorId;
	}

	@Override
	public String getDomainId() {
		return domainId;
	}

	@Override
	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}

	@Override
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	/**
	 * Fetches the XLinkTemple from the ConnectorManager
	 */
	private XLinkTemplate fetchTemplate(){
		return OpenEngSBConnectionManager.getInstance().getTemplate();
	}
}
