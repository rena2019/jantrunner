/*
 * (c) by ReNa2019 http://code.google.com/p/jantrunner/
 * 
 */

import java.awt.EventQueue;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Toolkit;

//XML
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
//ant tasks
// API doc http://www.jajakarta.org/ant/ant-1.6.1/docs/mix/manual/api/overview-summary.html
import org.apache.tools.ant.*;

/*
 2010-xx-xx started
 2011-03-23 sort ant tasks
 2011-03-27 ant batch
 2011-04-17 AntRunner class
 2011-04-26 config.xml buttons implemented
 2011-05-xx button, tabs via config.xml
 2011-05-07 Splitter tabbed pane/batch
 2011-06-01 load file moved to thread to shorten startup time
 2011-06-29 more generic
 2011-07-08 more generic, AntTestPanel, setFilename added
 2011-08-11 editFile
 2011-08-15 update timer added
 2011-08-21 target list with progress bar started
 2011-08-22 open build.xml file if given as command line parameter
 2011-08-23 execute target in thread
 2011-09-18 Statistics class added
 2011-12-22 added: edit with notepad
 2011-12-27 tasklist: double click starts task
 2012-02-04 some fixes, color button, default selected target, dumpTable
 2012-05-04 v0.4: AntTreeList
 2013-07-26 d&d handling started
 2013-12-xx batch list moved to class, button execution with treads, timer enable/disable

 TODO
 - timer thread for soft progressbar in AntRunnerTab
 - task -> target
 - AntRunnerFileTransferHandler: more generic / with interface ?
 - sqlite for statistic/estimation of avg./max. duration

 */

public class AntRunner /* extends JFrame */ {

	private static final String TASK_LIST = "TaskList";
	private static final String LOG_FILE = "log.txt";
	private static final String DEFAULT_CONFIG_XML = "config.xml";
	private static final String VERSION = "0.5";
	private static final String COMMENT_CREATED_BY = "<!--\r\ncreated with jantrunner\r\n-->\r\n";

	// TODO
	String ANT_FILES_PATH = null;
	String STATISTICS_DB = "antrunner.db";

	// TODO config?
	int message_output_level = Project.MSG_INFO;

	// global variables
	Project antproject;
	Project project;
	org.apache.tools.ant.BuildLogger logger = null;
	String loggerClass;
	String configFile = "";
	//timer setting
	String timer_ant_file;
	String timer_ant_target;
	long timer_delay_sec;
	
	String last_descr_build_file = "";
	JFrame frame;

	BatchList batchList;
	ButtonPanel cmdButtonPanel;

	// tools
	JCheckBox chkPoll;
	//JButton btnFileChooser;

	JTabbedPane tabbedPane;
	Timer timer = null;
	//JTextField txtTimerField;
	//JTextField loggerTextField;
	JComboBox comboboxLogger;
	JPanel configPanel;
	AntRunnerTab moreTab;
	
	JSplitPane splitPane;
	private Statistics statistics;

