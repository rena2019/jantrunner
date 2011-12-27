/*
 * (c) by ReNa2019
 * 
 */
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

public class ExecuteThread extends Thread implements BuildListener {

	private AntRunnerComponent comp;
	private AntRunner antrunner;
	private String filename;
	private String target;
	private int percent = 0;
	private JButton button = null;
	private double lastDuration = 0;
	Timer timer = null;
	private Date dateStart;

	ExecuteThread(AntRunner antrunner, AntRunnerComponent comp, JButton button) {
		this.antrunner = antrunner;
		this.comp = comp;
		this.button = button;
	}
	
	private class ProgressTimerTask extends TimerTask {
		private String filename;
		private String target;
		//private long delay;

		ProgressTimerTask(String filename, String target) {
			dateStart = new Date();
			this.filename = filename;
			this.target = target;
			try {
				lastDuration = antrunner.getStatistics().getLastDuration(filename, target);
				//System.out.println("lastDuration: " + lastDuration);
				antrunner.addToLog("lastDuration: " + lastDuration, Project.MSG_DEBUG);
			} catch (Exception e) {
				lastDuration = 0;
			}
		}

		public void run() {	
			if (lastDuration != 0) {
				percent = (int)((new Date().getTime() - dateStart.getTime()));
				percent = (int)(percent  / 10.0 / lastDuration);
				if (percent > 95)
					percent = 95;
			}
			//System.out.println("percent: " + percent);
			updateProgress(filename, target, (int)percent);
			
		}
		
	}//ProgressTimerTask

	public void run() {
		boolean errorOccured = false;
		if (button != null)
			antrunner.clearButtonBgColor(button);
		comp.clearBuildStatus();
		for (String targetname : comp.getTaskNames()) {
			String filename = comp.getFilename();
			//start update timer
			ProgressTimerTask task = new ProgressTimerTask(filename, targetname);
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(task, 0, 500);
			errorOccured = !antrunner.executeAntTarget(filename,
					targetname, this);
			timer.cancel();
			if (errorOccured)
				break;
		}
		// set background color
		if (button != null)
			antrunner.setButtonBgColor(button, !errorOccured);
	}// run
	
	private void updateProgress(String filename, String target, int percent) {
		//TODO
		this.percent = percent;
		comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
				"TODO updateProgress", null);
		//System.out.print("updateProgress: " + filename + ", " + target);
	}

	//@Override
	public void buildFinished(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 100;
		comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED,
				"TODO", arg0);
	}

	//@Override
	public void buildStarted(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 0;
		comp.progress(percent, AntRunnerComponent.ProgressState.STARTED,
				"TODO", arg0);
	}

	//@Override
	public void messageLogged(BuildEvent arg0) {
		// TODO Auto-generated method stub
		// NO MESS
		// percent += 1;
		// comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
		// "TODO messageLogged", arg0);
	}

	//@Override
	public void targetFinished(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 100;
		comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED,
				"TODO targetFinished", arg0);
	}

	//@Override
	public void targetStarted(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 0;
		comp.progress(percent, AntRunnerComponent.ProgressState.STARTED,
				"TODO targetStarted", arg0);
	}

	//@Override
	public void taskFinished(BuildEvent arg0) {
		// TODO timer
		/*percent += 1;
		comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
				"TODO taskFinished", arg0);*/
	}

	//@Override
	public void taskStarted(BuildEvent arg0) {
		// TODO timer
		/*percent += 1;
		comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
				"TODO taskStarted", arg0);*/
	}
}// ExecuteThread
