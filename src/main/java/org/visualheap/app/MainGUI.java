package org.visualheap.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.visualheap.debugger.*;
import org.visualheap.app.InputStreamThread;
import com.sun.jdi.*;
import java.util.List;

import java.io.*;

public class MainGUI {

  //Variables
	private Debugger debugger;
  private String classPath;
	private String className;
  
	//GUI Components
	private final JTextArea taConsoleOutput = new JTextArea();
  private final JTextArea taDebuggerOutput = new JTextArea();
  private final JFileChooser fc = new JFileChooser();
  private InputStreamThread istConsoleOutput;
  private InputStreamThread istDebuggerOutput;
  

  public MainGUI(Debugger debugger) {
		this.debugger = debugger;
	}

	public void show() {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
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
    

    JButton button = new JButton("Choose classpath");
		button.setPreferredSize(new Dimension(150, 40));
		button.addActionListener(new ActionListener() {
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
    fileSelectPane.add(button);

    final JTextField edtClassName = new JTextField();
		fileSelectPane.add(edtClassName);
		
		final JPanel toolbarPane = new JPanel();
		toolbarPane.setLayout(new BoxLayout(toolbarPane, BoxLayout.X_AXIS));
		toolbarPane.setPreferredSize(new Dimension(40,40));
		fileSelectPane.add(toolbarPane);
   
		final JButton btnResume = new JButton ("Resume");
		toolbarPane.add(btnResume);
    btnResume.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
			  debugger.resume();
			}
		});
		
		toolbarPane.add(btnResume);

		final JSpinner spinLine = new JSpinner();
		spinLine.setPreferredSize(new Dimension(40,40));
		toolbarPane.add(spinLine);


	  
		final JButton btnSetBreak = new JButton ("Set Breakpoint");
		toolbarPane.add(btnSetBreak);
    btnSetBreak.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
				System.out.println("Added breakpoint at line " + spinLine.getValue());
        debugger.addBreakpoint(edtClassName.getText(), (Integer) spinLine.getValue());
			}
		});
		
		final JLabel lblLineNo = new JLabel("Line Number: ");
		toolbarPane.add(lblLineNo);
		final JButton btnStep = new JButton ("Step");
		toolbarPane.add(btnStep);
    btnStep.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        debugger.step();
			}
		});

		
		JButton btnDebug = new JButton("Debug");
   	btnDebug.setPreferredSize(new Dimension(450, 40));
		btnDebug.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				  if (istConsoleOutput != null && !istConsoleOutput.finished()) {
            istConsoleOutput.finish();
					}
		      istConsoleOutput = new InputStreamThread(taConsoleOutput);
					className = edtClassName.getText();
					debugger.setClassName(className);
					debugger.setClassPath(classPath);
					debugger.bootVM();
			    istConsoleOutput.setReader(new BufferedReader(new InputStreamReader(debugger.getOutput())));
					istConsoleOutput.start();
				}
		});
    frame.getContentPane().add(btnDebug, BorderLayout.PAGE_END);


		frame.pack();
		frame.setVisible(true);
	}

}
