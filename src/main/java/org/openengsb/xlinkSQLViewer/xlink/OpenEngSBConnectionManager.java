package org.openengsb.xlinkSQLViewer.xlink;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.api.xlink.model.ModelViewMapping;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.service.XLinkConnectorManager;
import org.openengsb.domain.SQLCode.SQLCodeDomainEvents;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.loom.java.ProxyConnectorFactory;
import org.openengsb.loom.java.jms.JmsProtocolHandler;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;

/**
 * ConnectionManager for the OpenEngSB
 */
public class OpenEngSBConnectionManager {

    private static Logger logger = Logger.getLogger(OpenEngSBConnectionManager.class.getName());

    /* XLink variables */
    private ProxyConnectorFactory domainFactory;
    private JmsProtocolHandler jmsConfig;
    private XLinkConnectorManager xcm;

    /* Supplied program arguments */
    private final String xlinkServerURL;
    private final String domainId;
    private final String programname;
    private final String openengsbUser;
    private final String openengsbPassword;
    private final String sqlCodeDomainVersion;

    /** Id of the registered OpenEngSB-Connector */
    private String connectorUUID;

    /** Flag indicating the connection status */
    private boolean connected = false;

    /**
     * HostIP of the local system, used to identify the Host during an XLink call
     */
    private final String hostIp;

    /** Proxy EventInterface to send events to the OpenEngSB */
    private SQLCodeDomainEvents domainEvents;

    /** Only possible OpenEngSBConnectionManager instance */
    private static OpenEngSBConnectionManager instance = null;

    private OpenEngSBConnectionManager(String xlinkServerURL, String domainId, String programname,
            String openengsbUser, String openengsbPassword, String hostIp, String sqlCodeDomainVersion) {
        super();
        this.xlinkServerURL = xlinkServerURL;
        this.domainId = domainId;
        this.programname = programname;
        this.connected = false;
        this.hostIp = hostIp;
        this.openengsbUser = openengsbUser;
        this.openengsbPassword = openengsbPassword;
        this.sqlCodeDomainVersion = sqlCodeDomainVersion;
    }

    /**
     * Initializes the Connectors only instance.
     */
    public static void initInstance(String xlinkBaseUrl, String domainId, String programname, String openengsbUser,
        String openengsbPassword, String hostIp, String sqlCodeDomainVersion) {
        instance =
            new OpenEngSBConnectionManager(xlinkBaseUrl, domainId, programname, openengsbUser, openengsbPassword,
                    hostIp, sqlCodeDomainVersion);
    }

    /**
     * Returns the Connectors only instance.
     */
    public static OpenEngSBConnectionManager getInstance() {
        if (instance == null) {
            logger.warn("getInstance():OpenEngSBConnectionManager was not initialized.");
        }
        return instance;
    }

    /***
     * Creates/Registers the connector at the OpenEngSB and registers the connector to XLink
     * 
     * @throws DomainNotLinkableException
     */
    public void connectToOpenEngSbWithXLink(SqlViewerGUI gui) throws JMSException, ConnectorValidationFailedException {
        /* Create/Register the connector */
        jmsConfig = new JmsProtocolHandler(xlinkServerURL, "sample-xlink-java-client");
        domainFactory = new ProxyConnectorFactory(jmsConfig, openengsbUser, new Password(openengsbPassword));
        connectorUUID = domainFactory.createConnector(domainId);
        LinkableConnector handler = new LinkableConnector(gui, domainFactory);
        domainFactory.registerConnector(connectorUUID, handler);

        /* Fetch Event Interface */
        domainEvents = domainFactory.getRemoteProxy(SQLCodeDomainEvents.class);

        /* Register to XLink */
        xcm = domainFactory.getRemoteProxy(XLinkConnectorManager.class);
        xcm.registerWithXLink(connectorUUID, hostIp, "SQLViewer", initModelViewMapping());

        connected = true;
        gui.changeStatusToConnected();
        logger.info("connected to openEngSB and XLink");
    }

    /**
     * Creates the Array of Model/View relations, offered by the Tool, for XLink
     */
    private ModelViewMapping[] initModelViewMapping() {
        ModelViewMapping[] modelsViews = new ModelViewMapping[1];

        Map<Locale, String> descriptions = new HashMap<Locale, String>();
        descriptions.put(new Locale("en"), "This view opens the values in a SQLViewer.");
        descriptions.put(new Locale("de"), "Dieses Tool oeffnet die Werte in einem SQLViewer.");

        XLinkConnectorView[] views = new XLinkConnectorView[1];
        views[0] = new XLinkConnectorView(SqlViewerGUI.viewId, SqlViewerGUI.viewName, descriptions);

        modelsViews[0] =
            new ModelViewMapping(new ModelDescription(SQLCreate.class.getName(), sqlCodeDomainVersion), views);

        return modelsViews;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Sends DataUpdates to the OpenEnbSB
     */
    public void raiseDataUpdate(SQLCreate newCreate) {
        domainEvents.raiseDataUpdate(newCreate);
    }

    /**
     * Unregisters the connector from XLink and removes it from the OpenEngSB
     */
    public void disconnect() {
        xcm.unregisterFromXLink(connectorUUID);
        domainFactory.unregisterConnector(connectorUUID);
        domainFactory.deleteConnector(connectorUUID);
        jmsConfig.destroy();
        logger.info("disconnected from openEngSB and XLink");
    }

    public XLinkConnectorManager getXcm() {
        return xcm;
    }

    public String getConnectorId() {
        return connectorUUID;
    }
}
