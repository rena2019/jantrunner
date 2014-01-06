import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.tools.ant.BuildEvent;

/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */
/**
 * Class implements a list with ant targets.
 * @author rena2019
 *
 */
public class BatchList extends JPanel implements AntRunnerComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JList lstBatch;
	private AntRunner antrunner;
	
	private static final String ANTRUNNER_BATCH_FILE = "antrunner.xml";
	private static final String ANTRUNNER_BATCH_TASK = "batch";
	private static final String COMMENT_CREATED_BY = "<!--\r\ncreated with jantrunner\r\n-->\r\n";
	
	private int[] progress;
	private boolean[] passed;
	private int running_target_index=0;
	private String status = "";
	private String running_target = "";
	
	JButton btnAntBatchRun;
	
	BatchList(AntRunner antrunner) {
		super(new BorderLayout());
		this.antrunner = antrunner;
		lstBatch = new JList(new DefaultListModel());
		lstBatch.addMouseListener(mouseHandler);
		
		JPanel panelAntBatch = this;
		panelAntBatch.add(new JLabel("Batch"), BorderLayout.NORTH);
		panelAntBatch.add(lstBatch, BorderLayout.CENTER);
		btnAntBatchRun = new JButton("Run Batch");
		btnAntBatchRun.addActionListener(antBatchHandler);
		JButton btnAntBatchRemove = new JButton("Remove");
		btnAntBatchRemove.addActionListener(antBatchHandler);
		JButton btnAntBatchUp = new JButton("Up");
		btnAntBatchUp.addActionListener(antBatchHandler);
		JButton btnAntBatchDown = new JButton("Down");
		btnAntBatchDown.addActionListener(antBatchHandler);
		JPanel panelBatchButtons = new JPanel(new FlowLayout());

		panelBatchButtons.add(btnAntBatchRemove);
		panelBatchButtons.add(btnAntBatchUp);
		panelBatchButtons.add(btnAntBatchDown);
		panelBatchButtons.add(btnAntBatchRun);
		panelAntBatch.add(panelBatchButtons, BorderLayout.SOUTH);
		
		//drag&drop handler for batch list 
		new DropTargetListener(this);
		
		// http://www.jyloo.com/news/?pubId=1306947485000
				// paintComponent ListCellRenderer
		lstBatch.setCellRenderer(new DefaultListCellRenderer() {
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
	
	/**
	 * Write list of ant targets to ant xml file.
	 */
	private void writeBatchFile() {
		String targets = "";
		for (int i = 0; i < ((DefaultListModel) lstBatch.getModel())
				.size(); i++) {
			Object o = ((DefaultListModel) lstBatch.getModel()).get(i);
			AntTarget antTarget = (AntTarget)((DefaultListModel) lstBatch.getModel()).get(i);
			if (i > 0)
				targets += "\r\n";
			targets += "  <ant antfile=\"" + antTarget.file + "\" target=\"" + antTarget.target
					+ "\" />";
		}
		writeAntTaskfile(ANTRUNNER_BATCH_FILE, targets, ANTRUNNER_BATCH_TASK, "");
	}
	
	void writeAntTaskfile(String filename, String task, String taskname,
			String additional) {
		String antxml = "<project name=\"Run" + taskname + "\" default=\""
				+ taskname + "\" >\r\n" + COMMENT_CREATED_BY + additional + "<target name=\""
				+ taskname + "\">\r\n" + task + "\r\n</target>\r\n</project>";
		if (filename != null)
			antrunner.writeFile(filename, antxml);
	}
	
	/**
	 * Mouse handler: right click -> edit file
	 */
	MouseAdapter mouseHandler = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 1 && (e.getModifiers() & InputEvent.BUTTON3_MASK)
					== InputEvent.BUTTON3_MASK) {
				JList lst = ((JList) e.getSource());
				if (lst == lstBatch) {
					// list on the right side (batch list)
					writeBatchFile();
					antrunner.editFile(ANTRUNNER_BATCH_FILE);
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
			System.out.println(e.toString());
			// int index = locationToIndex(pEvent.getPoint());
		}
	};
	
	public void addAntTarget(AntTarget target) {
		DefaultListModel model = ((DefaultListModel)lstBatch.getModel());
		//String file = new File(target.file).getAbsolutePath();
		//model.add(model.size(), file + ";" + target.target);
		model.addElement(target);
		// select last item
		lstBatch.setSelectedIndex(model.size() - 1);
		progress = new int[model.size()];
		passed = new boolean[model.size()];
	}
	
	private void executeTargets() {
		antrunner.executeSelectedTargets(this, btnAntBatchRun);
	}
	
	/**
	 * button handler for ant batch list
	 */
	ActionListener antBatchHandler = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			//
			int idx = 0;
			if (event.getActionCommand() == "Run Batch") {
				//
				// print("run batch");
				// <ant antfile="subproject/subbuild.xml" target="compile"/>
				writeBatchFile();
				//T antrunner.setButtonBgColor((JButton) event.getSource(), antrunner.executeAntTarget(ANTRUNNER_BATCH_FILE, ANTRUNNER_BATCH_TASK));
				executeTargets();
				
			} else if (event.getActionCommand() == "Up") {
				//move selected element up
				idx = lstBatch.getSelectedIndex();
				if (idx > 0) {
					AntTarget old = (AntTarget)((DefaultListModel) lstBatch.getModel()).get(
							idx);
					((DefaultListModel) lstBatch.getModel())
							.removeElementAt(idx);
					((DefaultListModel) lstBatch.getModel()).insertElementAt(
							old, idx - 1);
					lstBatch.setSelectedIndex(idx - 1);
				}
			} else if (event.getActionCommand() == "Down") {
				//move selected element down
				idx = lstBatch.getSelectedIndex();
				if (idx < ((DefaultListModel) lstBatch.getModel()).size() - 1) {
					String old = ((DefaultListModel) lstBatch.getModel()).get(
							idx).toString();
					((DefaultListModel) lstBatch.getModel())
							.removeElementAt(idx);
					((DefaultListModel) lstBatch.getModel()).insertElementAt(
							old, idx + 1);
					lstBatch.setSelectedIndex(idx + 1);
				}
			} else if (event.getActionCommand() == "Remove") {
				// remove item from ant batch file
				idx = lstBatch.getSelectedIndex();
				if (idx >= 0)
					((DefaultListModel) lstBatch.getModel()).remove(idx);
				if (idx > ((DefaultListModel) lstBatch.getModel()).size() - 1)
					idx = ((DefaultListModel) lstBatch.getModel()).size() - 1;
				if (((DefaultListModel) lstBatch.getModel()).size() > 0)
					lstBatch.setSelectedIndex(idx);
			}
		}
	};

	@Override
	public void setRunner(AntRunner runner) {
		this.antrunner = runner;
	}

	@Override
	public String[] getTaskNames() {	
		/* TODO
		DefaultListModel model = ((DefaultListModel) lstBatch.getModel());
		String[] targets = new String[model.getSize()];
		for(int i=0; i < model.getSize(); i++) {
			targets[i] = ((AntTarget) model.getElementAt(i)).target;
		}
		return targets;
		*/
		return new String[] { ANTRUNNER_BATCH_TASK };
	}

	@Override
	public String getFilename() {
		return ANTRUNNER_BATCH_FILE;
	}

	@Override
	public void setFilename(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void progress(int percent, ProgressState state, String info,
			BuildEvent event) {
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
									running_target_index = getIndex(event.getTarget().getName());
									if (running_target_index >= 0) {
										progress[running_target_index] = 0; //TODO percent
										repaint();
									}
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
										if (index >= 0) {
											passed[index] = event.getException() == null;
											if (passed[index])
												progress[index] = 100;
											repaint();
										}
									}
										break;
				}
		
	}

	@Override
	public void clearBuildStatus() {
		running_target_index = -1;
		for(int i=0; i < progress.length; i++) {
			progress[i] = 0;
			passed[i] = true;
		}
		repaint();
	}
	
	/**
	 * Returns list index for the given target name.
	 */
	private int getIndex(String target){
		DefaultListModel model = ((DefaultListModel) lstBatch.getModel());
		for(int i=0; i < model.getSize(); i++) {
			AntTarget t = (AntTarget) model.getElementAt(i);
			if (t.target.equals(target))
				return i;
		}
		//should never happen
		return -1;
	}
}
