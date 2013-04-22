package org.openengsb.xlinkSQLViewer.ui.helper;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openengsb.xlinkSQLViewer.SqlCreateViewer;
import org.openengsb.xlinkSQLViewer.parseUtils.SQLParseUtils;
import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;

/**
 * Class to define action of LocalSwitch Popup menu items. Triggers a local switch with the data of the selected item.
 */
@SuppressWarnings("serial")
public class LocalSwitchAction extends AbstractAction {

    private SqlViewerGUI gui;
    private String destinedConnectorId;
    private String viewToswitchTo;

    public LocalSwitchAction(String name, SqlViewerGUI gui,
            String destinedConnectorId, String viewToswitchTo) {
        super(name);
        this.gui = gui;
        this.destinedConnectorId = destinedConnectorId;
        this.viewToswitchTo = viewToswitchTo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (SqlCreateViewer.isConnected()) {
            boolean success = false;
            try {
                success = SQLParseUtils.triggerLocalSwitch(gui
                        .getSelectedStmt(), gui.getOpenEngSBContext(),
                        destinedConnectorId, viewToswitchTo);
            } catch (Exception ex) {
                gui.handleErrorVisualy("Error during XLink creation.", ex);
                return;
            }
            if (success) {
                JOptionPane.showMessageDialog(gui, "LocalSwitch successfull",
                        "A local switch has been successfully triggered.",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(gui, "LocalSwitch failed",
                        "A local switch was triggered, but failed.",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(gui, "No Connection",
                    "Connection to OpenEngSb has not been established.",
                    JOptionPane.INFORMATION_MESSAGE);
        }

    }

}
