/*
 * (c) by ReNa2019
 */

public interface AntRunnerComponent {
	public void setRunner(AntRunner runner);
	public String[] getTaskNames();
	public String getFilename();
	public void setFilename(String filename);
}
