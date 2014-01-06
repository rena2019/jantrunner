/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;


public class ButtonPanel extends JPanel implements DragGestureListener{

	AntRunner antRunner;
	public ButtonPanel(AntRunner antRunner) {
		this.antRunner = antRunner;
	}
	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		//TODO
		System.out.println("dragGestureRecognized");
		Cursor cursor = null;
		String target="";
		if (event.getDragAction() == DnDConstants.ACTION_COPY) {
			cursor = DragSource.DefaultCopyDrop;
		}
		if (event.getComponent() instanceof AntTaskList) {
			AntTarget[] antTargets = ((AntTaskList)event.getComponent()).getSelectedAntTargets();
			if (antTargets.length > 0)
				event.startDrag(cursor, new TransferableAntTarget(antTargets));
		} else if (event.getComponent() instanceof AntTreeList) {
			AntTarget[] antTargets = ((AntTreeList)event.getComponent()).getSelectedAntTargets();
			if (antTargets.length > 0)
				event.startDrag(cursor, new TransferableAntTarget(antTargets));
		}
	}//dragGestureRecognized
	
	public void addAntTarget(AntTarget antTarget, boolean updateConfig) {
		//TODO store in config ... action handler ...
		AntRunnerButton btn = new AntRunnerButton(antTarget);
		btn.addActionListener(buttonHandler);
		add(btn);
		validate();
		if (updateConfig)
			antRunner.updateConfig();
	}
	
	/**
	 * global action handler
	 */
	ActionListener buttonHandler = new ActionListener() {
		public void actionPerformed(ActionEvent event) {

			if (event.getSource() instanceof AntRunnerButton) {
				antRunner.executeSelectedTargets((AntRunnerButton) event.getSource(), (AntRunnerButton) event.getSource());
				//AntRunnerButton btn = (AntRunnerButton) event.getSource();
				// clear color
				//clearButtonBgColor(btn);
				//boolean passed = antRunner.executeAntTarget(new File(btn.file).getAbsolutePath(), btn.target);
				//set red/green upon status
				//setButtonBgColor((JButton)btn, passed);
				//antRunner.executeSelectedTargets(antRunnerComponent, button)
			}
		}
	};
	
	/**
	 * Set the color of the buton to default system color.
	 * 
	 * @param button
	 */
	void clearButtonBgColor(JButton button) {
		button.setBackground((Color) Toolkit.getDefaultToolkit()
				.getDesktopProperty("control"));
	}
	
	/**
	 * Change the color of the given button to green (ok = true), otherwise to
	 * red.
	 */
	void setButtonBgColor(JButton button, boolean ok) {
		if (ok)
			button.setBackground(Color.GREEN);
		else
			button.setBackground(Color.RED);
	}
}
