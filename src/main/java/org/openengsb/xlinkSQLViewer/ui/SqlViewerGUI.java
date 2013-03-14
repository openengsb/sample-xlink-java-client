package org.openengsb.xlinkSQLViewer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileSystemView;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.xlinkSQLViewer.SqlCreateViewer;
import org.openengsb.xlinkSQLViewer.exceptions.SQLFileNotWellFormedException;
import org.openengsb.xlinkSQLViewer.model.SQLCreateModel;
import org.openengsb.xlinkSQLViewer.parseUtils.SQLParseUtils;
import org.openengsb.xlinkSQLViewer.ui.helper.SingleRootFileSystemView;
import org.openengsb.xlinkSQLViewer.ui.helper.SqlFileFilter;
import org.openengsb.xlinkSQLViewer.xlink.OpenEngSBConnectionManager;
import org.openengsb.xlinkSQLViewer.xlink.matching.MatchingResult;

/**
 * TODO TBW
 */
@SuppressWarnings("serial")
public class SqlViewerGUI extends JFrame implements ClipboardOwner {

	/** Id of this View */
	public static final String viewId = "SQLView";
	/** Name of this View */
	public static final String viewName = "SQL Viewer";

	private static Logger logger = Logger.getLogger(SqlCreateViewer.class
			.getName());

	/* GUI Items */
	private JMenuBar menueLeiste;
	private JMenu file;
	private JMenuItem open;
	private JMenuItem close;

	private JMenuItem extractXLink;
	private JMenu triggerLocalSwitchMenu;
	private List<JMenuItem> currentLocalSwitchItems;
	private JPopupMenu popup;

	private JList sqlList;
	private JScrollPane listSCP;

	private JLabel header;

	/* Logical GUI Items */
	private JFileChooser fc;
	private CustomActionHandler customActionHandler;

	/* Supplied program arguments */
	private File workingDirectory;
	private String openEngSBContext;

	/** List of currently opened Statements */
	private List<SQLCreateModel> createStatements;

	/** Currently selected Statements */
	private SQLCreateModel selectedStmt;

	public SqlViewerGUI(File workingDirectory, String openEngSBContext) {
		super();
		this.openEngSBContext = openEngSBContext;
		this.workingDirectory = workingDirectory;
		initItems();
		buildWindow();
	}

	/**
	 * Init's the Items of the GUI
	 */
	private void initItems() {
		header = new JLabel("No File openend.");

		createStatements = new ArrayList<SQLCreateModel>();

		FileSystemView fsv = new SingleRootFileSystemView(workingDirectory);
		fc = new JFileChooser(fsv);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new SqlFileFilter());

		customActionHandler = new CustomActionHandler();

		currentLocalSwitchItems = new ArrayList<JMenuItem>();

