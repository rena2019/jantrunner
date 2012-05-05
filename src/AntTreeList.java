import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class AntTreeList extends JTree implements AntRunnerComponent,
		MouseListener {

	private String filename = "";
	private AntRunner antrunner;
	private int[] progress;
	private boolean[] passed;
	private String status = "";
	String ant_build_file = "";
	Project ant_project;
	String path_separator = ".";

	/**
	 * Mouse motion handler: display ant target description.
	 */
	private MouseMotionAdapter mouseMotionHandler = new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
			AntTreeList tree = (AntTreeList) e.getSource();
			TreePath path = tree.getClosestPathForLocation(e.getPoint().x,
					e.getPoint().y);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			if (node != null) {
				Object object = node.getUserObject();
				if (object != null) {
					String targetName = ((NodeInfo) object).getTargetName();
					String description = antrunner.getAntDescription(filename,
							targetName);
					if (description != null)
						tree.setToolTipText(description);
					else
						tree.setToolTipText("");
				}
			}
		}
	};

	/** Constructor of AntTreeList
	 * 
	 * @param filename
	 */
	AntTreeList(String filename) {
		super();
		this.filename = filename;
		this.addMouseListener(this);
		this.addMouseMotionListener(mouseMotionHandler);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(filename);
		File buildfile = new File(filename);
		Project project = new Project();
		project.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		project.addReference("ant.projectHelper", helper);
		helper.parse(project, buildfile);
		String defaultTarget = project.getDefaultTarget();
		TreePath treePath = null;

		Vector v = new Vector();
		Enumeration en = project.getTargets().elements();
		while (en.hasMoreElements()) {
			String name = en.nextElement().toString();
			if (name != "")
				v.add(name);
		}
		Collections.sort(v);
		en = v.elements();
		// add all targets to tree
		while (en.hasMoreElements()) {
			String name = en.nextElement().toString();
			TreePath path = new TreePath(root);
			if (name != "") {
				String[] parts = name.split(path_separator.replace(".", "\\."));
				DefaultMutableTreeNode node = root;
				String targetName = "";
				for (int idx = 0; idx < parts.length; idx++) {
					boolean nodeNotFound = true;
					targetName += parts[idx];
					if (node.getChildCount() > 0) {
						for (Enumeration e = node.children(); e
								.hasMoreElements();) {
							TreeNode n = (TreeNode) e.nextElement();
							// tree node found?
							if (n.toString().equals(parts[idx])) {
								// node already existing -> use it for next loop
								node = (DefaultMutableTreeNode) n;
								path = path.pathByAddingChild(node);
								nodeNotFound = false;
								break;
							}
						}
					}
					if (nodeNotFound) {
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
								new NodeInfo(targetName));
						node.add(newNode);
						if (defaultTarget.equals(targetName)) {
							treePath = path.pathByAddingChild(newNode);
						}
						node = newNode;
					}
					targetName += path_separator;
				}// for
			}
		}
		this.setModel(new DefaultTreeModel(root));
		expandAll(this);
		// set default selected = default target
		if (treePath != null) {
			setSelectionPath(treePath);
			scrollPathToVisible(treePath);
			setLeadSelectionPath(treePath);
		}
		setRootVisible(false);
	}
	
	private void addNodesRecursive(TreeNode treeNode) {
		
	}
	

	/**
	 * Expand all nodes.
	 * 
	 * @param tree
	 */
	private void expandAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	/**
	 * Helper function.
	 * @param tree
	 * @param names
	 * @return
	 */
	private static TreePath findByName(JTree tree, String[] names) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		return find(tree, new TreePath(root), names, 0);
	}

	/**
	 * Helper function.
	 * @param tree
	 * @param parent
	 * @param nodes
	 * @param depth
	 * @return
	 */
	private static TreePath find(JTree tree, TreePath parent, Object[] nodes,
			int depth) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		Object o = node;

		if (o.equals(nodes[depth])) {
			if (depth == nodes.length - 1) {
				return parent;
			}
			if (node.getChildCount() >= 0) {
				for (Enumeration e = node.children(); e.hasMoreElements();) {
					TreeNode n = (TreeNode) e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					TreePath result = find(tree, path, nodes, depth + 1);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
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

	/**
	 * Returns list of selected ant targets.
	 */
	@Override
	public String[] getTaskNames() {
		TreeNode root = (TreeNode) ((DefaultTreeModel) this.getModel())
				.getRoot();
		TreePath[] paths = this.getSelectionPaths();
		String[] targets = new String[paths.length];
		int arrayLen = 0;
		for (int i = 0; i < targets.length; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i]
					.getLastPathComponent();
			if (node.isLeaf()) {
				NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
				targets[arrayLen] = nodeInfo.getTargetName();
				arrayLen++;
			}
		}
		String[] arr = new String[arrayLen];
		System.arraycopy(targets, 0, arr, 0, arrayLen);
		return arr;
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

	private class NodeInfo {
		public String targetName = "";

		public NodeInfo(String targetName) {
			this.targetName = targetName;
		}

		public String toString() {
			String[] s = targetName.split(path_separator.replace(".", "\\."));
			if (s.length > 0)
				return s[s.length - 1];
			return targetName;
		}

		public String getTargetName() {
			return targetName;
		}
	}

}
