package org.openengsb.xlinkSQLViewer.xlink;

import org.apache.log4j.Logger;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.LinkingSupport;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkObject;
import org.openengsb.domain.SQLCode.SQLCodeDomain;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.loom.java.ProxyConnectorFactory;
import org.openengsb.loom.java.util.JsonUtils;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingLogic;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingResult;

/**
 * Connector implementation of the ExampleDomain 'SQLCode'
 */
public class LinkableConnector implements SQLCodeDomain, Connector {
    private static Logger logger = Logger.getLogger(LinkableConnector.class.getName());

    /** Reference to the GUI */
    private final SqlViewerGUI gui;

    /** Class to process potential Matches */
    private final MatchingLogic matchLogic;

    private final ProxyConnectorFactory connectorFactory;

    private String domainId;
    private String connectorId;

    public LinkableConnector(SqlViewerGUI gui, ProxyConnectorFactory connectorFactory) {
        super();
        this.gui = gui;
        this.matchLogic = new MatchingLogic(gui.getWorkingDirectory());
        this.connectorFactory = connectorFactory;
    }

    /**
     * Updates the internal managed Datamodel
     */
    @Override
    public void updateData(SQLCreate model) {
        logger.debug("'updateData' was triggered from the OpenEngSB");
        gui.addData(model);
    }

    @Override
    public void showXLinks(final XLinkObject[] arg0) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (XLinkObject obj : arg0) {
                    LinkingSupport service =
                        connectorFactory.getRemoteProxy(LinkingSupport.class, obj.getConnectorId());
                    // service.openXLink(obj.getModelDescription(), obj, obj.getViews());
                    if (!obj.getModelDescription().getModelClassName().equals(SQLCreate.class.getName())) {
                        logger.warn("An XLink matching was triggered, but the defined model is not supported");
                        gui.showXLinkMissMatch("An XLink matching was triggered, but the defined model is not supported");
                        continue;
                    }
                    SQLCreate currentMatch = convertToCreate(obj.getModelObject());
                    if (currentMatch != null) {
                        MatchingResult foundMatch = matchLogic.findMostPotentialLocalMatch(currentMatch);
                        if (foundMatch != null) {
                            gui.openXLinkMatch(foundMatch);
                            return;
                        }
                    }

                }
                logger.warn("An XLink matching was triggered, but no match was found.");
                gui.showXLinkMissMatch("An XLink matching was triggered, but no match was found.");
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
    public void openXLink(final ModelDescription arg0, final Object arg1, XLinkConnectorView arg2) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!arg0.getModelClassName().equals(SQLCreate.class.getName())) {
                    logger.warn("An XLink matching was triggered, but the defined model is not supported");
                    gui.showXLinkMissMatch("An XLink matching was triggered, but the defined model is not supported");
                } else {
                    SQLCreate currentMatch = convertToCreate(arg1);
                    if (currentMatch != null) {
                        MatchingResult foundMatch = matchLogic.findMostPotentialLocalMatch(currentMatch);
                        if (foundMatch != null) {
                            gui.openXLinkMatch(foundMatch);
                            return;
                        }
                    } else {
                        logger.warn("An XLink matching was triggered, but no match was found.");
                        gui.showXLinkMissMatch("An XLink matching was triggered, but no match was found.");
                    }
                }
            }
        }).start();

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

}
