package org.visualheap.app;

import java.util.Vector;
import java.awt.EventQueue;

import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;
import org.visualheap.world.display.Display;

import com.sun.jdi.StackFrame;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


public class TestGUI extends NullListener {

	//Variables
	private volatile Debugger debugger;
	private String classPath;
	private String className;
	private volatile GUI_STATE state;
	private volatile StringBuilder finalPath;

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
	private BreakpointTableModel tableModel;
	
	private JPanel paneVisual;
	private Display game;

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
	    finalPath = new StringBuilder();
		initialize();
	}

	public TestGUI(Debugger debugger) {
		this.debugger = debugger;
		state = GUI_STATE.UNLOADED;
		finalPath = new StringBuilder();
	}
	
	public void show() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initialize();
					frame.setVisible(true);
					game = new Display(paneVisual.getWidth(), paneVisual.getHeight());
					paneVisual.add(game);
					
					//game.start();
					
					tableModel.addRow(new Object[]{ new Integer(12), "debugger.testprogs.Array"});
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
		btnNewBreakpoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.addRow(new Object[] { null, className });
			}
		});
		sl_paneConfigure.putConstraint(SpringLayout.WEST, btnNewBreakpoint, 24, SpringLayout.WEST, paneConfigure);
		paneConfigure.add(btnNewBreakpoint);

		JButton btnRemoveBreakpoint = new JButton("Delete Breakpoint");
		btnRemoveBreakpoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tblBreakpoints.getSelectedRow() > -1) {
					tableModel.removeRow(tblBreakpoints.getSelectedRow());
				}
			}
		});
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
		tableModel = new BreakpointTableModel();
				
		tblBreakpoints.setModel(tableModel);
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
				if (state.equals(GUI_STATE.LOADED)) {
					//Load breakpoints to the debugger;
					int size = tableModel.getRowCount();
					for (int i = 0; i < size; i++) {
						Vector<? extends Object> row = (Vector<? extends Object>)tableModel.getDataVector().elementAt(i);
						if (!row.elementAt(0).equals(null) && !row.elementAt(1).equals(null)) {
							debugger.addBreakpoint((String)row.elementAt(1), (Integer)row.elementAt(0));
						}
					}
				}
					
				state = GUI_STATE.STARTED;
				btnResume.setEnabled(false);
				btnNewBreakpoint.setEnabled(false);
				btnStep.setEnabled(false);
				debugger.resume();
			}
		});
		paneDebugControls.add(btnResume);

		

		lblLineNo = new JLabel("Line Number: 0");
		paneDebugControls.add(lblLineNo);

		paneVisual = new JPanel();
		tabbedPane.addTab("Visual", null, paneVisual, null);
		paneVisual.setVisible(true);
		
		
		
		
		edtClassName.getDocument().addDocumentListener(new PathFieldListener());
		edtClassPath.getDocument().addDocumentListener(new PathFieldListener());
		
		btnLoadVM.addActionListener(new DebugConfig(this, debugger));
	}
	
	
	public void onBreakpoint(StackFrame sf) {
		lblLineNo.setText("Line Number: " + sf.location().lineNumber());    
		btnStep.setEnabled(true);
		btnResume.setEnabled(true);
		state = GUI_STATE.SUSPENDED;
	}

	public void onStep(StackFrame sf) {
		if (state.equals(GUI_STATE.FINISHED)) {
			debugger.resume();
		} else {
			lblLineNo.setText("Line Number: " + sf.location().lineNumber());    
			btnStep.setEnabled(true);
			btnResume.setEnabled(true);
			state = GUI_STATE.SUSPENDED;
		}
	}

	@Override
	public void vmDeath() {
		btnLoadVM.setEnabled(true);
		btnStep.setEnabled(false);
		btnResume.setEnabled(false);
		state = GUI_STATE.UNLOADED;
		btnLoadVM.doClick();
	}

	@Override
	public void exitedMain() {
		state = GUI_STATE.FINISHED;
	}
	
	private enum GUI_STATE {
		UNLOADED, LOADED, STARTED,SUSPENDED, FINISHED
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
			state = GUI_STATE.LOADED;
		}

	}
	
	private class BreakpointTableModel extends DefaultTableModel {
		
		private static final long serialVersionUID = 1L;
			
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
			
		public Class getColumnClass(int columnIndex) {
			return columnTypes[columnIndex];
		}
			
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
		
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
            //if the new string results in a final product, load the VM automagically.
            
            File f = new File(finalPath.toString());
            if(f.exists()) { 
                btnLoadVM.doClick();
            }
        }
        
        
    } 

}
