import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/*
 set default
 TreePath path = this.pathFromFile(home);
 tree.setSelectionPath(path);
 tree.scrollPathToVisible(treePath);
 
 */

public class AntTreeList extends JTree implements AntRunnerComponent, MouseListener {

	private String filename = "";
	private AntRunner antrunner;
	private String running_target = "";
	private int[] progress;
	private boolean[] passed;
	private int running_target_index=0;
	private String status = "";
	String ant_build_file = "";
	Project ant_project;
	
	// constructor
	AntTreeList(String filename) {
		super();
		this.filename = filename;
		this.addMouseListener(this);
		
		//Create the nodes.
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
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
		//TreeNode root = (TreeNode)((DefaultTreeModel) this.getModel()).getRoot();
		while (en.hasMoreElements()) {
			String name = en.nextElement().toString();
			if (name != "") {
				String s = buildfile.getName();
				if (false)
					try {
						s = buildfile.getCanonicalPath();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				root.add(new DefaultMutableTreeNode(name));
			}
		}
        this.setModel(new DefaultTreeModel(root));
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	// @Override
	public void setRunner(AntRunner runner) {
		antrunner = runner;
	}

	// returns list of ant tasks
	@Override
	public String[] getTaskNames() {
		TreeNode root = (TreeNode)((DefaultTreeModel) this.getModel()).getRoot();
		TreePath[] paths = this.getSelectionPaths();
		String[] targets = new String[paths.length];
		for(int i=0; i<targets.length; i++) {
			targets[i] = paths[i].getLastPathComponent().toString();
		}
		/* TODO
		int[] idx = this.getSelectedIndices();
		String[] tasks = new String[idx.length];
		DefaultListModel model = ((DefaultListModel) this.getModel());
		for (int i = 0; i < idx.length; i++) {
			tasks[i] = model.get(idx[i]).toString();
		}
		*/
		return targets;
	}

	@Override
	public String getFilename() {
		return this.filename;
	}

	// @Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public void progress(int progress, ProgressState state, String info,
			BuildEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearBuildStatus() {
		// TODO Auto-generated method stub

	}

}
