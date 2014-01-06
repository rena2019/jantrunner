/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;


class XThread extends Thread {

	private AntTaskList tasklist;
	private String filename;

	XThread(AntTaskList tasklist, String filename) {
		this.tasklist = tasklist;
		this.filename = filename;
	}

	/*
	 * XThread(String threadName) { super(threadName); // Initialize thread.
	 * start(); }
	 */
	public void run() {
		// Display info about this particular thread
		tasklist.addAntTasksToList(filename);
		//tasklist.setSelectedIndex(0);
	}
}

class AntTaskList extends JList implements AntRunnerComponent, MouseListener /*, BuildListener*/ {

	private String filename = "";
	private AntRunner antrunner;
	private String running_target = "";
	private int[] progress;
	private boolean[] passed;
	private int running_target_index=0;
	private String status = "";

	// constructor
	AntTaskList(String filename) {
		super(new DefaultListModel());
		if (filename != null) {
			this.filename = filename;
			XThread t = new XThread(this, filename);
			t.start();
		}
		this.addMouseListener(this);
		this.addMouseMotionListener(mouseMotionHandler);

		// http://www.jyloo.com/news/?pubId=1306947485000
		// paintComponent ListCellRenderer
		this.setCellRenderer(new DefaultListCellRenderer() {
			private boolean isSelected;
			private boolean cellHasFocus;
			private String value;
			private JList list;
			private int index;
			private String info;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value,
						index, isSelected, cellHasFocus);
				this.list = list;
				this.isSelected = isSelected;
				this.cellHasFocus = cellHasFocus;
				this.value = value.toString();
				this.index = index;
				return c;
			}

			// @Override
			protected void paintComponent(Graphics g) {
				FontMetrics fm = g.getFontMetrics();
				//TODO
				if (progress[this.index]>0) {
					// red/green
					if (passed[this.index])
						g.setColor(Color.GREEN);
					else
						g.setColor(Color.RED);
					// progress
					g.fillRect(0, 0, this.getWidth() * progress[this.index] / 100,
							this.getHeight());
				} else {
					if (isSelected) {
						g.setColor(list.getSelectionBackground());
						g.fillRect(0, 0, this.getWidth(), this.getHeight());
					}
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				g.setColor(Color.black);
				g.drawString(String.valueOf(this.value), 1,
						this.getHeight() - 5);
				if (this.index == running_target_index && progress[this.index] < 100 &&
						this.getWidth()-fm.stringWidth(status) > fm.stringWidth(String.valueOf(this.value)))
				   g.drawString(status, this.getWidth()-fm.stringWidth(status), this.getHeight() - 5);
			}

		});
	}

	String ant_build_file = "";
	Project ant_project;

	public String getFilename() {
		return this.filename;
	}

	// returns list of ant tasks
	public String[] getTaskNames() {
		int[] idx = this.getSelectedIndices();
		String[] tasks = new String[idx.length];
		DefaultListModel model = ((DefaultListModel) this.getModel());
		for (int i = 0; i < idx.length; i++) {
			tasks[i] = model.get(idx[i]).toString();
		}
		return tasks;
	}
	
	/**
	 * Returns list index for the given target name.
	 */
	private int getIndex(String target){
		DefaultListModel model = ((DefaultListModel) this.getModel());
		for(int i=0; i < model.getSize(); i++) {
			AntTarget t = (AntTarget) model.getElementAt(i);
			if (t.target.equals(target))
				return i;
		}
		//should never happen
		return -1;
	}

	//
	public boolean addAntTasksToList(String filename) {
		try {
			boolean bAddPath = false;
			this.filename = filename;

			// clear list
			((DefaultListModel) this.getModel()).clear();
			File buildfile = new File(filename);
			Project project = new Project();
			project.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			project.addReference("ant.projectHelper", helper);
			helper.parse(project, buildfile);

			// Hashtable hash = project.getTargets();
			// print(buildfile.name + " size: " + hash.size());
			// Enumeration en = hash.keys();
			Vector v = new Vector();
			Enumeration en = project.getTargets().elements();
			while (en.hasMoreElements()) {
				String name = en.nextElement().toString();
				if (name != "")
					v.add(name);
			}
			Collections.sort(v);
			en = v.elements();

			while (en.hasMoreElements()) {
				String targetName = en.nextElement().toString();
				if (targetName != "") {
					String fileName = buildfile.getName();
					if (bAddPath)
						fileName = buildfile.getCanonicalPath();
					Target t = (Target)project.getTargets().get(targetName);
					((DefaultListModel) this.getModel()).addElement(new AntTarget(targetName, fileName, targetName, t.getDescription() ));
				}
			}
			progress = new int[((DefaultListModel) this.getModel()).getSize()];
			passed = new boolean[((DefaultListModel) this.getModel()).getSize()];
			this.setPrototypeCellValue("Index 1234567890");
			//set default selected = default target
			String defaultTarget = project.getDefaultTarget();
			this.setSelectedIndex(((DefaultListModel) this.getModel()).indexOf(defaultTarget));
			if (this.getSelectedIndex() == -1) {
				this.setSelectedIndex(0);
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
			return false;
		}
		return true;
	}// addAntTasksToList
	
	private void selectDefaultTask() {
		try {
			File buildFile = new File(this.filename);

			if (ant_build_file == "" || ant_build_file != this.filename) {
				ProjectHelper helper = ProjectHelper.getProjectHelper();
				ant_project = new Project();
				ant_project.init();
				ant_project.addReference("ant.projectHelper", helper);
				ant_build_file = this.filename;
				helper.parse(ant_project, buildFile);
				String defaultTask = ant_project.getDefaultTarget();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/*
	MouseAdapter mouseHandler = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			//right mouse button click? -> edit
			if (e.getClickCount() == 1 && (e.getModifiers() & InputEvent.BUTTON3_MASK)
					== InputEvent.BUTTON3_MASK) {
				antrunner.editFile(filename);
			//double click? -> run
			}else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK)
					== InputEvent.BUTTON1_MASK) {
				//antrunner.run();
				antrunner.executeSelectedTargets((AntRunnerComponent)this, null);
			}
		}
	};*/

	MouseMotionAdapter mouseMotionHandler = new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
			AntTaskList list = (AntTaskList) e.getSource();
			int idx = list.locationToIndex(e.getPoint());
			if (idx >= 0 && idx < ((DefaultListModel) list.getModel()).size()) {
				String target = ((DefaultListModel) list.getModel()).get(idx)
						.toString();
				String description = antrunner.getAntDescription(filename, target);
				if (description != null)
					list.setToolTipText(description);
				else
					list.setToolTipText("");
			}

		}
	};

	// @Override
	public void setRunner(AntRunner runner) {
		antrunner = runner;
	}

	// @Override
	public void setFilename(String filename) {
		this.filename = filename;

	}

	/*@Override
	public void buildFinished(BuildEvent arg0) {
		System.out.println("------- buildFinished");	
	}

	//@Override
	public void buildStarted(BuildEvent be) {
		System.out.println("------- buildStarted");	
	}

	//@Override
	public void messageLogged(BuildEvent arg0) {
		// TODO Auto-generated method stub

	}

	//@Override
	public void targetFinished(BuildEvent be) {
		// TODO Auto-generated method stub

	}

	//@Override
	public void targetStarted(BuildEvent be) {
		// TODO Auto-generated method stub


	}

	//@Override
	public void taskFinished(BuildEvent be) {
		// TODO Auto-generated method stub
		System.out.println("* taskFinished\n target: " + be.getTarget()
				+ " task: " + be.getTask());
			progress[running_target_index] += 10;
			repaint();
	}

	//@Override
	public void taskStarted(BuildEvent be) {
		// TODO Auto-generated method stub
		System.out.println("* taskStarted\n target: " + be.getTarget()
				+ " task: " + be.getTask());
	}*/

	//@Override
	public void progress(int percent, ProgressState state, String info, BuildEvent event) {
		// TODO System.out.println("progress----- info: " + info);
		if (antrunner != null) {
			//
		}
		this.status = info;
		switch (state)
		{
			case STARTED: if (event.getTarget() != null) {
							// TODO System.out.println("* targetStarted target: " + event.getTarget() + ", task: " + event.getTask() + "info: " + info);
							running_target = event.getTarget().getName();
							running_target_index = this.getIndex(event.getTarget().getName());
							progress[running_target_index] = 0; //TODO percent
							repaint();
						}
						break;
						
			case RUNNING: 	if (running_target_index >= 0) {
								progress[running_target_index] = percent;
								repaint();
							}
							break;
			case FINISHED: 
							if (event.getTarget() != null && running_target_index >= 0) {
								// TODO  System.out.println("* targetFinished target: " + event.getTarget() + " task: " + event.getTask() + " Exception: " + (event.getException() != null) + " info: " + info);
								// running_target = "";
								int index = this.getIndex(event.getTarget().getName());
								passed[index] = event.getException() == null;
								if (passed[index])
									progress[index] = 100;
								repaint();
							}
								break;
		}
	}

	//@Override
	public void clearBuildStatus() {
		running_target_index = -1;
		for(int i=0; i < progress.length; i++) {
			progress[i] = 0;
			passed[i] = true;
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		//right mouse button click? -> edit
		if (e.getClickCount() == 1 && (e.getModifiers() & InputEvent.BUTTON3_MASK)
				== InputEvent.BUTTON3_MASK) {
			antrunner.editFile(filename);
		//double click? -> run
		}else if (e.getClickCount() == 2 && (e.getModifiers() & InputEvent.BUTTON1_MASK)
				== InputEvent.BUTTON1_MASK) {
			antrunner.executeSelectedTargets((AntRunnerComponent)this, null);
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public AntTarget[] getSelectedAntTargets() {
		//TODO
		AntTarget[] targets = new AntTarget[getSelectedValues().length];
		for(int i=0; i < targets.length;i++) {
			Object o = getSelectedValues()[i];
			//targets[i] = new AntTarget(this.filename, getSelectedValues()[i].toString());
			targets[i] = (AntTarget)o;
		}
		return targets;
	}
}// AntTaskList