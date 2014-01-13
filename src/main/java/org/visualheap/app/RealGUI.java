package org.visualheap.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.StackFrame;

import org.visualheap.debugger.Breakpoint;
import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.DebugListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JSpinner;

public class RealGUI implements DebugListener {

	//Variables
	private volatile Debugger debugger;
	private volatile GUI_STATE state;
	private volatile StringBuilder finalPath;
	private InputStreamThread istConsoleOutput;
	private InputStreamThread istConsoleErrOutput;
	private String classPath;
	private String className;
	private String cmdArgs;
	private Game visualiser;
	private StackFrame currentStackFrame;

	private Set<String> classNames;
	private String cachedJarPath;
	private boolean loadingAJar;

	//Swing Components
	private JFrame frame;
	private JTextField edtClassPath;
	private JTextField edtClassName;
	private JTextField edtCmdArgs;
	private JTable tblBreakpoints;
	private JLabel lblLineNo;
	private JButton btnStep;
	private JButton btnResume;
	private JButton btnNewBreakpoint;
	private JButton btnVisualise;
	private JButton btnRemoveBreakpoint;
	private JButton btnClasspath;
	private JTextArea taConsoleOutput;
	private BreakpointTableModel tableModel;
	private JRadioButton rbtnKK;
	private JRadioButton rbtnFR;
	private JRadioButton rbtnISOM;
	private JCheckBox chkboAnimate;
	private JSpinner sedtDepth;

	//Constants
	private final JFileChooser fc = new JFileChooser();
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private enum GUI_STATE {
		UNLOADED, LOADED, STARTED, SUSPENDED, FINISHED, FINISHED_ERROR, CLOSING
	}

	/**
	 * Create the GUI.
	 * @wbp.parser.constructor
	 */
	public RealGUI() {
		finalPath = new StringBuilder();
		cachedJarPath = "";
		tableModel = new BreakpointTableModel();
		state = GUI_STATE.UNLOADED;
		initialize();
	}

	public RealGUI(Debugger debugger) {
		this.debugger = debugger;
		state = GUI_STATE.UNLOADED;

		cachedJarPath = "";
		finalPath = new StringBuilder();
		tableModel = new BreakpointTableModel();
	}

	public void show(String path, String name) {
		classPath = path;
		className = name;
		cmdArgs = "";
		show();
	}

