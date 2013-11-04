package org.visualheap.app;

import java.io.*;
import javax.swing.*;

public class InputStreamThread extends Thread{
  
	private JTextArea area;
  private volatile boolean finished;
 
  
  public InputStreamThread(JTextArea area) {
		this.area = area;	
	}

	public void finish() {
    finished = false;
	}

	public boolean finished() {
    return finished;
	}

	
	public void run() {
		finished = false;
		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(debugger.getOutput()));
			while (!finished) {
				if (br.ready()) { 
					area.append(br.readLine() + "\n");
					}
	    }
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
