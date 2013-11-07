package org.visualheap.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.visualheap.debugger.Debugger;
import org.visualheap.debugger.NullListener;

import com.sun.jdi.StackFrame;


public class MainGUI extends NullListener {

	//Variables
	private Debugger debugger;
	private String classPath;
	private String className;
	private volatile boolean finished;

	//GUI Components
	private final JTextArea taConsoleOutput = new JTextArea();
	private final JTextArea taDebuggerOutput = new JTextArea();
	private final JFileChooser fc = new JFileChooser();
	private InputStreamThread istConsoleOutput;
	private InputStreamThread istDebuggerOutput;
	private final JLabel lblLineNo = new JLabel("Line Number: ");
	private	final JTextField edtClassName = new JTextField();
	private	final JButton btnStep = new JButton ("Step");
	private	final JButton btnResume = new JButton ("Resume");
	private final JButton btnLoad = new JButton("Load Program");
	private	final JButton btnSetBreak = new JButton ("Set Breakpoint");


	public MainGUI(Debugger debugger) {
		this.debugger = debugger;
	}


	/**
	 * Displays the gui which is built by constructing the MainGUI object.
	 * 
	 */
	public void show() {
		//Schedule a job for the event-dispatching thread:
		//creating and showing thiFs application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				buildGUI();
			}
		});

	}



	private void buildGUI() {

		//Generate the GUI root frame
		JFrame frame = new JFrame("Visual Heap Analyser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Base pane to add content to
		JPanel contentPane = new JPanel(new BorderLayout());
		frame.setContentPane(contentPane);

		//Creates the pane containing the console and debugger outputs
		JPanel consolePane = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		contentPane.add(consolePane,BorderLayout.CENTER);

		JScrollPane debuggerScrollPane = new JScrollPane(taDebuggerOutput);
		debuggerScrollPane.setPreferredSize(new Dimension(300, 380));
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx=0.5;
		constraints.weighty=0.5;
		constraints.gridy=0;
		constraints.gridx=0;
		consolePane.add(debuggerScrollPane, constraints);

		//This thread sets up the MainApplication's output and 
		//error streams to the taDebuggerOutput.

		Thread d = new Thread() {
			public void run() {
				try {
					System.setErr(System.out);
					PipedOutputStream pOut = new PipedOutputStream();
					System.setOut(new PrintStream(pOut));
					PipedInputStream pIn = new PipedInputStream(pOut);
					BufferedReader br = new BufferedReader(new InputStreamReader(pIn));
					while (true) {
						if (br.ready()) taDebuggerOutput.append(br.readLine() + "\n");
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		};
		d.start();


		JScrollPane consoleScrollPane = new JScrollPane(taConsoleOutput);
		consoleScrollPane.setPreferredSize(new Dimension(300, 380));
		constraints.gridx=1;
		consolePane.add(consoleScrollPane, constraints);

		JPanel fileSelectPane = new JPanel();
		fileSelectPane.setLayout(new BoxLayout(fileSelectPane, BoxLayout.Y_AXIS));
		frame.getContentPane().add(fileSelectPane, BorderLayout.NORTH);


		JButton btnClasspath = new JButton("Choose classpath");
		//button.setPreferredSize(new Dimension(150, 40));
		btnClasspath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//In response to a button click:
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					taConsoleOutput.append("Classpath found: " +   fc.getSelectedFile().getPath() + "\n");
					classPath = fc.getSelectedFile().getPath();
				}
			}
		});
		//frame.getContentPane().add(button, BorderLayout.BEFORE_FIRST_LINE);
		fileSelectPane.add(btnClasspath);

		fileSelectPane.add(edtClassName);

		final JPanel toolbarPane = new JPanel();
		toolbarPane.setLayout(new BoxLayout(toolbarPane, BoxLayout.X_AXIS));
		toolbarPane.setPreferredSize(new Dimension(40,40));
		fileSelectPane.add(toolbarPane);

		btnResume.setEnabled(false);
		toolbarPane.add(btnResume);
		btnResume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnResume.setEnabled(false);
				btnSetBreak.setEnabled(false);
				btnStep.setEnabled(false);

				debugger.resume();
			}
		});

		toolbarPane.add(btnResume);

		final JSpinner spinLine = new JSpinner();
		spinLine.setPreferredSize(new Dimension(40,40));
		toolbarPane.add(spinLine);



		btnSetBreak.setEnabled(false);
		toolbarPane.add(btnSetBreak);
		btnSetBreak.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Added breakpoint at line " + spinLine.getValue());
				debugger.addBreakpoint(edtClassName.getText(), (Integer) spinLine.getValue());
			}
		});

		toolbarPane.add(lblLineNo);
		toolbarPane.add(btnStep);
		btnStep.setEnabled(false);
		btnStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStep.setEnabled(false);
				debugger.step();
			}
		});


		btnLoad.setPreferredSize(new Dimension(450, 40));
		btnLoad.addActionListener(new DebugConfig(this));
		frame.getContentPane().add(btnLoad, BorderLayout.PAGE_END);


		frame.pack();
		frame.setVisible(true);
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
		btnLoad.setEnabled(true);
		btnStep.setEnabled(false);
		btnResume.setEnabled(false);
	}

	@Override
	public void exitedMain() {
		finished = true;
	}


	private class DebugConfig implements ActionListener {

		final MainGUI gui;

		public DebugConfig(MainGUI gui) {
			this.gui = gui;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (istConsoleOutput != null && !istConsoleOutput.finished()) {
				istConsoleOutput.finish();
			}
			istConsoleOutput = new InputStreamThread(taConsoleOutput);
			className = edtClassName.getText();
			debugger.setClassName(className);
			debugger.setClassPath(classPath);
			debugger.bootVM();
			debugger.addListener(gui);
			istConsoleOutput.setReader(new BufferedReader(new InputStreamReader(debugger.getOutput())));
			istConsoleOutput.start();
			btnSetBreak.setEnabled(true);
			btnStep.setEnabled(false);
			btnResume.setEnabled(true);
			btnLoad.setEnabled(false);
			finished = false;
		}

	}

}