	public void show() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initialize();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});	
	}


	public void addBreakpoint(Integer number, String className) {
		tableModel.addRow(new Object[]{number, className});
	}

	@Override
	public void onInvalidBreakpointEvent(final List<Breakpoint> bps) {
		SwingUtilities.invokeLater(new Runnable () {

			@Override
			public void run() {
				StringBuilder sb = new StringBuilder();
				sb.append("Invalid breakpoints were found:");
				sb.append('\n');
				for (Breakpoint bp : bps) {
					sb.append(bp.getClassName());

					sb.append(" : ");
					sb.append(bp.getLine());
					sb.append('\n');	
				}
				//System.out.println(sb.toString());
				JOptionPane.showMessageDialog(frame, sb.toString());
			}

		});
	}

	@Override
	public void vmStart() {
		SwingUtilities.invokeLater(new Runnable () {

			@Override
			public void run() {
				System.out.println("VM Invoked");
			}
		});
	}

	@Override
	public void onBreakpoint(final StackFrame sf) {
		SwingUtilities.invokeLater(new Runnable () {
			@Override
			public void run() {
				currentStackFrame = sf;
				try {
					lblLineNo.setText("File: " + sf.location().sourceName() + " - Line Number: " + sf.location().lineNumber());
				} catch (AbsentInformationException e) {
					lblLineNo.setText("File: Unknown - Line Number: " + sf.location().lineNumber());
				} 
				state = GUI_STATE.SUSPENDED;
				setButtonsByState();
			}
		});
	}

	@Override
	public void onStep(final StackFrame sf) {
		SwingUtilities.invokeLater(new Runnable () {
			@Override
			public void run() {
				if (state.equals(GUI_STATE.FINISHED)) {
					debugger.resume();
				} else {
					currentStackFrame = sf;
					try {
						lblLineNo.setText("File: " + sf.location().sourceName() + " - Line Number: " + sf.location().lineNumber());
					} catch (AbsentInformationException e) {
						lblLineNo.setText("File: Unknown - Line Number: " + sf.location().lineNumber());
					}    

					state = GUI_STATE.SUSPENDED;
					setButtonsByState();

					if (visualiser != null && visualiser.isRunning()) {
						//TODO: If there is time, update the visualiser window after each step
						visualiser.sync(null);
					}
				}
			}
		});
	}

	@Override
	public void vmDeath() {
		SwingUtilities.invokeLater(new Runnable () {
			@Override
			public void run() {
				if (state.equals(GUI_STATE.STARTED)) {
					state = GUI_STATE.FINISHED_ERROR;
				}

				if (state.equals(GUI_STATE.FINISHED) || state.equals(GUI_STATE.FINISHED_ERROR)) {
					state = GUI_STATE.LOADED;
					setButtonsByState();
					prepareVM();
				} else {
					state = GUI_STATE.LOADED;
					setButtonsByState();
				}
			}
		});
	}

	@Override
	public void exitedMain() {
		state = GUI_STATE.FINISHED;
		setButtonsByState();
	}

	private void setButtonsByState() {
		switch (state) {
		case FINISHED_ERROR:
		case FINISHED:
			btnNewBreakpoint.setEnabled(true);
			btnRemoveBreakpoint.setEnabled(true);
			btnClasspath.setEnabled(true);
			btnVisualise.setEnabled(false);
			btnStep.setEnabled(false);
			btnResume.setEnabled(false);

			break;
		case LOADED:
			btnNewBreakpoint.setEnabled(true);
			btnRemoveBreakpoint.setEnabled(true);
			btnClasspath.setEnabled(true);
			btnStep.setEnabled(false);
			btnResume.setEnabled(true);
			btnResume.setText("Run");
			btnVisualise.setEnabled(false);
			break;
		case STARTED:
			btnResume.setEnabled(false);
			btnNewBreakpoint.setEnabled(false);
			btnRemoveBreakpoint.setEnabled(false);
			btnClasspath.setEnabled(false);
			btnStep.setEnabled(false);
			btnVisualise.setEnabled(false);
			break;
		case SUSPENDED:
			btnStep.setEnabled(true);
			btnResume.setEnabled(true);
			btnNewBreakpoint.setEnabled(false);
			btnClasspath.setEnabled(false);
			btnRemoveBreakpoint.setEnabled(false);
			btnResume.setText("Resume");
			btnVisualise.setEnabled(true);
			break;
		case UNLOADED:
			btnStep.setEnabled(false);
			btnResume.setEnabled(false);
			btnNewBreakpoint.setEnabled(false);
			btnRemoveBreakpoint.setEnabled(false);
			btnClasspath.setEnabled(true);
			btnVisualise.setEnabled(false);
			break;
		default:
			break;
		}
	}

	private void prepareVM() {
		if (istConsoleOutput != null && !istConsoleOutput.finished()) {
			istConsoleOutput.finish();
		}
		istConsoleOutput = new InputStreamThread(taConsoleOutput);
		istConsoleErrOutput = new InputStreamThread(taConsoleOutput);
		classPath = edtClassPath.getText();
		className = edtClassName.getText();
		cmdArgs = edtCmdArgs.getText();
		if (debugger == null) {
			System.out.println("NULL");
		}
		debugger.setClassName(className);
		debugger.setClassPath(classPath);
		debugger.setCmdArgs(cmdArgs);
		debugger.kill();
		debugger.bootVM();
		debugger.addListener(this);
		istConsoleOutput.setReader(new BufferedReader(new InputStreamReader(debugger.getOutput())));
		istConsoleOutput.start();
		istConsoleErrOutput.setReader(new BufferedReader(new InputStreamReader(debugger.getErrOutput())));
		istConsoleErrOutput.start();

		state = GUI_STATE.LOADED;
		setButtonsByState();
	}

	private class PathFieldListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			buildUpdate(e);
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			buildUpdate(e);
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		private void buildUpdate(DocumentEvent e) {
			finalPath.setLength(0);
			finalPath.append(edtClassPath.getText());
			finalPath.append("/");
			finalPath.append(edtClassName.getText().replaceAll("\\Q.\\E", "/"));
			finalPath.append(".class");
			// if the new string results in a final product, load the VM auto-magically.

			File f = new File(finalPath.toString());
			if(f.exists() && f.isFile()) {
				prepareVM();
				loadingAJar = false;
			} else {
				finalPath.setLength(0);
				finalPath.append(edtClassPath.getText());
				File j = new File(finalPath.toString());
				if (edtClassName.getText().isEmpty()) return;
				if (edtClassPath.getText().isEmpty()) return;
				if(j.exists()  && j.isFile() && classExistsInJAR(edtClassPath.getText(), edtClassName.getText())) { 
					prepareVM();
					loadingAJar = true;
				} else {
					state = GUI_STATE.UNLOADED;
					setButtonsByState();
					debugger.kill();
				}
			}
		}
	} 

	private boolean classExistsInJAR(String jarPath, String searchClass) {
		//Quick fix, Adapted from here: http://stackoverflow.com/a/15720973
		if (cachedJarPath != null && !cachedJarPath.equals(jarPath)) {
			cachedJarPath = jarPath;
			classNames = new HashSet<String>();
			ZipInputStream zip;
			try {
				zip = new ZipInputStream(new FileInputStream(jarPath));

				for(ZipEntry entry=zip.getNextEntry(); entry!=null; entry=zip.getNextEntry()) {
					if(entry.getName().endsWith(".class") && !entry.isDirectory()) {
						// This ZipEntry represents a class. Now, what class does it represent?
						StringBuilder className = new StringBuilder();
						for(String part : entry.getName().split("/")) {
							if(className.length() != 0) {
								className.append(".");
							}
							className.append(part);
							if(part.endsWith(".class")) {
								className.setLength(className.length()-".class".length());
							}
						}
						classNames.add(className.toString());
					}
				}
				zip.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return classNames.contains(searchClass);
	}

	private class ClassPathButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//In response to a button click:
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Classpath found: " +   fc.getSelectedFile().getPath() + "\n");
				edtClassPath.setText(fc.getSelectedFile().getPath());
			}
		}
	}

	private class NewBreakpointButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			tableModel.addRow(new Object[] { null, className });
		}
	}

	private class RemoveBreakpointButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (tblBreakpoints.getSelectedRow() > -1) {
				tableModel.removeRow(tblBreakpoints.getSelectedRow());
			}
		}
	}

	private class StepButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			state = GUI_STATE.STARTED;
			setButtonsByState();
			debugger.step();
		}
	}

	private class VisualiseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (visualiser == null || !visualiser.isRunning() ) {
				Integer depth = (Integer)sedtDepth.getValue();

				if (rbtnKK.isSelected()) {
					visualiser = new Game(depth, Game.LAYOUT.KK);
					System.err.println("1");
				} else  if (rbtnFR.isSelected()) {
					visualiser = new Game(depth, Game.LAYOUT.FR, chkboAnimate.isSelected());
					System.err.println("2");
				} else  if (rbtnISOM.isSelected()) {
					visualiser = new Game(depth, Game.LAYOUT.ISOM, chkboAnimate.isSelected());
					System.err.println("3");
				} else {
					visualiser = new Game();
					System.err.println("4");
				}

				visualiser.beginGame(debugger.getObjectReferencesFromStackFrame(currentStackFrame), debugger);
			}
		}
	}

	private class ResumeButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (state.equals(GUI_STATE.LOADED)) {
				//Load breakpoints to the debugger;
				int size = tableModel.getRowCount();
				List<Breakpoint> invalidBPS = new ArrayList<Breakpoint>();
				for (int i = 0; i < size; i++) {
					Vector<?> row = (Vector<?>)tableModel.getDataVector().elementAt(i);
					if (!row.elementAt(0).equals(null) && !row.elementAt(1).equals(null)) {
						boolean breakpointedClassFileExists = true;
						String breakpointClass = (String)row.elementAt(1);
						Integer breakpointLine = (Integer)row.elementAt(0);
						if (loadingAJar) {

							breakpointedClassFileExists = classExistsInJAR(edtClassPath.getText(),(String)row.elementAt(1));
						} else {
							StringBuilder sb = new StringBuilder();
							sb.append(edtClassPath.getText());
							sb.append("/");
							sb.append(breakpointClass.replaceAll("\\Q.\\E", "/"));
							sb.append(".class");
							File f = new File(sb.toString());
							breakpointedClassFileExists = (f.exists() && f.isFile());
						}
						if (!breakpointedClassFileExists) {

							invalidBPS.add(new Breakpoint(breakpointClass, breakpointLine));
						} else {

							debugger.addBreakpoint(breakpointClass, breakpointLine);
						}
					}
				}
				if (invalidBPS.size() > 0) {
					onInvalidBreakpointEvent(invalidBPS);
					return;
				}
			}

			state = GUI_STATE.STARTED;
			setButtonsByState();

			debugger.resume();
		}
	}

	/*********************************************************************************************
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * Initialize the contents of the GUI.
	 * Avoid changing this method unless you are using WindowBuilder or another compatible Swing GUI editor.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				state = GUI_STATE.CLOSING;
				debugger.kill();
				if (visualiser != null && visualiser.isRunning()) {
					visualiser.stop();
				}
			}
		});

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane);

		JPanel paneConfigure = new JPanel();
		tabbedPane.addTab("Configure", null, paneConfigure, null);
		SpringLayout sl_paneConfigure = new SpringLayout();
		paneConfigure.setLayout(sl_paneConfigure);

		edtClassPath = new JTextField();
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, edtClassPath, 32, SpringLayout.NORTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, edtClassPath, 24, SpringLayout.WEST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, edtClassPath, -188, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(edtClassPath);
		edtClassPath.setColumns(10);

		btnClasspath = new JButton("Select Classpath");
		btnClasspath.addActionListener(new ClassPathButtonListener());
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, btnClasspath, 29, SpringLayout.NORTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, btnClasspath, -29, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(btnClasspath);

		edtClassName = new JTextField();
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, edtClassName, 6, SpringLayout.SOUTH, edtClassPath);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, edtClassName, -29, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(edtClassName);
		edtClassName.setColumns(10);

		JLabel lblClassName = new JLabel("Qualified Class Name:");
		sl_paneConfigure.putConstraint(SpringLayout.WEST, edtClassName, 6, SpringLayout.EAST, lblClassName);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, lblClassName, 2, SpringLayout.NORTH, edtClassName);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, lblClassName, 0, SpringLayout.WEST, edtClassPath);
		paneConfigure.add(lblClassName);

		btnNewBreakpoint = new JButton("Add Breakpoint");
		sl_paneConfigure.putConstraint(SpringLayout.WEST, btnNewBreakpoint, 0, SpringLayout.WEST, edtClassPath);
		btnNewBreakpoint.addActionListener(new NewBreakpointButtonListener());
		paneConfigure.add(btnNewBreakpoint);

		btnRemoveBreakpoint = new JButton("Delete Breakpoint");
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, btnRemoveBreakpoint, 0, SpringLayout.NORTH, btnNewBreakpoint);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, btnRemoveBreakpoint, 0, SpringLayout.WEST, edtClassName);
		btnRemoveBreakpoint.addActionListener(new RemoveBreakpointButtonListener());
		paneConfigure.add(btnRemoveBreakpoint);

		JScrollPane scrollPane = new JScrollPane();
		sl_paneConfigure.putConstraint(SpringLayout.SOUTH, btnNewBreakpoint, -6, SpringLayout.NORTH, scrollPane);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, scrollPane, 155, SpringLayout.NORTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, scrollPane, 24, SpringLayout.WEST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, scrollPane, -29, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(scrollPane);

		tblBreakpoints = new JTable();
		tblBreakpoints.setFillsViewportHeight(true);
		scrollPane.setViewportView(tblBreakpoints);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, tblBreakpoints, 23, SpringLayout.SOUTH, btnNewBreakpoint);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, tblBreakpoints, 378, SpringLayout.WEST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.SOUTH, tblBreakpoints, -10, SpringLayout.SOUTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, tblBreakpoints, 0, SpringLayout.EAST, btnClasspath);

		tblBreakpoints.setModel(tableModel);
		tblBreakpoints.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblBreakpoints.getColumnModel().getColumn(0).setMinWidth(100);
		tblBreakpoints.getColumnModel().getColumn(1).setPreferredWidth(160);
		tblBreakpoints.getColumnModel().getColumn(1).setMinWidth(160);
		tblBreakpoints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JPanel paneOutputs = new JPanel();
		tabbedPane.addTab("Program Output", null, paneOutputs, null);
		paneOutputs.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setResizeWeight(0.5);
		paneOutputs.add(splitPane);

		JPanel paneDebugger = new JPanel();
		splitPane.setLeftComponent(paneDebugger);
		paneDebugger.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_2 = new JScrollPane();
		paneDebugger.add(scrollPane_2, BorderLayout.CENTER);

		JTextArea taDebuggerOutput = new JTextArea();
		(new SystemOutThread(taDebuggerOutput)).start();
		scrollPane_2.setViewportView(taDebuggerOutput);

		JPanel paneDebuggerOutput = new JPanel();
		scrollPane_2.setColumnHeaderView(paneDebuggerOutput);

		JLabel lblDebuggerOutput = new JLabel("Debugger Output:");
		paneDebuggerOutput.add(lblDebuggerOutput);

		JPanel paneProgramOutput = new JPanel();
		splitPane.setRightComponent(paneProgramOutput);
		paneProgramOutput.setLayout(new BorderLayout(0, 0));

		JPanel paneProgramLabel = new JPanel();
		paneProgramOutput.add(paneProgramLabel, BorderLayout.NORTH);

		JLabel lblOutput = new JLabel("Program Output");
		paneProgramLabel.add(lblOutput);

		JScrollPane scrollPane_1 = new JScrollPane();
		paneProgramOutput.add(scrollPane_1, BorderLayout.CENTER);

		taConsoleOutput = new JTextArea();
		scrollPane_1.setViewportView(taConsoleOutput);

		JPanel paneDebugControls = new JPanel();
		paneOutputs.add(paneDebugControls, BorderLayout.NORTH);

		btnStep = new JButton("Step");
		btnStep.addActionListener(new StepButtonListener());
		paneDebugControls.add(btnStep);

		btnResume = new JButton("Resume");
		btnResume.addActionListener(new ResumeButtonListener());
		paneDebugControls.add(btnResume);

		lblLineNo = new JLabel("Line Number: 0");
		paneDebugControls.add(lblLineNo);

		JPanel panel = new JPanel();
		paneOutputs.add(panel, BorderLayout.SOUTH);

		rbtnISOM = new JRadioButton("ISOM");
		rbtnISOM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkboAnimate.setEnabled(true);
			}
		});

		JLabel lblRenderDepth = new JLabel("Render Depth:");
		panel.add(lblRenderDepth);

		sedtDepth = new JSpinner();
		panel.add(sedtDepth);

		SpinnerModel depthModel =
				new SpinnerNumberModel(3, //initial value
						0, //min
						null, //max
						1); //step
		sedtDepth.setModel(depthModel);
		JComponent spinnerEditor = ((JSpinner.DefaultEditor) sedtDepth.getEditor());
		Dimension prefSize = spinnerEditor.getPreferredSize();
		prefSize = new Dimension(50, prefSize.height);
		spinnerEditor.setPreferredSize(prefSize);


		btnVisualise = new JButton("Visualise");
		panel.add(btnVisualise);
		prefSize = btnVisualise.getPreferredSize();
		prefSize = new Dimension(200, prefSize.height);
		btnVisualise.setPreferredSize(prefSize);

		btnVisualise.addActionListener(new VisualiseButtonListener());
		buttonGroup.add(rbtnISOM);
		rbtnISOM.setSelected(true);
		panel.add(rbtnISOM);

		rbtnFR = new JRadioButton("FR");
		rbtnFR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chkboAnimate.setEnabled(true);
			}
		});
		buttonGroup.add(rbtnFR);
		panel.add(rbtnFR);

		rbtnKK = new JRadioButton("KK");
		rbtnKK.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				chkboAnimate.setEnabled(false);  
			}
		});
		buttonGroup.add(rbtnKK);
		panel.add(rbtnKK);



		chkboAnimate = new JCheckBox("Animate?");
		panel.add(chkboAnimate);

		edtClassName.getDocument().addDocumentListener(new PathFieldListener());
		edtClassPath.getDocument().addDocumentListener(new PathFieldListener());

		edtCmdArgs = new JTextField();
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, edtCmdArgs, 6, SpringLayout.SOUTH, edtClassName);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, edtCmdArgs, -73, SpringLayout.WEST, edtClassName);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, edtCmdArgs, 0, SpringLayout.EAST, btnClasspath);
		edtCmdArgs.setText((String) null);
		edtCmdArgs.setColumns(10);
		edtCmdArgs.getDocument().addDocumentListener(new PathFieldListener());
		paneConfigure.add(edtCmdArgs);

		edtClassPath.setText(classPath);
		edtClassName.setText(className);

		JLabel lblArguments = new JLabel("Arguments:");
		sl_paneConfigure.putConstraint(SpringLayout.WEST, lblArguments, 0, SpringLayout.WEST, edtClassPath);
		sl_paneConfigure.putConstraint(SpringLayout.SOUTH, lblArguments, -27, SpringLayout.NORTH, btnNewBreakpoint);
		paneConfigure.add(lblArguments);

		setButtonsByState();
	}

	private class BreakpointTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("rawtypes")
		Class[] columnTypes = new Class[] {
			Integer.class, String.class
		};

		boolean[] columnEditables = new boolean[] {
				true, true
		};

		/**
		 * 
		 */
		public BreakpointTableModel() {
			super(new String[] {
					"Line Number", "Class Name"
			}, 0);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int columnIndex) {
			return columnTypes[columnIndex];
		}

		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
	}
}
