/*
 * (c) by ReNa2019
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;


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
			tasklist.setSelectedIndex(0);
		}
	}

	class AntTaskList extends JList implements AntRunnerComponent {
		
		private String filename = "";
		private AntRunner antrunner;
		//constructor
		AntTaskList(String filename) {
			super(new DefaultListModel());
			if (filename != null) {
				this.filename = filename;
				XThread t = new XThread(this, filename);
				t.start();
			}
			this.addMouseListener(mouseHandler);
			this.addMouseMotionListener(mouseMotionHandler);
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

		//
		public boolean addAntTasksToList(String filename) {
			try {
				boolean bAddPath = false;
				this.filename = filename;

				// clear list
				((DefaultListModel) this.getModel()).clear();
				this.setPrototypeCellValue("Index 1234567890");
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
					String name = en.nextElement().toString();
					if (name != "") {
						String s = buildfile.getName();
						if (bAddPath)
							s = buildfile.getCanonicalPath();
						((DefaultListModel) this.getModel()).addElement(name);
					}
				}
			} catch (Exception ex) {
				System.err.println(ex.toString());
				return false;
			}
			return true;
		}// addAntTasksToList

		String getAntDescription(String target) {
			String s = "";
			try {
				File buildFile = new File(this.filename);

				if (ant_build_file == "" || ant_build_file != this.filename) {
					ProjectHelper helper = ProjectHelper.getProjectHelper();
					ant_project = new Project();
					ant_project.init();
					ant_project.addReference("ant.projectHelper", helper);
					ant_build_file = this.filename;
					helper.parse(ant_project, buildFile);
				}

				Hashtable hash = ant_project.getTargets();
				s = ((Target) hash.get(target)).getDescription();
				// System.out.println("target=" + target + "/" + s);
			} catch (Exception e) {
				System.out.println(e.toString());
			}

			return s;
		}// getAntDescription

		MouseAdapter mouseHandler = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					antrunner.editFile(filename);
				}
			}
		};

		MouseMotionAdapter mouseMotionHandler = new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				AntTaskList list = (AntTaskList) e.getSource();
				int idx = list.locationToIndex(e.getPoint());
				if (idx >= 0
						&& idx < ((DefaultListModel) list.getModel()).size()) {
					String task = ((DefaultListModel) list.getModel()).get(idx)
							.toString();
					String description = list.getAntDescription(task);
					if (description != null)
						list.setToolTipText(description);
				}

			}
		};

		@Override
		public void setRunner(AntRunner runner) {
			antrunner = runner;
			
		}

		@Override
		public void setFilename(String filename) {
			this.filename = filename;
			
		}
	}//AntTaskList