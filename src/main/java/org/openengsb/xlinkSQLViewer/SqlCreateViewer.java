package org.openengsb.xlinkSQLViewer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;
import org.openengsb.xlinkSQLViewer.xlink.OpenEngSBConnectionManager;

/**
 * Main class of the XLinkExample program<br/>
 * <br/>
 * 
 * Propertiesfile must supply the following properties:<br/>
 * working.dir - Workingdirectory of the program, to search for SQL files.<br/>
 * xlink.baseUrl - Connection URL to the OpenEngSB<br/>
 * openengsb.context - Projectcontext to use at the OpenEngSB<br/>
 * openengsb.connection.domainId - DomainId of the implemented Domain<br/>
 */
public class SqlCreateViewer {

    /** program exit codes - constant */
    private static final int EXIT_SUCCESS = 0;

    /** program exit codes - constant */
    private static final int EXIT_FAILURE = 1;

    /** path to the properties file */
    private static final String propertiesFile_dev = "src/main/resources/application.properties";
    private static String propertiesFile = "application.properties";
    /** path to the log4j properties file */
    private static final String log4jFile_dev = "src/main/resources/log4j.properties";
    private static String log4jFile = "log4j.properties";

    /** Programname to Display */
    private static String programname = "XLinkJavaClient";

    /* Supplied program arguments */
    private static String xlinkServerURL;
    private static String openEngSBContext;
    private static String domainId;
    private static File workingDirFile;
    private static String openengsbUser;
    private static String openengsbPassword;
    private static String openengsbHostIp;
    private static String clientLocale;
    private static String sqlCodeDomainVersion;

    private static Logger logger = Logger.getLogger(SqlCreateViewer.class.getName());

