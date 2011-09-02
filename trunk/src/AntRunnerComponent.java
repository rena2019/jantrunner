import org.apache.tools.ant.BuildEvent;

/*
 * (c) by ReNa2019
 */

public interface AntRunnerComponent {
	public void setRunner(AntRunner runner);
	public String[] getTaskNames();
	public String getFilename();
	public void setFilename(String filename);
	public abstract void progress(int progress, ProgressState state, String info, BuildEvent event);
	public abstract void clearBuildStatus();
	public static enum ProgressState { STARTED, RUNNING, FINISHED }; 
}
