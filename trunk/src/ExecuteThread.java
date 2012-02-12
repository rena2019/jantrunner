/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 * 
 */
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

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
	private double averageDuration = 0;
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
		//private Date lastProgress;
		//private long delay;

		/**
		 * This timer class is periodically triggered to update the progress info
		 */
		ProgressTimerTask(String filename, String target) {
			dateStart = new Date();
			this.filename = filename;
			this.target = target;
			try {
				averageDuration = antrunner.getStatistics().getAverage(filename, target);
			} catch (Exception e) {
				averageDuration = 0;
			}
		}

		public void run() {	
			if (averageDuration != 0) {
				percent = (int)((new Date().getTime() - dateStart.getTime()));
				percent = (int)(percent  / 10.0 / averageDuration);
			} else {
				percent++;
			}
			if (percent > 95)
				percent = 95;
			//System.out.println("percent: " + percent);
			int leftDuration = (int)(averageDuration - ((new Date().getTime() - dateStart.getTime()) / 1000));
			String status = "";
			if (leftDuration < 0)
				status += "overdue: ";
			else
				status += "left: ";
			status +=  formatDuration(Math.abs(leftDuration*1000), true);
			updateProgress(filename, target, (int)percent, status);
			
		}
		
		public String formatDuration(long millis, boolean hideZeroValues)
	    {
	        long days = TimeUnit.MILLISECONDS.toDays(millis);
	        millis -= TimeUnit.DAYS.toMillis(days);
	        long hours = TimeUnit.MILLISECONDS.toHours(millis);
	        millis -= TimeUnit.HOURS.toMillis(hours);
	        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
	        millis -= TimeUnit.MINUTES.toMillis(minutes);
	        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

	        StringBuilder sb = new StringBuilder(64);
	        boolean addAllOthers = false;
	        if ((!hideZeroValues && days == 0) || (hideZeroValues && days > 0)) {
	        	sb.append(days);
	        	sb.append(" days ");
	        	addAllOthers = true;
	        }
	        if ((!hideZeroValues  && hours == 0) || (hideZeroValues  && hours > 0) || addAllOthers) {
	        	sb.append(hours);
		        sb.append(" hours ");
	        }
	        if ((!hideZeroValues && minutes == 0)  || (hideZeroValues && minutes > 0) || addAllOthers) {
	        	sb.append(minutes);
	        	sb.append(" min ");
	        }
	        sb.append(seconds);
	        sb.append(" sec");

	        return(sb.toString());
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
	
	private void updateProgress(String filename, String target, int percent, String info) {
		this.percent = percent;
		comp.progress(percent, AntRunnerComponent.ProgressState.RUNNING,
				info, null);
		//System.out.print("updateProgress: " + filename + ", " + target);
	}

	//@Override
	public void buildFinished(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 100;
		comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED, "", arg0);
	}

	//@Override
	public void buildStarted(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 0;
		comp.progress(percent, AntRunnerComponent.ProgressState.STARTED, "", arg0);
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
		comp.progress(percent, AntRunnerComponent.ProgressState.FINISHED, "", arg0);
	}

	//@Override
	public void targetStarted(BuildEvent arg0) {
		// TODO Auto-generated method stub
		percent = 0;
		comp.progress(percent, AntRunnerComponent.ProgressState.STARTED, "", arg0);
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
