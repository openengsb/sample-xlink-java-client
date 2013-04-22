package org.openengsb.xlinkSQLViewer.xlink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.domain.SQLCode.SQLCodeDomain;
import org.openengsb.domain.SQLCode.model.SQLCreate;
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
                        SQLCreate currentMatch = convertToCreate(match
                                .toString());
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
     * Converts the received match to the local used SQLCreate object. Returns null, if the conversion fails TODO
     * [OPENENGSB-3270] remove hack
     */
    private SQLCreate convertToCreate(String object) {
        object = convertJSONStringHack(object);
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

    /**
     * TODO [OPENENGSB-3270] parse the incoming string into the correct format, as long as the loom-java bridge dosen't
     * resolve the objects correctly
     */
    private String convertJSONStringHack(String jsonString) {
        // remove tail
        String replaced = jsonString.replaceAll(", openEngSBModelTail=\\[\\]",
                "");
        // replace normal key/value with =
        replaced = replaced.replaceAll(
                "[ ]?(\\w+\\([0-9]+\\)|\\w+)=(\\w+\\([0-9]+\\)|\\w+)",
                "\"$1\":\"$2\"");
        // replace head of list
        replaced = replaced.replaceAll(" (\\w+\\([0-9]+\\)|\\w+)=\\[",
                "\"$1\":\\[");

        // replaced blank between complex elements
        replaced = replaced.replaceAll("\\}, \\{", "\\},\\{");

        // replace list
        String listPatternString =
            "\\[[\\w |REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)][, \\w |REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)]*\\]";
        Pattern listPattern = Pattern.compile(listPatternString);
        Matcher foreingKeyMatcher = listPattern.matcher(replaced);

        while (foreingKeyMatcher.find()) {
            String foundList = foreingKeyMatcher.group(0);
            // singleList Replace
            String replacedList = foundList.replaceAll(
                    "\\[([\\w |REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)]+)\\]",
                    "\\[\"$1\"\\]");
            // startList Replace
            replacedList = replacedList.replaceAll(
                    "\\[([\\w |REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)]+), ",
                    "\\[\"$1\",");
            // middleList Replace
            replacedList = replacedList.replaceAll(
                    ",[ ]?([\\w |REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)]+)[ ]?,",
                    ",\"$1\",");
            // endList Replace
            replacedList = replacedList.replaceAll(
                    ",[ ]?([\\w |REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)]+)\\]",
                    ",\"$1\"\\]");
            replaced = replaced.replace(foundList, replacedList);
        }
        return replaced;
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
