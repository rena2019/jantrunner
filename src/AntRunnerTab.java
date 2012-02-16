/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
					DefaultListModel model = ((DefaultListModel) antrunner
							.getBatchList().getModel());
					for (String taskname : ((AntRunnerComponent) comp).getTaskNames()) {
							if (!(new File(file)).exists())
								file = new File(file).getAbsolutePath(); // TODO
																			// current
																			// path?
							model.add(model.size(), file + ";" + taskname);
					}
					// select last
					antrunner.getBatchList().setSelectedIndex(model.size() - 1);
				}

			} else if (event.getSource() == btnExecute) {
				antrunner.executeSelectedTargets((AntRunnerComponent)comp, btnExecute);
				}
			}
	};

	AntRunnerTab(AntRunner runner, JTabbedPane tabbedPane, String name,
			String type, String file, String source) {
		super(new BorderLayout());
		antrunner = runner;
		//use filename if tab name not set
		if (name.equals(""))
			name = (new File(file)).getName();
		tabbedPane.addTab(name, this);
		this.type = type;
		if ((type.equals("TaskList") || type.equals("")) && source.equals("")) {
			comp = new AntTaskList(file);
			((AntRunnerComponent) comp).setRunner(runner);
			((AntRunnerComponent) comp).setFilename(file);
			this.add(new JScrollPane(comp), BorderLayout.CENTER);
		} else {
			try {
				if (source.equals("")) {
					comp = (JComponent) Class.forName(type).newInstance();
				} else {
					//bsh file
					Interpreter i = new Interpreter();
					comp = (JComponent) i.source(source);
				}
				((AntRunnerComponent) comp).setRunner(runner);
				((AntRunnerComponent) comp).setFilename(file);
				this.add(new JScrollPane(comp), BorderLayout.CENTER);
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}

		JPanel panelButtons = new JPanel(new FlowLayout());
		btnExecute = new JButton("Execute");
		panelButtons.add(btnExecute);
		btnAddToBatch = new JButton("Add to Batch");
		panelButtons.add(btnAddToBatch);
		btnAddToBatch.addActionListener(buttonHandler);

		this.add(panelButtons, BorderLayout.SOUTH);
		btnExecute.addActionListener(buttonHandler);
	}

	// TODO not public -> implement d&d handler
	AntTaskList getTaskList() {
		return (AntTaskList) comp;
	}
}// AntRunnerTab