		initPopupMenu();
		initMenu();
		buildList();
	}

	/**
	 * Sets up the Layout of the Window
	 */
	private void buildWindow() {

		this.setJMenuBar(menueLeiste);
		setLayout(new BorderLayout());

		JPanel listPanel = new JPanel();
		listPanel.add(listSCP);
		JScrollPane framescroller = new JScrollPane(listPanel);
		add(framescroller, BorderLayout.CENTER);

		JPanel headerPanel = new JPanel();
		headerPanel.add(header);
		add(headerPanel, BorderLayout.NORTH);

		int frameHeigth = 600;
		int frameWidht = 1000;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int top = (screenSize.height - frameHeigth) / 2;
		int left = (screenSize.width - frameWidht) / 2;

		setSize(frameWidht, frameHeigth);
		setLocation(left, top);
		setTitle("XLinkClient Java - SqlCreateViewer");
		setVisible(true);
	}

	/**
	 * Init's the Popup Menu for the List
	 */
	private void initPopupMenu() {
		popup = new JPopupMenu();
		extractXLink = new JMenuItem("XLink cannot be generated");
		extractXLink.setEnabled(false);
		extractXLink.addActionListener(customActionHandler);
		triggerLocalSwitchMenu = new JMenu("LocalSwitch not available");
		triggerLocalSwitchMenu.setEnabled(false);
		popup.add(extractXLink);
	}

	/**
	 * Init's the Menu for the MenuBar
	 */
	private void initMenu() {
		menueLeiste = new JMenuBar();

		file = new JMenu("File");

		menueLeiste.add(file);

		open = new JMenuItem("open SQLFile");
		open.addActionListener(customActionHandler);

		close = new JMenuItem("close program");
		close.addActionListener(customActionHandler);

		file.add(open);
		file.addSeparator();
		file.add(close);

	}

	/**
	 * Init's the List to display the opened SQL Statements in.
	 */
	private void buildList() {
		sqlList = new JList();
		sqlList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sqlList.setLayoutOrientation(JList.VERTICAL);
		sqlList.setVisibleRowCount(-1);
		listSCP = new JScrollPane(sqlList);
		listSCP.setPreferredSize(new Dimension(350, 500));
		sqlList.addMouseListener(new SqlListMouseListener());
	}

	/**
	 * Updates the Header Text if the given File exists
	 */
	private void updateHeader(File file) {
		if (createStatements.isEmpty()) {
			header.setText("No File openend.");
		} else {
			header.setText("SQL File " + file.getName() + " opened.");
		}
	}

	/**
	 * Updates the displayed List with the List of fetched Statements
	 */
	private void updateListFromStatements() {
		sqlList.removeAll();
		DefaultListModel listModel = new DefaultListModel();
		for (SQLCreateModel create : createStatements) {
			listModel.addElement(create);
		}
		sqlList.setModel(listModel);
	}

	public void openXLinkMatch(MatchingResult match) {
		createStatements = match.getCreateStatements();
		updateListFromStatements();
		updateHeader(match.getFileName());
		SQLCreateModel createStmt = (SQLCreateModel) sqlList.getModel()
				.getElementAt(match.getIndexOfMatch());
		sqlList.ensureIndexIsVisible(match.getIndexOfMatch());
		new SQLCreateDialog(this, createStmt);
	}

	/**
	 * Shows a Dialog with the given Message to inform the user that no Match
	 * could be found.
	 */
	public void showXLinkMissMatch(String msg) {
		JOptionPane.showMessageDialog(this, msg, "XLink Matching Failed.",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Changes the Status of the GUI to 'Connected with OpenEngSB and XLink'
	 */
	public void changeStatusToConnected() {
		extractXLink.setText("Extract XLink");
		extractXLink.setEnabled(true);
		if (fetchTemplate().getRegisteredTools().length > 0) {
			updateLocalSwitchMenu();
		}
	}

	/**
	 * TODO TBW
	 */
	public void updateLocalSwitchMenu() {
		if (fetchTemplate().getRegisteredTools().length > 0) {
			triggerLocalSwitchMenu.setText("Local Switching");
			triggerLocalSwitchMenu.setEnabled(true);
			triggerLocalSwitchMenu.removeAll();
			currentLocalSwitchItems = new ArrayList<JMenuItem>();

			XLinkConnector[] currentLocalTools = fetchTemplate()
					.getRegisteredTools();
			for (int i = 0; i < currentLocalTools.length; i++) {
				JMenu toolMenu = new JMenu(currentLocalTools[i].getToolName());
				triggerLocalSwitchMenu.add(toolMenu);
				XLinkConnectorView[] currentLocalToolViews = currentLocalTools[i]
						.getAvailableViews();
				for (int e = 0; e < currentLocalToolViews.length; e++) {
					JMenuItem newMenuItemOfTool = new JMenuItem(
							currentLocalToolViews[e].getViewId());
					String description = getDescriptionOfView(currentLocalToolViews[e]);
					newMenuItemOfTool.setToolTipText(description);
					newMenuItemOfTool.addActionListener(customActionHandler);
					currentLocalSwitchItems.add(newMenuItemOfTool);
				}
			}
		} else {
			triggerLocalSwitchMenu.setText("LocalSwitch not available");
			triggerLocalSwitchMenu.setEnabled(false);
		}
	}

	/**
	 * TODO TBW
	 */
	private String getDescriptionOfView(XLinkConnectorView view) {
		// TODO note that locale "en" is assumed
		String description = view.getDescriptions().get("en");
		if (description == null) {
			for (String dummyValue : view.getDescriptions().values()) {
				description = dummyValue;
				break;
			}
		}
		return description;
	}

	/**
	 * Adds the given Statement to the internal Datasource
	 */
	public void addData(SQLCreate model) {
		// Implement in real Program
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Do nothing
	}

	/**
	 * Notifies the User about the given Error with a Dialog.
	 */
	public void handleErrorVisualy(String msg, Exception ex) {
		JOptionPane.showMessageDialog(this, msg + " " + ex.getMessage(),
				"An Error Occured", JOptionPane.ERROR_MESSAGE);
		SqlCreateViewer.writeErrorWithoutExit(msg, ex);
	}

	/**
	 * MouseAdapter to handle the selection of a ListElement. Manages the
	 * PopupMenu and the Doubleclick to open the Statement's Details
	 */
	private class SqlListMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
				int index = sqlList.locationToIndex(e.getPoint());
				if (index >= 0) {
					selectedStmt = (SQLCreateModel) sqlList.getModel()
							.getElementAt(index);
					logger.log(Level.DEBUG, "Menu on: "
							+ selectedStmt.getTableName());
				}
			}
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int index = sqlList.locationToIndex(e.getPoint());
				SQLCreateModel createStmt = (SQLCreateModel) sqlList.getModel()
						.getElementAt(index);
				sqlList.ensureIndexIsVisible(index);
				new SQLCreateDialog(SqlViewerGUI.this, createStmt);
			}
		}
	}

	/**
	 * Actionhandler that manages the input from the Menubar and the PopUpMenu
	 */
	private class CustomActionHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == open) {
				int returnVal = fc.showOpenDialog(SqlViewerGUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						createStatements = SQLParseUtils
								.parseCreateStatementsFromFile(file);
					} catch (FileNotFoundException e1) {
						handleErrorVisualy("Error while reading SQLFile.", e1);
					} catch (IOException e2) {
						handleErrorVisualy("Error while reading SQLFile.", e2);
					} catch (SQLFileNotWellFormedException e3) {
						handleErrorVisualy("Error while reading SQLFile.", e3);
					}
					updateListFromStatements();
					updateHeader(file);
				}
			}
			if (e.getSource() == close) {
				getToolkit().getSystemEventQueue().postEvent(
						new WindowEvent(SqlViewerGUI.this,
								WindowEvent.WINDOW_CLOSING));
			}
			if (e.getSource() == extractXLink) {
				if (SqlCreateViewer.isConnected()) {
					if (selectedStmt != null) {
						StringSelection stringSelection;
						try {
							stringSelection = new StringSelection(SQLParseUtils
									.genereateXLink(selectedStmt,
											openEngSBContext));
							Clipboard clipboard = Toolkit.getDefaultToolkit()
									.getSystemClipboard();
							clipboard.setContents(stringSelection,
									(ClipboardOwner) SqlViewerGUI.this);
						} catch (Exception ex) {
							handleErrorVisualy("Error during XLink creation.",
									ex);
						}
					}
				} else {
					JOptionPane
							.showMessageDialog(
									SqlViewerGUI.this,
									"No Connection",
									"Connection to OpenEngSb has not been established.",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}
			for (JMenuItem localSwitchItem : currentLocalSwitchItems) {
				if (SqlCreateViewer.isConnected()) {
					if (e.getActionCommand() == localSwitchItem.getText()) {
						// TODO note assume that all viewIds are unique
						String viewToSwitchTo = e.getActionCommand();
						boolean success = false;
						try {
							success = SQLParseUtils.triggerLocalSwitch(
									selectedStmt, openEngSBContext,
									viewToSwitchTo);
						} catch (Exception ex) {
							handleErrorVisualy("Error during XLink creation.",
									ex);
							return;
						}
						if (success) {
							JOptionPane
									.showMessageDialog(
											SqlViewerGUI.this,
											"LocalSwitch successfull",
											"A local switch has been successfully triggered.",
											JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane
									.showMessageDialog(
											SqlViewerGUI.this,
											"LocalSwitch failed",
											"A local switch was triggered, but failed.",
											JOptionPane.INFORMATION_MESSAGE);
						}
					}
				} else {
					JOptionPane
							.showMessageDialog(
									SqlViewerGUI.this,
									"No Connection",
									"Connection to OpenEngSb has not been established.",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
	}

	/**
	 * Fetches the XLinkUrlBlueprint from the ConnectorManager
	 */
	private static XLinkUrlBlueprint fetchTemplate() {
		return OpenEngSBConnectionManager.getInstance().getBluePrint();
	}
}
