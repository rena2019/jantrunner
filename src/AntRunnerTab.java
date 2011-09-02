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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

import bsh.Interpreter;

public class AntRunnerTab extends JPanel {
	
	private class ExecThread extends Thread implements BuildListener {

		private AntRunnerComponent comp;
		private String filename;
		private String target;
		private int percent = 0;

		ExecThread(AntRunnerComponent comp) {
			this.comp = comp;
		}

		/*
		 * ExecThread(String threadName) { super(threadName); // Initialize thread.
		 * start(); }
		 */
		public void run() {
			comp.clearBuildStatus();
			for (String taskname : comp.getTaskNames()) {
				if (!antrunner.executeAntTarget(
							comp.getFilename(),
							taskname, this))
						break;
			
		}
	}//run

		@Override
		public void buildFinished(BuildEvent arg0) {
			// TODO Auto-generated method stub
			percent = 100;
			comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED, "TODO", arg0);
		}

		@Override
		public void buildStarted(BuildEvent arg0) {
			// TODO Auto-generated method stub
			percent = 0;
			comp.progress(percent, AntRunnerComponent.ProgressState.STARTED, "TODO", arg0);
		}

		@Override
		public void messageLogged(BuildEvent arg0) {
			// TODO Auto-generated method stub
			//NO MESS
			//percent += 1;
			//comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING, "TODO messageLogged", arg0);
		}

		@Override
		public void targetFinished(BuildEvent arg0) {
			// TODO Auto-generated method stub
			percent = 100;
			comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED, "TODO targetFinished", arg0);
		}

		@Override
		public void targetStarted(BuildEvent arg0) {
			// TODO Auto-generated method stub
			percent = 0;
			comp.progress(percent, AntRunnerComponent.ProgressState.STARTED, "TODO targetStarted", arg0);
		}

		@Override
		public void taskFinished(BuildEvent arg0) {
			// TODO timer
			percent += 5;
			comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING, "TODO taskFinished", arg0);
		}

		@Override
		public void taskStarted(BuildEvent arg0) {
			// TODO timer
			percent += 5;
			comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING, "TODO taskStarted", arg0);
		}
	}//ExecThread
	
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
				//execute task
				if (comp instanceof AntTaskList) {
					
					/*String file = ((AntTaskList) comp).getFilename();
					// batch list on the right side
					DefaultListModel model = ((DefaultListModel) antrunner
							.getBatchList().getModel());
					for (String taskname : ((AntTaskList) comp).getTaskNames()) {
						//TODO log move to antrunner?
						//antrunner.addToLog("execute " + taskname + " (" + file
						//		+ ")");
						antrunner.clearButtonBgColor((JButton) event
								.getSource());
						antrunner.setButtonBgColor((JButton) event.getSource(),
								antrunner.executeAntTarget(
										new File(file).getAbsolutePath(),
										taskname, (BuildListener)comp));
						//antrunner.addToLog("execute " + taskname + " (" + file
						//		+ ")" + " done");*/
						ExecThread t = new ExecThread((AntRunnerComponent)comp);
						t.start();
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
		if (type.equals("TaskList") || type.equals("")) {
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
