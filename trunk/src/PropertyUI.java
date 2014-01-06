import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * This class displays user controls (JCheckBox, JTextField, JRadioButton) created from a XUL-like file. The settings
 * can be stored in a property file.
 * 
 * @author rena2019
 * 
 */
public class PropertyUI extends JPanel implements AntRunnerConfig,
		ActionListener {

	private static final long serialVersionUID = 1L;
	private AntRunner antrunner;
	// XUL UI control file
	private String configFile;
	// settings of UI will be stored in a property file
	private String propertyFile;
	// helper hash map for radio buttons
	private HashMap<String, ButtonGroup> radiobuttons;

	@Override
	public void setRunner(AntRunner runner) {
		// System.out.println(String.format("setRunner"));
		// reference to application
		antrunner = runner;
	}

	@Override
	public void setFilename(String filename) {
		configFile = filename;
		propertyFile = configFile + ".properties";
		// read in config -> create GUI
		createGUI();
	}

	/**
	 * Write all settings to a property file.
	 */
	private void writePropertyFile() {
		Properties prop = new Properties();

		try {
			// set the properties value
			for (int i = 0; i < getComponentCount(); i++) {
				if (getComponent(i).getClass() == JCheckBox.class) {
					prop.setProperty(getComponent(i).getName(),
							((Boolean) ((JCheckBox) getComponent(i))
									.isSelected()).toString());
				} else if (getComponent(i).getClass() == JTextField.class) {
					prop.setProperty(getComponent(i).getName(),
							((JTextField) getComponent(i)).getText());
				} /*
				 * else if (getComponent(i).getClass() == JRadioButton.class) {
				 * if (((JRadioButton)getComponent(i)).isSelected())
				 * prop.setProperty(((JRadioButton)getComponent(i)).get,
				 * ((JRadioButton)getComponent(i)).getText()); }
				 */
			}
			// radiogroup -> hashmap
			Set<String> keys = radiobuttons.keySet();
			for (String key : keys) {
				ButtonGroup group = radiobuttons.get(key);
				prop.setProperty(key, getSelectedButtonText(group));
			}
			// save properties to project root folder
			prop.store(new FileOutputStream(propertyFile), null);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Read settings from property file.
	 * 
	 * @return
	 */
	private Properties readProperties() {
		Properties prop = new Properties();

		try {
			// load a properties file
			prop.load(new FileInputStream(propertyFile));
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return prop;

	}

	/**
	 * Helper function to get text of selected radio button.
	 * 
	 * @param buttonGroup
	 * @return
	 */
	private String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons
				.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();

			if (button.isSelected()) {
				return button.getText();
			}
		}
		return null;
	}

	/**
	 * Read property file, XUL file and create user controls.
	 */
	private void createGUI() {
		radiobuttons = new HashMap<String, ButtonGroup>();
		// read property file with settings
		Properties prop = readProperties();
		File fXmlFile = new File(configFile);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			NodeList controls = doc.getElementsByTagName("box").item(0)
					.getChildNodes();

			for (int i = 0; i < controls.getLength(); i++) {
				NamedNodeMap attrib = controls.item(i).getAttributes();
				if (controls.item(i).getNodeName().toLowerCase()
						.equals("textbox")) {
					// textbox
					String text = prop.getProperty(attrib.getNamedItem("id")
							.getNodeValue(), attrib.getNamedItem("value")
							.getNodeValue());
					JTextField textfield = new JTextField(text);
					textfield.setName(attrib.getNamedItem("id").getNodeValue());
					textfield.addActionListener(this);
					add(textfield);
				} else if (controls.item(i).getNodeName().toLowerCase()
						.equals("label")) {
					// label
					JLabel label = new JLabel(attrib.getNamedItem("value")
							.getNodeValue());
					add(label);
				} else if (controls.item(i).getNodeName().toLowerCase()
						.equals("checkbox")) {
					// checkbox
					if (attrib.getNamedItem("label") != null
							&& attrib.getNamedItem("id") != null) {
						boolean checked = false;
						if (attrib.getNamedItem("checked") != null)
							checked = Boolean.parseBoolean(attrib.getNamedItem(
									"checked").getNodeValue());
						checked = Boolean.parseBoolean(prop.getProperty(attrib
								.getNamedItem("id").getNodeValue(), Boolean
								.toString(checked)));
						JCheckBox checkbox = new JCheckBox(attrib.getNamedItem(
								"label").getNodeValue(), checked);
						checkbox.setName(attrib.getNamedItem("id")
								.getNodeValue());
						checkbox.addActionListener(this);
						add(checkbox);
					} else {
						System.err.println(String.format(
								"invalid checkbox definition (%s, %s)",
								attrib.getNamedItem("label"),
								attrib.getNamedItem("id")));
					}
				} else if (controls.item(i).getNodeName().toLowerCase()
						.equals("radiogroup")) {
					// radiogroup
					ButtonGroup group = new ButtonGroup();
					NodeList nodes = controls.item(i).getChildNodes();
					String key = null;
					for (int j = 0; j < nodes.getLength(); j++) {
						if (nodes.item(j).getNodeName().toLowerCase()
								.equals("radio")) {
							// id of radiogroup as key
							key = attrib.getNamedItem("id").getNodeValue();
							NamedNodeMap radioAttributes = nodes.item(j)
									.getAttributes();
							boolean selected = false;
							if (radioAttributes.getNamedItem("selected") != null
									&& !prop.containsKey(key))
								selected = Boolean.parseBoolean(radioAttributes
										.getNamedItem("selected")
										.getNodeValue());
							if (radioAttributes.getNamedItem("label") != null) {
								if (prop.containsKey(key)
										&& radioAttributes
												.getNamedItem("label")
												.getNodeValue()
												.equals(prop.get(key)))
									selected = true;
								JRadioButton radioButton = new JRadioButton(
										radioAttributes.getNamedItem("label")
												.getNodeValue(), selected);
								// radioButton.setName(attrib.getNamedItem("group").getNodeValue());
								radioButton.addActionListener(this);
								group.add(radioButton);
								add(radioButton);

							}
						}
					}
					if (key != null)
						radiobuttons.put(key, group);
				}
			}
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		writePropertyFile();

	}
}