    public static void main(String[] args) {
        testSetPropertiesFilePaths();

        PropertyConfigurator.configure(log4jFile);
        readProperties();

        SqlViewerGUI gui = new SqlViewerGUI(workingDirFile, openEngSBContext, clientLocale);
        gui.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdownApplication();
            }
        });

        OpenEngSBConnectionManager.initInstance(xlinkServerURL, domainId, programname, openengsbUser,
                openengsbPassword, openengsbHostIp, sqlCodeDomainVersion);
        try {
            OpenEngSBConnectionManager.getInstance().connectToOpenEngSbWithXLink(gui);
        } catch (Exception e) {
            gui.handleErrorVisualy("Could not establish connection to OpenEngSB.", e);
        }
    }

    /**
     * Test if the propertyFiles exits
     */
    private static void testSetPropertiesFilePaths() {
        File propF = new File(propertiesFile);
        File propF_dev = new File(propertiesFile_dev);
        File log4jF = new File(log4jFile);
        File log4jF_dev = new File(log4jFile_dev);
        if (!propF.exists()) {
            if (!propF_dev.exists()) {
                logger.error("PropertiesFile '" + propertiesFile + "' was not found.");
                shutdownApplication(EXIT_FAILURE);
            } else {
                propertiesFile = propertiesFile_dev;
            }
        }
        if (!log4jF.exists()) {
            if (!log4jF_dev.exists()) {
                logger.error("LogConfigFile '" + log4jFile + "' was not found.");
                shutdownApplication(EXIT_FAILURE);
            } else {
                log4jFile = log4jFile_dev;
            }
        }
    }

    /**
     * Activates the logger, writes out the given message and exception and<br/>
     * terminates the program with an error.
     * 
     * @param msg error message
     * @param ex occured exception
     */
    public static void writeErrorAndExit(String msg, Exception ex) {
        logger.setLevel(Level.ALL);
        if (ex != null) {
            logger.log(Level.FATAL, msg, ex);
        } else {
            logger.log(Level.FATAL, msg);
        }
        shutdownApplication(EXIT_FAILURE);
    }

    public static void writeErrorWithoutExit(String msg, Exception ex) {
        logger.setLevel(Level.ALL);
        if (ex != null) {
            logger.log(Level.FATAL, msg, ex);
        } else {
            logger.log(Level.FATAL, msg);
        }
    }

    private static void shutdownApplication() {
        shutdownApplication(EXIT_SUCCESS);
    }

    /**
     * Closes all ressources properly before shutdown.
     * 
     * @param exitcode
     */
    private static void shutdownApplication(int exitcode) {
        logger.info("Exiting Program");
        if (OpenEngSBConnectionManager.getInstance() != null) {
            if (isConnected()) {
                OpenEngSBConnectionManager.getInstance().disconnect();
            }
        }
        System.exit(exitcode);
    }

    public static boolean isConnected() {
        return OpenEngSBConnectionManager.getInstance().isConnected();
    }

    /**
     * Reads the properties file
     */
    private static void readProperties() {
        try (java.io.InputStream in = new FileInputStream(propertiesFile)) {
            if (in != null) {
                java.util.Properties properties = new java.util.Properties();
                properties.load(in);

                initWorkingDirectory(properties);
                initXlinkServerURL(properties);
                initOpenEngSBContext(properties);
                initDomainId(properties);
                initOpenEngSBUser(properties);
                initOpenEngSBPassword(properties);
                initOpenEngSBHostIp(properties);
                initClientLocale(properties);
                initSqlCodeDomainVersion(properties);
            }
        } catch (IOException ex) {
            writeErrorAndExit("The properties file '" + propertiesFile + "' was not found.\n", ex);
        } catch (NumberFormatException e) {
            writeErrorAndExit("One of the values of the properties file, doesn't have the correct format.\n", e);
        } catch (Exception e3) {
            writeErrorAndExit("An exception occured during startup.\n", e3);
        }
    }

    private static void initSqlCodeDomainVersion(Properties properties) throws Exception {
        if (properties.getProperty("sqlcode.version") != null) {
            sqlCodeDomainVersion = properties.getProperty("sqlcode.version");
        } else {
            throw new Exception("SQLCode domain version (parameter 'sqlcode.version') must be set.");
        }
    }

    private static void initClientLocale(Properties properties) {
        if (properties.getProperty("client.locale") != null) {
            clientLocale = properties.getProperty("client.locale");
        } else {
            clientLocale = "en";
        }
    }

    private static void initOpenEngSBHostIp(Properties properties) throws Exception {
        if (properties.getProperty("openengsb.hostIp") != null) {
            openengsbHostIp = properties.getProperty("openengsb.hostIp");
        } else {
            openengsbHostIp = "127.0.0.1";
        }
    }

    private static void initOpenEngSBPassword(Properties properties) throws Exception {
        if (properties.getProperty("openengsb.password") != null) {
            openengsbPassword = properties.getProperty("openengsb.password");
        } else {
            throw new Exception("OpenEngSb Password (parameter 'openengsb.password') must be set.");
        }
    }

    private static void initOpenEngSBUser(Properties properties) throws Exception {
        if (properties.getProperty("openengsb.user") != null) {
            openengsbUser = properties.getProperty("openengsb.user");
        } else {
            throw new Exception("OpenEngSb User (parameter 'openengsb.user') must be set.");
        }
    }

    private static void initDomainId(Properties properties) throws Exception {
        if (properties.getProperty("openengsb.connection.domainId") != null) {
            domainId = properties.getProperty("openengsb.connection.domainId");
        } else {
            throw new Exception("OpenEngSb DomainId (parameter 'openengsb.connection.domainId') must be set.");
        }
    }

    private static void initOpenEngSBContext(Properties properties) throws Exception {
        if (properties.getProperty("openengsb.context") != null) {
            openEngSBContext = properties.getProperty("openengsb.context");
        } else {
            throw new Exception("OpenEngSb Context (parameter 'openengsb.context') must be set.");
        }
    }

    private static void initWorkingDirectory(Properties properties) throws Exception {
        if (properties.getProperty("working.dir") != null) {
            String workingDir = properties.getProperty("working.dir");
            workingDirFile = new File(workingDir);
            if (!workingDirFile.isDirectory()) {
                throw new Exception("Defined WorkingDirectory \"" + workingDir + "\" does not exist.");
            }
        } else {
            throw new Exception("WorkingDirectory (parameter 'working.dir') must be set.");
        }
    }

    private static void initXlinkServerURL(Properties properties) throws Exception {
        if (properties.getProperty("xlink.xlinkServerURL") != null) {
            xlinkServerURL = properties.getProperty("xlink.xlinkServerURL");
        } else {
            throw new Exception("XLinkUrl (parameter 'xlink.xlinkServerURL') must be set.");
        }
    }

}
