package org.openengsb.xlinkSQLViewer.xlink;

import org.apache.log4j.Logger;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.domain.SQLCode.SQLCodeDomain;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.loom.java.util.JsonUtils;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingLogic;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingResult;

/**
 * Connector implementation of the ExampleDomain 'SQLCode'
 */
public class LinkableConnector implements SQLCodeDomain, Connector {

    private static Logger logger = Logger.getLogger(LinkableConnector.class
            .getName());

    /** Reference to the GUI */
    private SqlViewerGUI gui;

    /** Class to process potential Matches */
    private MatchingLogic matchLogic;

    private String domainId;
    private String connectorId;

    public LinkableConnector(SqlViewerGUI gui) {
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
    public void onRegisteredToolsChanged(XLinkConnector[] changedConnectors) {
        logger
                .debug("'onRegisteredToolsUpdateEvent' was triggered from the OpenEngSB");
        fetchTemplate().setRegisteredTools(changedConnectors);
        gui.updateLocalSwitchMenu();
    }

    /**
     * Processes incomming, potential matches.<br/>
     * Local matches are searched and displayed.
     */
    @Override
    public void openXLinks(final Object[] potentialMatch, final String viewId) {
        new Thread(new Runnable() {
            public void run() {
                if (!SqlViewerGUI.viewId.equals(viewId)) {
                    logger
                            .warn("An XLink matching was triggerd with an unknown viewId.");
                    gui
                            .showXLinkMissMatch("An XLink matching was triggerd with an unknown viewId.");
                    return;
                }
                ModelDescription modelInformation = fetchTemplate()
                        .getViewToModels().get(viewId);
                /* The Model 'SQLCreate' is the only accepted Model here */
                if (modelInformation.getModelClassName().equals(
                        SQLCreate.class.getName())) {
                    for (Object match : potentialMatch) {
                        SQLCreate currentMatch = convertToCreate(match);
                        if (currentMatch != null) {
                            MatchingResult foundMatch = matchLogic
                                    .findMostPotentialLocalMatch(currentMatch);
                            if (foundMatch != null) {
                                gui.openXLinkMatch(foundMatch);
                                return;
                            }
                        }
                    }
                } else {
                    logger
                            .warn("An XLink matching was triggered, but the defined model is not supported");
                    gui
                            .showXLinkMissMatch("An XLink matching was triggered, but the defined model is not supported");
                }
                logger
                        .warn("An XLink matching was triggered, but no match was found.");
                gui
                        .showXLinkMissMatch("An XLink matching was triggered, but no match was found.");
            }
        }).start();
    }

    /**
     * Converts the received match to the local used SQLCreate object. Returns null, if the conversion fails 
     */
    private SQLCreate convertToCreate(Object object) {
        SQLCreate result = null;
        try {
             result = JsonUtils.convertArgument(object, SQLCreate.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return result;
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
    private XLinkUrlBlueprint fetchTemplate() {
        return OpenEngSBConnectionManager.getInstance().getBluePrint();
    }

}
