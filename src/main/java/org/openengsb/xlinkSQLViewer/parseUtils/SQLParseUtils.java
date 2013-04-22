package org.openengsb.xlinkSQLViewer.parseUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.domain.SQLCode.model.SQLCreateField;
import org.openengsb.xlinkSQLViewer.exceptions.GenerateXLinkException;
import org.openengsb.xlinkSQLViewer.exceptions.SQLFileNotWellFormedException;
import org.openengsb.xlinkSQLViewer.model.SQLCreateModel;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;
import org.openengsb.xlinkSQLViewer.xlink.OpenEngSBConnectionManager;

/**
 * Utils class, offering methods for SQL FileParsing
 */
public final class SQLParseUtils {

    private SQLParseUtils() {
    }

    /**
     * Returns all Contained SQL CreateStatements from the given file.<br/>
     * An Exception is thrown if a SQL CreateStatement was not well formed.
     */
    public static List<SQLCreateModel> parseCreateStatementsFromFile(File file)
            throws IOException, SQLFileNotWellFormedException {
        List<SQLCreateModel> newStatements = new ArrayList<SQLCreateModel>();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String inputLine = null;
        String createBody = "";
        String createName = "";
        boolean startedCreate = false;

        String createTableRegEx = " *CREATE *TABLE * ([a-zA-Z]+) *";
        Pattern createTablePattern = Pattern.compile(createTableRegEx);
        Matcher createTableMatcher;

        String openCreateBodyRegEx = "^\\($";
        Pattern openCreateBodyPattern = Pattern.compile(openCreateBodyRegEx);

        String endCreateRegEx = "^\\);$";
        Pattern endCreatePattern = Pattern.compile(endCreateRegEx);

        while ((inputLine = br.readLine()) != null) {
            String inputString = inputLine.trim();// .toLowerCase();

            if ((createTableMatcher = createTablePattern.matcher(inputString))
                    .find()) {
                if (startedCreate) {
                    throw new SQLFileNotWellFormedException(
                            "Create Statement "
                                    + inputLine
                                    + " was openend before the last statement was closed.");
                }
                createName = createTableMatcher.group(1);
                createBody = "";
                startedCreate = true;

            } else if (endCreatePattern.matcher(inputString).find()) {
                if (!startedCreate) {
                    throw new SQLFileNotWellFormedException(
                            "A create Statement is closed but no statement was opened.");
                }
                startedCreate = false;
                newStatements.add(new SQLCreateModel(createName, createBody));
            } else if (openCreateBodyPattern.matcher(inputString).find()) {
                continue;
            } else {
                if (startedCreate) {
                    if (inputLine.endsWith(",")) {
                        inputLine = inputLine.substring(0,
                                inputLine.length() - 1);
                        inputLine = inputLine.trim();
                    }
                    createBody += inputLine + "\n";
                }
            }
        }
        br.close();
        fr.close();
        return newStatements;
    }

    /**
     * Generates an XLink URL out of the given SQLCreateModel
     */
    public static String genereateXLink(SQLCreateModel selectedStmt,
            String openEngSBContext) throws JsonGenerationException,
            JsonMappingException, IOException, GenerateXLinkException {
        ModelDescription modelInformation = fetchTemplate().getViewToModels()
                .get(SqlViewerGUI.viewId);
        String definedClass = modelInformation.getModelClassName();
        /* Note that only the target class SQLCreate is allowed */
        if (!definedClass.equals(SQLCreate.class.getName())) {
            throw new GenerateXLinkException("XLinkError - Targetmodel '"
                    + SQLCreate.class.getName() + "' not supported");
        }
        String completeUrl = fetchTemplate().getBaseUrl();
        completeUrl += "&"
                + fetchTemplate().getKeyNames().getModelClassKeyName() + "="
                + urlEncodeParameter(modelInformation.getModelClassName());
        completeUrl += "&"
                + fetchTemplate().getKeyNames().getModelVersionKeyName() + "="
                + urlEncodeParameter(modelInformation.getVersionString());
        completeUrl += "&"
                + fetchTemplate().getKeyNames().getContextIdKeyName() + "="
                + urlEncodeParameter(openEngSBContext);

        SQLCreate emtpyCreate = new SQLCreate();
        emtpyCreate.setTableName(selectedStmt.getTableName());
        emtpyCreate.setFields(parseFieldsOutOfCreateBody(selectedStmt
                .getCreateBody()));

        ObjectMapper mapper = new ObjectMapper();
        String objectString = mapper.writeValueAsString(emtpyCreate);
        completeUrl += "&"
                + fetchTemplate().getKeyNames().getIdentifierKeyName() + "="
                + urlEncodeParameter(objectString);
        return completeUrl;
    }

    /**
     * Triggers a local Switch request to the given SQL statemen, destined connector and viewid.
     */
    public static boolean triggerLocalSwitch(SQLCreateModel selectedStmt,
            String openEngSBContext, String connectorId, String viewId)
            throws JsonGenerationException, JsonMappingException, IOException,
            GenerateXLinkException {
        String xlink = genereateXLink(selectedStmt, openEngSBContext);
        // TODO remove Hack (hardcoded ConnectorIdKeyname) after correct
        // implementation at OpenEngSB
        xlink += "&" + "connectorId=" + connectorId + "&"
                + fetchTemplate().getKeyNames().getViewIdKeyName() + "="
                + urlEncodeParameter(viewId);
        URL url = new URL(xlink);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return true;
        }
        System.out.println("Error during XLink, StatusCode "
                + conn.getResponseCode() + " returned.");
        return false;
    }

    /**
     * Returns the SQLFields from the given String
     */
    private static SQLCreateField[] parseFieldsOutOfCreateBody(String createBody) {
        List<SQLCreateField> fields = new ArrayList<SQLCreateField>();
        List<String> attributeStrings = Arrays.asList(createBody.split("\n"));

        String attributeMatcher = "([a-zA-Z_]+) ([a-zA-Z]+\\([0-9]+\\)|[a-zA-Z]+)[ ]*(.*)";
        Pattern pattern = Pattern.compile(attributeMatcher);

        for (String attributeString : attributeStrings) {
            attributeString = attributeString.trim();
            Matcher matcher = pattern.matcher(attributeString);
            if (matcher.find()) {
                fields.add(new SQLCreateField(matcher.group(1), matcher
                        .group(2), findConstraints(matcher.group(3))));
            }
        }

        return fields.toArray(new SQLCreateField[0]);
    }

    /**
     * Returns the Constraints from the given String
     */
    private static String[] findConstraints(String constraintString) {
        List<String> constraints = new ArrayList<String>();
        if (constraintString.contains("NOT NULL")) {
            constraints.add("NOT NULL");
        }
        if (constraintString.contains("PRIMARY KEY")) {
            constraints.add("PRIMARY KEY");
        }
        String foreingKeyMatchString = ".*(REFERENCES [a-zA-Z]+\\([a-zA-Z]+\\)).*";
        Pattern foreingKeyPattern = Pattern.compile(foreingKeyMatchString);
        Matcher foreingKeyMatcher = foreingKeyPattern.matcher(constraintString);
        if (foreingKeyMatcher.matches()) {
            constraints.add(foreingKeyMatcher.group(1));
        }
        return constraints.toArray(new String[0]);
    }

    /**
     * URLEncodes the given Parameter in UTF-8
     */
    private static String urlEncodeParameter(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return parameter;
    }

    /**
     * Fetches the XLinkUrlBlueprint from the ConnectorManager
     */
    private static XLinkUrlBlueprint fetchTemplate() {
        return OpenEngSBConnectionManager.getInstance().getBluePrint();
    }

}
