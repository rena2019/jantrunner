/*
 * (c) by ReNa2019
 * 
 */
import javax.swing.JButton;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

public class ExecuteThread extends Thread implements BuildListener {

	private AntRunnerComponent comp;
	private AntRunner antrunner;
	private String filename;
	private String target;
	private int percent = 0;
	private JButton button = null;

	ExecuteThread(AntRunner antrunner, AntRunnerComponent comp, JButton button) {
		this.antrunner = antrunner;
		this.comp = comp;
		this.button = button;
	}

	public void run() {
		boolean errorOccured = false;
		if (button != null)
			antrunner.clearButtonBgColor(button);
		comp.clearBuildStatus();
		for (String taskname : comp.getTaskNames()) {
			if (errorOccured |= !antrunner.executeAntTarget(comp.getFilename(),
					taskname, this))
				break;
		}
		// set background color
		if (button != null)
			antrunner.setButtonBgColor(button, !errorOccured);
	}// run

	@Override
	public void buildFinished(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 100;
		comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED,
				"TODO", arg0);
	}

	@Override
	public void buildStarted(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 0;
		comp.progress(percent, AntRunnerComponent.ProgressState.STARTED,
				"TODO", arg0);
	}

	@Override
	public void messageLogged(BuildEvent arg0) {
		// TODO Auto-generated method stub
		// NO MESS
		// percent += 1;
		// comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
		// "TODO messageLogged", arg0);
	}

	@Override
	public void targetFinished(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 100;
		comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED,
				"TODO targetFinished", arg0);
	}

	@Override
	public void targetStarted(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 0;
		comp.progress(percent, AntRunnerComponent.ProgressState.STARTED,
				"TODO targetStarted", arg0);
	}

	@Override
	public void taskFinished(BuildEvent arg0) {
		// TODO timer
		percent += 5;
		comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
				"TODO taskFinished", arg0);
	}

	@Override
	public void taskStarted(BuildEvent arg0) {
		// TODO timer
		percent += 5;
		comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
				"TODO taskStarted", arg0);
	}
}// ExecuteThread
