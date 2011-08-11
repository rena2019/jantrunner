/*
 * (c) by ReNa2019
 */
import javax.swing.JButton;

public class AntRunnerButton extends JButton {
	AntRunnerButton(String text, String file, String target, String description) {
		super(text);
		this.file = file;
		this.target = target;
		if (!description.equals(""))
			this.setToolTipText(description);
	}
	String file;
	String target;
}
