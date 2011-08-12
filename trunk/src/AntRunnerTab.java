/*
 * (c) by ReNa2019
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
				if (comp instanceof AntTaskList) {
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
					/*
					 * int[] idx = ((TaskList)comp).getSelectedIndices();
					 * DefaultListModel model = ((DefaultListModel)
					 * lstAntBatch.getModel()); for(int i=0; i < idx.length;
					 * i++) { String task = ((DefaultListModel)taskList.getModel
					 * ()).get(idx[i]).toString(); String file =
					 * taskList.getFilename(); if (!(new File(file)).exists())
					 * file = new File(file).getAbsolutePath(); //TODO current
					 * path? model.add(model.size(), file + ";" + task); }
					 */
				} else if (comp instanceof AntRunnerComponent) {
					String file = ((AntRunnerComponent) comp).getFilename();
					String taskname = ((AntRunnerComponent) comp)
							.getTaskNames()[0];
					DefaultListModel model = ((DefaultListModel) antrunner
							.getBatchList().getModel());
					if (!(new File(file)).exists())
						file = new File(file).getAbsolutePath(); // TODO
																	// current
																	// path?
					model.add(model.size(), file + ";" + taskname);
					// select
					antrunner.getBatchList().setSelectedIndex(model.size() - 1);
				}

			} else if (event.getSource() == btnExecute) {
				if (comp instanceof AntTaskList) {
					String file = ((AntTaskList) comp).getFilename();
					// batch list on the right side
					DefaultListModel model = ((DefaultListModel) antrunner
							.getBatchList().getModel());
					for (String taskname : ((AntTaskList) comp).getTaskNames()) {
						antrunner.addToLog("execute " + taskname + " (" + file
								+ ")");
						antrunner.clearButtonBgColor((JButton) event
								.getSource());
						antrunner.setButtonBgColor((JButton) event.getSource(),
								antrunner.executeAntTarget(
										new File(file).getAbsolutePath(),
										taskname));
						antrunner.addToLog("execute " + taskname + " (" + file
								+ ")" + " done");
					}
					/*
					 * //execute selected tasks int[] idx =
					 * taskList.getSelectedIndices(); for(int i=0; i <
					 * idx.length; i++) { String task =
					 * ((DefaultListModel)taskList
					 * .getModel()).get(idx[i]).toString(); //String file =
					 * s.substring(0, s.indexOf(";")); String file =
					 * taskList.getFilename(); //String task =
					 * s.substring(s.indexOf(";") + 1); addToLog("execute " +
					 * task + " (" + file + ")");
					 * clearButtonBgColor((JButton)event.getSource());
					 * setButtonBgColor((JButton)event.getSource(),
					 * executeAntTarget(new File(file).getAbsolutePath(),
					 * task)); addToLog("execute " + task + " (" + file + ")" +
					 * " done"); }
					 */
				} else {
					String file = ((AntRunnerComponent) comp).getFilename();
					// batch list on the right side
					DefaultListModel model = ((DefaultListModel) antrunner
							.getBatchList().getModel());
					for (String taskname : ((AntRunnerComponent) comp)
							.getTaskNames()) {
						antrunner.addToLog("execute " + taskname + " (" + file
								+ ")");
						antrunner.clearButtonBgColor((JButton) event
								.getSource());
						antrunner.setButtonBgColor((JButton) event.getSource(),
								antrunner.executeAntTarget(
										new File(file).getAbsolutePath(),
										taskname));
						antrunner.addToLog("execute " + taskname + " (" + file
								+ ")" + " done");
					}
				}

			}
		}
	};

	AntRunnerTab(AntRunner runner, JTabbedPane tabbedPane, String name,
			String type, String file, String source) {
		super(new BorderLayout());
		antrunner = runner;
		tabbedPane.addTab(name, this);
		this.type = type;
		if (type.equals("TaskList")) {
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
