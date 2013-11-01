package org.visualheap.debugger;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.visualheap.debugger.*;
import com.sun.jdi.*;
import java.util.List;

import java.io.*;

public class MainApplication {

  private static final JTextArea taConsoleOutput = new JTextArea();
  private static final JTextArea taDebuggerOutput = new JTextArea();
  private static final JFileChooser fc = new JFileChooser();
  private static Debugger debugger;


  private static void buildGUI() {

		JFrame frame = new JFrame("Visual Heap Analyser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
		JPanel contentPane = new JPanel(new BorderLayout());
		frame.setContentPane(contentPane);

		JPanel consolePane = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		contentPane.add(consolePane,BorderLayout.CENTER);

		JScrollPane debuggerScrollPane = new JScrollPane(taDebuggerOutput);
		debuggerScrollPane.setPreferredSize(new Dimension(200, 180));
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx=0.5;
		constraints.weighty=0.5;
		constraints.gridy=0;
		constraints.gridx=0;
    consolePane.add(debuggerScrollPane, constraints);


		Thread d = new Thread() {
			public void run() {
				try {
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
		consoleScrollPane.setPreferredSize(new Dimension(200, 180));
		constraints.gridx=1;
    consolePane.add(consoleScrollPane, constraints);

    JButton button = new JButton("Choose class");
		button.setPreferredSize(new Dimension(150, 40));
		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				//In response to a button click:
				
				int returnVal = fc.showOpenDialog(null);
			  if (returnVal == JFileChooser.APPROVE_OPTION) {
          taConsoleOutput.append("File found: " +   fc.getSelectedFile().getPath() + "\n");
				}
			}
		});
    frame.getContentPane().add(button, BorderLayout.PAGE_START);

    JButton btnDebug = new JButton("Debug");
		btnDebug.setPreferredSize(new Dimension(150, 40));
		btnDebug.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				  debugger = new Debugger("build/classes/test", "debugger.testprogs.Array", 12, new TestDebugListener() {
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