	/**
	 * Add the given info to the log file.
	 */
	public void addToLog(String info, int loglevel) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(LOG_FILE,
					true));
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd kk:mm:ss");
			Date dt = new java.util.Date();
			String s = formatter.format(dt);
			s = s + " " + System.getenv("COMPUTERNAME") + " " + info;
			out.write(s);
			if (project != null)
				project.log(s);
			else
				System.out.println(s);
			out.newLine();
			// Close the output stream
			out.close();
		} catch (Exception e) {
			// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * execute given program and wait for process
	 */
	public String exec(String path) {
		return exec(path, true);
	}
	

	/**
	 * execute given program
	 * 
	 * @param path
	 * @param wait
	 *            if true wait for process to exit
	 * @return
	 */
	public String exec(String path, boolean wait) {
		String out = "";
		try {
			String line;
			Process p = Runtime.getRuntime().exec(path);
			if (wait) {
				BufferedReader input = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				BufferedReader err = new BufferedReader(
						new InputStreamReader(p.getErrorStream()));
				while ((line = input.readLine()) != null ) {
					if (out != "")
						out += "\r\n";
					out += line;
					System.out.println(line);
					line = err.readLine();
					if (line != null)
						System.err.println(line);
				}
				input.close();
			}
		} catch (Exception err) {
			System.err.println("ERROR");
			err.printStackTrace();
		}
		return out;
	}

	/**
	 * edit the given file with the default system editor
	 * 
	 * @param filename
	 */
	public void editFile(String filename) {
		boolean startNotepad = false;
		try {
			Desktop desktop = null;
			if (Desktop.isDesktopSupported()) {
				desktop = Desktop.getDesktop();
				File f = new File(filename);
				//System.out.print(f.exists());
				desktop.edit(f.getCanonicalFile());
			}
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			startNotepad = true;
		}
		//try to open notepad if edit method failed
		if (startNotepad) {
			try {
				String s = "notepad.exe " + new File(filename).getCanonicalPath();
				exec(s);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * rename the given file (append date)
	 * 
	 * @param filepath
	 */
	void renameYYYYMMDD(String filepath) {
		// print(file);
		File file = new File(filepath);
		String filename = file.getName();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date dt = new java.util.Date(file.lastModified());
		String dateString = formatter.format(dt);
		file.renameTo(new File(file.getParent(), filename.substring(0,
				filename.lastIndexOf('.') + 1)
				+ dateString + filename.substring(filename.lastIndexOf('.'))));
	}

	/**
	 * This method returns the selected radio button in a button group public
	 * 
	 * @param group
	 * @return
	 */
	JRadioButton getSelection(ButtonGroup group) {
		for (Enumeration e = group.getElements(); e.hasMoreElements();) {
			JRadioButton b = (JRadioButton) e.nextElement();
			if (b.getModel() == group.getSelection()) {
				return b;
			}
		}
		return null;
	}

	/**
	 * write the given text to file
	 * 
	 * @param filename
	 * @param text
	 */
	void writeFile(String filename, String text) {
		try {

			File file = new File(filename);
			Writer output = new BufferedWriter(new FileWriter(file));
			output.write(text);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * read in the given text file
	 * 
	 * @param filename
	 * @return
	 */
	public String readFile(String filename) {
		String text = "";
		try {

			File file = new File(filename);
			BufferedReader in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				text += str + "\r\n";
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * copy the given text file
	 * 
	 * @param src
	 * @param dest
	 */
	void copyFileAscii(String src, String dest) {
		try {
			File inputFile = new File(src);
			File outputFile = new File(dest);

			FileReader in = new FileReader(inputFile);
			FileWriter out = new FileWriter(outputFile);
			int c;

			while ((c = in.read()) != -1)
				out.write(c);

			in.close();
			out.close();
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}

	}

	String Hex2Ascii(String s) {
		StringBuilder ascii = new StringBuilder();
		for (int i = 0; i < s.length(); i += 2) {
			int c = Integer.parseInt(
					s.substring(i, Math.min(i + 2, s.length())), 16);
			ascii.append(c == 0 ? '?' : (char) c);
		}
		return ascii.toString();
	}
	
	/**
	 * Execute the selected targets of the given AntRunnerComponent in a thread. Change the background
	 * color of the given button depending on the status
	 * @param antRunnerComponent
	 * @param button Background color of this button will be changed after target completed
	 */
	public void executeSelectedTargets(AntRunnerComponent antRunnerComponent, JButton button) {
		ExecuteThread t = new ExecuteThread(this, antRunnerComponent, button);
		t.start();
	}

	/**
	 * Set the color of the button to default system color.
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

	/**
	 * Execute the ant target of the given build file.
	 * 
	 * @param ant
	 *            build filename
	 * @param ant
	 *            target
	 * @return true if successfully
	 */
	public boolean executeAntTarget(String filename, String target,
			BuildListener listener) {
		boolean ret = true;
		Date dateStart = new Date();
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		File buildFile = new File(filename);
		project = new Project();
		project.setUserProperty("ant.file", buildFile.getAbsolutePath());
		// TODO remove AntLogger logger = new AntLogger();
		if (logger instanceof DefaultLogger) {
			logger.setErrorPrintStream(System.err);
			logger.setOutputPrintStream(System.out);
			logger.setMessageOutputLevel(message_output_level);
		}
		// TODO remove p.setProjectReference(logger);
		// Listeners & Loggers http://antinstaller.sourceforge.net/manual/manual/listeners.html#DefaultLogger
		project.addBuildListener(logger);
		// TODO remove p.addBuildListener(logger);
		if (listener != null)
			project.addBuildListener(listener);

		try {
			project.fireBuildStarted();
			project.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			project.addReference("ant.projectHelper", helper);
			helper.parse(project, buildFile);
			project.executeTarget(target);
			project.fireBuildFinished(null);
			Date dateEnd = new Date();
			SimpleDateFormat formatNew = new SimpleDateFormat("s.SSS");
			Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//calculate duration
			double duration = (dateEnd.getTime() - dateStart.getTime()) / 1000;
			//add to db
			statistics.addValues(formatter.format(dateEnd), filename, target, duration );
		} catch (Exception e) {
			project.fireBuildFinished(e);
			project = null;
			ret = false;
		}
		frame.setCursor(Cursor.getDefaultCursor());
		return ret;
	}

	public boolean executeAntTarget(String filename, String target) {
		return executeAntTarget(filename, target, null);
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

	/**
	 * Returns the ant description of the given target.
	 * 
	 * @param file
	 * @param target
	 * @return
	 */
	String getAntDescription(String file, String targetname) {
		String s = "";
		try {
			File buildFile = new File(ANT_FILES_PATH, file);

			if (last_descr_build_file == "" || last_descr_build_file != file) {
				ProjectHelper helper = ProjectHelper.getProjectHelper();
				antproject = new Project();
				antproject.init();
				antproject.addReference("ant.projectHelper", helper);
				last_descr_build_file = file;
				helper.parse(antproject, buildFile);
			}

			// print("target=" + (Target)p.getTaskDefinitions().get(target));
			Hashtable hash = antproject.getTargets();
			Target target= (Target) hash.get(targetname);
			if (target != null) {
				s = target.getDescription();
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		return s;
	}

	MouseMotionAdapter mouseMotionHandler = new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
			if (e.getSource() instanceof AntTaskList) {
				// buildfile.getCanonicalPath()
				System.out.println(((AntTaskList) e.getSource()).getFilename());
			}
			if (e.getSource() instanceof JList) {
				int idx = ((JList) e.getSource()).locationToIndex(e.getPoint());
				if (idx >= 0
						&& idx < ((DefaultListModel) ((JList) e.getSource())
								.getModel()).size()) {
					String s = ((DefaultListModel) ((JList) e.getSource())
							.getModel()).get(idx).toString();
					// display tooltip text
					String filename = s.substring(0, s.indexOf(";"));
					String task = s.substring(s.indexOf(";") + 1);
					String description = getAntDescription(filename, task);
					if (description != null)
						((JList) e.getSource()).setToolTipText(description);
				}
			}
		}
	};

	ActionListener toolsHandler = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == chkPoll) {
				setUpdateTimer(chkPoll.getModel().isSelected());
			} else if (event.getSource() == comboboxLogger) {
				setLoggerClass(comboboxLogger.getSelectedItem().toString());
				updateConfig();
			}
			/*if (event.getSource() == btnFileChooser) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
				}
			}*/

		}
	};

	class MyTimerTask extends TimerTask {
		private String filename;
		private String target;
		private long delay;
		private boolean checked;

		MyTimerTask(String filename, String target, long delay, boolean checked) {
			this.filename = filename;
			this.target = target;
			this.delay = delay;
			this.checked = checked;
		}

		public void run() {
			// call ant task
			executeAntTarget(filename, target);
			setUpdateTimer(checked);
		}
	}
	
	void setUpdateTimer(boolean checked) {
		if (chkPoll != null && timer_ant_file != null && timer_ant_target != null && timer_delay_sec > 5 && checked) {
			// set/start update timer
			if (timer == null)
				timer = new Timer(true);
			timer.schedule(new MyTimerTask(timer_ant_file, timer_ant_target, timer_delay_sec, checked),
					timer_delay_sec * 1000);
		} else {
			// clear update timer
			if (timer != null)
				timer.cancel();
			timer = null;
		}
	}
	
	private void setLoggerClass(String string) {
		
		try {
			Class c;
			c = Class.forName(string);
			logger = (BuildLogger)c.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	String replace(String str, String pattern, String replace) {
		int start = 0;
		int index = 0;
		StringBuffer result = new StringBuffer();

		while ((index = str.indexOf(pattern, start)) >= 0) {
			result.append(str.substring(start, index));
			result.append(replace);
			start = index + pattern.length();
		}
		result.append(str.substring(start));
		return result.toString();
	}

	void writeAntTaskfile(String filename, String task, String taskname,
			String additional) {
		String antxml = "<project name=\"Run" + taskname + "\" default=\""
				+ taskname + "\" >\r\n" + COMMENT_CREATED_BY + additional + "<target name=\""
				+ taskname + "\">\r\n" + task + "\r\n</target>\r\n</project>";
		writeFile(filename, antxml);
	}
	
	private void readSingleBuildFile(String buildfile) {
		new AntRunnerTab(this, tabbedPane,
				new File(buildfile).getName(),
				TASK_LIST,
				buildfile,
				"",
				this.cmdButtonPanel);
	}
	
	/**
	 * Hide batch list, "+" tab and tools tab.
	 */
	private void showSimpleGui() {
		configPanel.setVisible(false);
	}

	/**
	 * Read in the config file and create the specified GUI
	 * @param xmlFile
	 */
	private void readConfig(String xmlFile) {
		try {
			//store file name
			configFile = xmlFile;
			File file = new File(xmlFile);
			if (file.exists()) {
				// Create a factory
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				// Use the factory to create a builder
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(xmlFile);
				NodeList nodes;
				// Get a list of all elements in the document
				// buttons ------------------
				if (doc.getElementsByTagName("buttons").item(0) != null) {
					nodes = doc.getElementsByTagName("buttons").item(0)
							.getChildNodes();
					// System.out.println("XML Elements: ");
					for (int i = 0; i < nodes.getLength(); i++) {
						// Get element
						// Element element = (Element)list.item(i);
						// System.out.println(element.getNodeName());
						Node node = nodes.item(i);
						if (node instanceof Element) {
							// a child element to process
							Element child = (Element) node;
							AntTarget target = new AntTarget(
									child.getAttribute("name"),
									child.getAttribute("file"),
									child.getAttribute("execute"),
									child.getAttribute("description"));
							cmdButtonPanel.addAntTarget(target, false);
						}
					}
				} else {
					// no buttons -> hide panel
					cmdButtonPanel.setVisible(false);
				}
				// options ----------------------
				nodes = doc.getElementsByTagName("options");
				if (nodes.getLength() > 0) {
					//timer
					Node node = nodes.item(0);
					if (node instanceof Element) {
						// a child element to process
						Element child = (Element) node;
						loggerClass = child.getAttribute("logger");
					}
				}
				// tabs -------------------------
				nodes = doc.getElementsByTagName("tabs").item(0)
						.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node instanceof Element) {
						// a child element to process
						Element child = (Element) node;
						// System.out.println("name=" +
						// child.getAttribute("name") + ", file=" +
						// child.getAttribute("file"));
						new AntRunnerTab(this, tabbedPane,
								child.getAttribute("name"),
								child.getAttribute("type"),
								child.getAttribute("file"),
								child.getAttribute("source"),
								this.cmdButtonPanel);
					}
				}

				// timer
				nodes = doc.getElementsByTagName("timer");
				if (nodes.getLength() > 0) {
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if (node instanceof Element) {
						// a child element to process
						Element child = (Element) node;
						timer_ant_file = child.getAttribute("file");
						timer_ant_target = child.getAttribute("execute");
						timer_delay_sec = Long.parseLong(child.getAttribute("interval"));
						chkPoll.setText("Timer: " + timer_ant_file + ";" + timer_ant_target + " (" + String.valueOf(timer_delay_sec) + "sec)");
						chkPoll.setSelected(Boolean.parseBoolean(child.getAttribute("checked")));
					}
				}
				} else {
					//disable timer checkbox 
					chkPoll.setSelected(false);
					chkPoll.setEnabled(false);
				}

			} else {
				System.err.print("File " + xmlFile + " not found!");
			}
		} catch (Exception e) {
			System.err.print(e.toString());
			System.exit(1);
		}
		try {
			if (loggerClass == null || loggerClass.equals(""))
				loggerClass = "org.apache.tools.ant.DefaultLogger";
			Class c = Class.forName(loggerClass);
			logger = (BuildLogger)c.newInstance();
			comboboxLogger.setSelectedItem(logger.getClass().getCanonicalName());
		}
		catch (Exception e) {
			System.err.print(e.toString());
			System.exit(1);
		}
	}
	
	public void updateConfig() {
        File xmlFile = new File(configFile);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
             
            //update attribute value
            Node node = null;
            if (doc.getElementsByTagName("buttons").item(0) == null) {
            	//add buttons node
            	doc.getElementsByTagName("config").item(0).appendChild(doc.createElement("buttons"));
            }
            node = doc.getElementsByTagName("buttons").item(0);
            NodeList buttons = doc.getElementsByTagName("button");
            for(int i=0; i < cmdButtonPanel.getComponentCount(); i++) {
	        	Node newChild = null;
	        	AntRunnerButton btn =  (AntRunnerButton)cmdButtonPanel.getComponent(i);
	        	//search for existing button
	        	for(int j=0; j < buttons.getLength(); j++) {
	        		if (buttons.item(j).getAttributes().getNamedItem("execute").getNodeValue().equals(btn.getTarget()) &&
	        				buttons.item(j).getAttributes().getNamedItem("file").getNodeValue().equals(btn.getFilename())) {
	        			newChild = buttons.item(j);
	        			break;
	        		}
	        	}
	        	if (newChild == null) {
	        		newChild = doc.createElement("button");
	        		((Element)newChild).setAttribute("name", "" /*TODO*/);
	        		((Element)newChild).setAttribute("execute", btn.getTarget());
	        		((Element)newChild).setAttribute("file", btn.getFilename());
	        		node.appendChild(newChild);
	        	}
            }
            if (doc.getElementsByTagName("options").item(0) == null) {
            	//add config node
            	doc.getElementsByTagName("config").item(0).appendChild(doc.createElement("options"));
            }
            node = doc.getElementsByTagName("options").item(0);
            Attr attr = doc.createAttribute("logger");
            if (node.getAttributes().getNamedItem("logger") != null) {
            	attr = (Attr) node.getAttributes().getNamedItem("logger");
            } 
            attr.setNodeValue(comboboxLogger.getSelectedItem().toString());
            node.getAttributes().setNamedItem(attr);
            
            //write the updated document to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(configFile));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
             
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
	/**
	 * Check if given filename is an ant build file.
	 * @param filename
	 * @return true - ant file was supplied.
	 */
	public boolean checkAntFile(String filename) {
		File buildFile = new File(filename);
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		boolean ant_file = true;
		try {
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, buildFile);
		} catch (BuildException e) {
			ant_file = false;
		}
		return ant_file;
	}

	/**
	 * Launch the application.
	 */
	private static String[] parms;
	public static void main(String[] args) {
		parms = args;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AntRunner window = new AntRunner();
					if (parms.length == 0) {
						File f = new File(DEFAULT_CONFIG_XML);
						if (!f.exists()) {
							JFileChooser fc = new JFileChooser();
							if (JFileChooser.APPROVE_OPTION == fc
									.showOpenDialog(null)) {
								f = fc.getSelectedFile();
							}
						}
						window.readConfig(f.getAbsolutePath());
						window.AddAdvGui();
					}
					else if (window.checkAntFile(parms[0])) {
						//load ant build file
						window.readSingleBuildFile(parms[0]);
						//window.showSimpleGui();
					}
					window.frame.pack();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AntRunner() {
		initialize();
		try {
			statistics = new Statistics(STATISTICS_DB);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void AddAdvGui()
	{
		// AntTask
		moreTab = new AntRunnerTab(this, tabbedPane, "+",
				TASK_LIST, null, "",
				this.cmdButtonPanel);
		// add drag-drop handler
		moreTab.setTransferHandler(new AntRunnerFileTransferHandler(moreTab
				.getTaskList()));
		
		//add config tab
		tabbedPane.addTab("Config", configPanel);

	}
	
	public void addToBatchList(AntTarget antTarget) {
		batchList.addAntTarget(antTarget);
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame(String.format("jantrunner %s", VERSION));
		frame.getContentPane().setLayout(new BorderLayout(5, 5));

		// buttons
		cmdButtonPanel = new ButtonPanel(this);
		cmdButtonPanel.setLayout(new FlowLayout());
		frame.getContentPane().add(cmdButtonPanel, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);

		// left side
		JPanel panelFrm = new JPanel(new BorderLayout());
		// frame.getContentPane().add(panelFrm, BorderLayout.WEST);
		splitPane.setLeftComponent(panelFrm);

		batchList = new BatchList(this);
		splitPane.setRightComponent(batchList);

		tabbedPane = new JTabbedPane();
		panelFrm.add(tabbedPane, BorderLayout.CENTER);
		
		//prepare adv gui
		//tools tab		
		configPanel = new JPanel(/*new BorderLayout()*/);
		//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		
		JPanel toolsChildPanel = new JPanel();
		//toolsChildPanel.setLayout(new BorderLayout());
		toolsChildPanel.setLayout(new BoxLayout(toolsChildPanel, BoxLayout.Y_AXIS));
		
		configPanel.add(toolsChildPanel, BorderLayout.CENTER);

		chkPoll = new JCheckBox("Timer");
		chkPoll.addActionListener(toolsHandler);
		toolsChildPanel.add(chkPoll);
		

		// todo label/list ?
		//txtTimerField = new JTextField();
		//txtTimerField.setEditable(false);
		//toolsChildPanel.add(txtTimerField);
		
		toolsChildPanel.add(new JLabel("Logger: "));
		comboboxLogger = new JComboBox();
		comboboxLogger.addItem("org.apache.tools.ant.DefaultLogger");
		comboboxLogger.addItem("org.apache.tools.ant.NoBannerLogger");
		comboboxLogger.addItem("org.apache.tools.ant.XmlLogger");
		comboboxLogger.addItem("org.apache.tools.ant.listener.TimestampedLogger");
		comboboxLogger.addItem("org.apache.tools.ant.listener.ProfileLogger");
		comboboxLogger.addItem("org.apache.tools.ant.listener.BigProjectLogger");
		comboboxLogger.addItem("org.apache.tools.ant.listener.SimpleBigProjectLogger");
		toolsChildPanel.add(comboboxLogger);
		comboboxLogger.addActionListener(toolsHandler);
		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	}
}// AntRunner
