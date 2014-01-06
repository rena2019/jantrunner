/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.tools.ant.BuildEvent;

public class AntRunnerButton extends JButton implements AntRunnerComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AntRunner antrunner;
	private int progress;
	private boolean passed = true;

	AntRunnerButton(String text, String file, String target, String description) {
		super(text);
		this.file = file;
		this.target = target;
		if (!description.equals(""))
			this.setToolTipText(description);
		addDragHandler();
	}

	AntRunnerButton(AntTarget antTarget) {
		if (antTarget.name != null && !antTarget.name.equals(""))
			this.setText(antTarget.name);
		else
			this.setText(antTarget.target);
		this.file = antTarget.file;
		this.target = antTarget.target;
		if (antTarget.description != null && !antTarget.description.equals(""))
			this.setToolTipText(antTarget.description);
		// TODO addDragHandler() ;
	}

	public String getTarget() {
		return target;
	}

	// TODO remove button from panel
	void addDragHandler() {
		this.setTransferHandler(new TransferHandler("text"));
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JComponent c = (JComponent) e.getSource();
				TransferHandler th = c.getTransferHandler();
				th.exportAsDrag(c, e, TransferHandler.COPY);
			}
		});
	}

	String file;
	String target;

	@Override
	public void setRunner(AntRunner runner) {
		antrunner = runner;
	}

	@Override
	public String[] getTaskNames() {
		return new String[] { target };
	}

	@Override
	public String getFilename() {
		return file;
	}

	@Override
	public void setFilename(String filename) {
		file = filename;
	}

	@Override
	public void progress(int progress, ProgressState state, String info,
			BuildEvent event) {
		this.progress = progress;
		if (state == ProgressState.FINISHED)
			passed = event.getException() == null;
		else if (state == ProgressState.STARTED)
			passed = true;
		repaint();
	}

	@Override
	public void clearBuildStatus() {
		passed = true;
		progress = 0;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {

		final Graphics2D g2d = (Graphics2D) g;
		Color c1 = Color.LIGHT_GRAY;
		Color c2 = Color.WHITE;
		GradientPaint gp = new GradientPaint(0, getHeight(), c1, 0,
				getHeight() / 3, c2, true);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		if (!passed)
			g2d.setColor(Color.RED);
		else
			g2d.setColor(Color.GREEN);
		// progress
		g2d.fillRect(0, 0, this.getWidth() * progress / 100, this.getHeight());
		g2d.setColor(Color.BLACK);
		int x = (getWidth() - g2d.getFontMetrics().stringWidth(getText())) / 2;
		int y = getHeight()
				- ((getHeight() - g2d.getFontMetrics().getHeight()) / 2) - 3;
		g2d.drawString(getText(), x, y);
	}

}
