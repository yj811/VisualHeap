package org.visualheap.app;

import java.awt.EventQueue;

import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;

import com.sun.jdi.StackFrame;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JInternalFrame;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestGUI extends NullListener {

	//Variables
	private volatile Debugger debugger;
	private String classPath;
	private String className;
	private volatile boolean finished;

	private InputStreamThread istConsoleOutput;
	
	private JFrame frame;
	private JTextField edtClassPath;
	private JTextField edtClassName;
	private JTable tblBreakpoints;
	private JLabel lblLineNo;
	private JButton btnStep;
	private JButton btnResume;
	private JButton btnLoadVM;
	private JButton btnNewBreakpoint;
	private JTextArea taConsoleOutput;
	private final JFileChooser fc = new JFileChooser();

	/**
	 * Launch the application.
	 */
	
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestGUI window = new TestGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	 */
	
	/**
	 * Create the GUI.
	 * @wbp.parser.constructor
	 */
	public TestGUI() {
		initialize();
	}

	public TestGUI(Debugger debugger) {
		this.debugger = debugger;
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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 393);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

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

		JButton btnClasspath = new JButton("Select Classpath");
		btnClasspath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//In response to a button click:
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("Classpath found: " +   fc.getSelectedFile().getPath() + "\n");
					edtClassPath.setText(fc.getSelectedFile().getPath());
				}
			}
		});
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, btnClasspath, 29, SpringLayout.NORTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, btnClasspath, -29, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(btnClasspath);

		edtClassName = new JTextField();
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, edtClassName, 6, SpringLayout.SOUTH, edtClassPath);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, edtClassName, -29, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(edtClassName);
		edtClassName.setColumns(10);

		JLabel lblClassName = new JLabel("Package/Class:");
		sl_paneConfigure.putConstraint(SpringLayout.WEST, edtClassName, 6, SpringLayout.EAST, lblClassName);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, lblClassName, 2, SpringLayout.NORTH, edtClassName);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, lblClassName, 0, SpringLayout.WEST, edtClassPath);
		paneConfigure.add(lblClassName);

		btnNewBreakpoint = new JButton("Add Breakpoint");
		sl_paneConfigure.putConstraint(SpringLayout.WEST, btnNewBreakpoint, 24, SpringLayout.WEST, paneConfigure);
		paneConfigure.add(btnNewBreakpoint);

		JButton btnRemoveBreakpoint = new JButton("Delete Breakpoint");
		sl_paneConfigure.putConstraint(SpringLayout.WEST, btnRemoveBreakpoint, 6, SpringLayout.EAST, btnNewBreakpoint);
		paneConfigure.add(btnRemoveBreakpoint);

		JScrollPane scrollPane = new JScrollPane();
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, scrollPane, 150, SpringLayout.NORTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, scrollPane, 24, SpringLayout.WEST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, scrollPane, -29, SpringLayout.EAST, paneConfigure);
		paneConfigure.add(scrollPane);

		tblBreakpoints = new JTable();
		tblBreakpoints.setFillsViewportHeight(true);
		scrollPane.setViewportView(tblBreakpoints);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, tblBreakpoints, 23, SpringLayout.SOUTH, btnNewBreakpoint);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, tblBreakpoints, 378, SpringLayout.WEST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.SOUTH, tblBreakpoints, -10, SpringLayout.SOUTH, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, tblBreakpoints, 0, SpringLayout.EAST, btnClasspath);
		tblBreakpoints.setModel(new DefaultTableModel(
				new Object[][] {
						{null, null},
				},
				new String[] {
						"Line Number", "Class Name"
				}
				) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] {
					Integer.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
					false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tblBreakpoints.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblBreakpoints.getColumnModel().getColumn(0).setMinWidth(100);
		tblBreakpoints.getColumnModel().getColumn(1).setPreferredWidth(160);
		tblBreakpoints.getColumnModel().getColumn(1).setMinWidth(160);
		tblBreakpoints.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		btnLoadVM = new JButton("Load VM");
		
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, btnRemoveBreakpoint, 6, SpringLayout.SOUTH, btnLoadVM);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, btnNewBreakpoint, 6, SpringLayout.SOUTH, btnLoadVM);
		sl_paneConfigure.putConstraint(SpringLayout.WEST, btnLoadVM, 24, SpringLayout.WEST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.EAST, btnLoadVM, -29, SpringLayout.EAST, paneConfigure);
		sl_paneConfigure.putConstraint(SpringLayout.NORTH, btnLoadVM, 7, SpringLayout.SOUTH, edtClassName);
		paneConfigure.add(btnLoadVM);

		JPanel paneOutputs = new JPanel();
		tabbedPane.addTab("Program Output", null, paneOutputs, null);
		paneOutputs.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setResizeWeight(0.5);
		paneOutputs.add(splitPane);

		JScrollPane scrollPane_2 = new JScrollPane();
		splitPane.setLeftComponent(scrollPane_2);

		JTextArea taDebuggerOutput = new JTextArea();
		(new SystemOutThread(taDebuggerOutput)).start();
		scrollPane_2.setViewportView(taDebuggerOutput);

		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_1);

		
		taConsoleOutput = new JTextArea();
		scrollPane_1.setViewportView(taConsoleOutput);

		JPanel paneDebugControls = new JPanel();
		paneOutputs.add(paneDebugControls, BorderLayout.NORTH);

		btnStep = new JButton("Step");
		btnStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStep.setEnabled(false);
				debugger.step();
			}
		});
		paneDebugControls.add(btnStep);
		
		btnResume = new JButton("Resume");
		btnResume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnResume.setEnabled(false);
				btnNewBreakpoint.setEnabled(false);
				btnStep.setEnabled(false);
				debugger.resume();
			}
		});
		paneDebugControls.add(btnResume);

		

		lblLineNo = new JLabel("Line Number: 0");
		paneDebugControls.add(lblLineNo);

		JPanel paneVisual = new JPanel();
		tabbedPane.addTab("Visual", null, paneVisual, null);
		
		btnLoadVM.addActionListener(new DebugConfig(this, debugger));
	}
	
	
	public void onBreakpoint(StackFrame sf) {
		lblLineNo.setText("Line Number: " + sf.location().lineNumber());    
		btnStep.setEnabled(true);
		btnResume.setEnabled(true);
	}

	public void onStep(StackFrame sf) {
		if (finished) {
			debugger.resume();
		} else {
			lblLineNo.setText("Line Number: " + sf.location().lineNumber());    
			btnStep.setEnabled(true);
			btnResume.setEnabled(true);
		}
	}

	@Override
	public void vmDeath() {
		btnLoadVM.setEnabled(true);
		btnStep.setEnabled(false);
		btnResume.setEnabled(false);
	}

	@Override
	public void exitedMain() {
		finished = true;
	}
	
	private class DebugConfig implements ActionListener {

		final TestGUI gui;
		final Debugger deb;

		public DebugConfig(TestGUI gui, Debugger deb) {
			this.gui = gui;
			this.deb = deb;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (istConsoleOutput != null && !istConsoleOutput.finished()) {
				istConsoleOutput.finish();
			}
			istConsoleOutput = new InputStreamThread(taConsoleOutput);
			classPath = edtClassPath.getText();
			className = edtClassName.getText();
			if (deb == null) {
				System.out.println("NULL");
			}
			deb.setClassName(className);
			deb.setClassPath(classPath);
			deb.bootVM();
			deb.addListener(gui);
			istConsoleOutput.setReader(new BufferedReader(new InputStreamReader(deb.getOutput())));
			istConsoleOutput.start();
			btnNewBreakpoint.setEnabled(true);
			btnStep.setEnabled(false);
			btnResume.setEnabled(true);
			if (btnLoadVM == null) {
				System.out.println("NULL");
			}
			btnLoadVM.setEnabled(false);
			finished = false;
		}

	}

}