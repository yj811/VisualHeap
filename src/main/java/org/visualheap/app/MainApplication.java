package org.visualheap.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.visualheap.debugger.*;
import com.sun.jdi.*;
import java.util.List;

import java.io.*;

public class MainApplication {

  //Variables
	private static Debugger debugger;
  private static String classPath;
	private static String className;
  
	//GUI Components
	private static final JTextArea taConsoleOutput = new JTextArea();
  private static final JTextArea taDebuggerOutput = new JTextArea();
  private static final JFileChooser fc = new JFileChooser();
  

  private static void buildGUI() {

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
    
		
		JButton btnDebug = new JButton("Debug");
		btnDebug.setPreferredSize(new Dimension(150, 40));
		btnDebug.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				  className = edtClassName.getText();
				  debugger = new Debugger(classPath, className, 12, new TestDebugListener() {
					  public void onBreakpoint(List<ObjectReference> ref) {
						  super.onBreakpoint(ref);
							System.out.println("BREAKPOINT, resuming....");
							debugger.resume();	
						}
					});			
		      Thread t = new Thread() {
						public void run() {
							try {
								BufferedReader br = new BufferedReader(new InputStreamReader(debugger.getOutput()));
								while (true) {
									if (br.ready()) taConsoleOutput.append(br.readLine() + "\n");
								}
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}
					};
					t.start();
				}
		});
    frame.getContentPane().add(btnDebug, BorderLayout.PAGE_END);


		frame.pack();
		frame.setVisible(true);
	}


	public static void main (String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		    buildGUI();
		  }
		});
	}

}
