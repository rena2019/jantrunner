/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

import bsh.Interpreter;

public class AntRunnerTab extends JPanel {
	
	// TaskList taskList;
	JComponent comp;
	JButton btnExecute;
	JButton btnAddToBatch;
	// JList lstBatch;
	String type = "";
	AntRunner antrunner;
	ButtonPanel cmdButtonPanel;
	// button handler for execute actions
	ActionListener buttonHandler = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == btnAddToBatch) {
				// add selected task to batch file
				/*if (comp instanceof AntTaskList) {
					String file = ((AntTaskList) comp).getFilename();
					// batch list on the right side
					DefaultListModel model = ((DefaultListModel) antrunner
							.getBatchList().getModel());
					for (String taskname : ((AntTaskList) comp).getTaskNames()) {
						if (!(new File(file)).exists())
							file = new File(file).getAbsolutePath(); // TODO
																		// current
																		// path?
						model.add(model.size(), file + ";" + taskname);
					}
				} else*/ if (comp instanceof AntRunnerComponent) {
					String file = ((AntRunnerComponent) comp).getFilename();
					if (file != null) {
						for (String taskname : ((AntRunnerComponent) comp).getTaskNames()) {
							antrunner.addToBatchList(new AntTarget(new File(file).getAbsolutePath(), taskname));
						}
					}
				}

			} else if (event.getSource() == btnExecute) {
				antrunner.executeSelectedTargets((AntRunnerComponent)comp, btnExecute);
				}
			}
	};

	AntRunnerTab(AntRunner runner, JTabbedPane tabbedPane, String name,
			String type, String file, String source, ButtonPanel buttonPanel) {
		super(new BorderLayout());
		antrunner = runner;
		cmdButtonPanel = buttonPanel;
		new DropTargetListener(cmdButtonPanel);
		//use filename if tab name not set
		if (name.equals(""))
			name = (new File(file)).getName();
		tabbedPane.addTab(name, this);
		this.type = type;
		if ((type.equals("TaskList") || type.equals("")) && source.equals("")) {
			//create new ant target list
			comp = new AntTaskList(file);
			((AntRunnerComponent) comp).setRunner(runner);
			((AntRunnerComponent) comp).setFilename(file);
			this.add(new JScrollPane(comp), BorderLayout.CENTER);
			//add drag&drop handling
			DragSource ds = new DragSource();
			ds.createDefaultDragGestureRecognizer(comp, DnDConstants.ACTION_COPY,
					cmdButtonPanel);
		} else if (type.equals("TreeList")) {
			comp = new AntTreeList(file);
			((AntRunnerComponent) comp).setRunner(runner);
			((AntRunnerComponent) comp).setFilename(file);
			this.add(new JScrollPane(comp), BorderLayout.CENTER);
			//add drag&drop handling
			DragSource ds = new DragSource();
			ds.createDefaultDragGestureRecognizer(comp, DnDConstants.ACTION_COPY,
					cmdButtonPanel);
		}
		 else {
			try {
				if (source.equals("")) {
					comp = (JComponent) Class.forName(type).newInstance();
				} else {
					//bsh file
					Interpreter i = new Interpreter();
					comp = (JComponent) i.source(source);
				}
				if (comp instanceof AntRunnerComponent) {
					((AntRunnerComponent) comp).setRunner(runner);
					((AntRunnerComponent) comp).setFilename(file);
					this.add(new JScrollPane(comp), BorderLayout.CENTER);
				} else if (comp instanceof AntRunnerConfig) {
					((AntRunnerConfig) comp).setRunner(runner);
					((AntRunnerConfig) comp).setFilename(file);
					this.add(new JScrollPane(comp), BorderLayout.CENTER);
				}
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}
		if (comp instanceof AntRunnerComponent) {

		JPanel panelButtons = new JPanel(new FlowLayout());
		btnExecute = new JButton("Execute");
		panelButtons.add(btnExecute);
		btnAddToBatch = new JButton("Add to Batch");
		panelButtons.add(btnAddToBatch);
		btnAddToBatch.addActionListener(buttonHandler);

		this.add(panelButtons, BorderLayout.SOUTH);
		btnExecute.addActionListener(buttonHandler);
		}
	}

	// TODO not public -> implement d&d handler
	AntTaskList getTaskList() {
		return (AntTaskList) comp;
	}
}// AntRunnerTab
