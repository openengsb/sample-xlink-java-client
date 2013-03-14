package org.openengsb.xlinkSQLViewer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.openengsb.xlinkSQLViewer.model.SQLCreateModel;

/**
 * Simple Dialog which enables the User to insert new CreateStatements
 */
@SuppressWarnings("serial")
public class SQLCreateDialog extends JDialog implements ActionListener,
		KeyListener {

	private SQLCreateModel createItem;
	private JFrame frame;

	private JButton yesButton;
	private JButton noButton;
	private JTextField tableName;
	private JTextArea tableAttrs;

	public SQLCreateDialog(JFrame frame, SQLCreateModel createItem) {
		super(frame, "SqlCreate Dialog", true);
		this.createItem = createItem;
		this.frame = frame;
		buildDialog();
	}

	private void buildDialog() {
		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel);

		contentPanel.setLayout(new MigLayout());
		contentPanel.add(new JLabel("create Table: "));
		tableName = new JTextField(25);
		tableName.setText(createItem.getTableName());
		tableName.addKeyListener(this);
		tableName.setEnabled(false);
		contentPanel.add(tableName, "wrap");

		tableAttrs = new JTextArea(15, 35);
		tableAttrs.setText(createItem.getCreateBody());
		tableAttrs.addKeyListener(this);
		tableAttrs.setEnabled(false);
		JScrollPane scp = new JScrollPane(tableAttrs);
		contentPanel.add(scp, "span 2, wrap");

		yesButton = new JButton("Save");
		yesButton.addActionListener(this);
		// contentPanel.add(yesButton,"align left");

		noButton = new JButton("Close");
		noButton.addActionListener(this);
		contentPanel.add(noButton, "align right, wrap");

		addWindowListener(new WA());
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (yesButton == e.getSource()) {
			setVisible(false);
		} else if (noButton == e.getSource()) {
			setVisible(false);
		}
	}

	private class WA extends WindowAdapter {
		public void windowClosing(WindowEvent ev) {
			setVisible(false);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_ENTER) {
			// yesButton.doClick();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
