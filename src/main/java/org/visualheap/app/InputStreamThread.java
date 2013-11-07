package org.visualheap.app;

import java.io.*;
import javax.swing.*;

public class InputStreamThread extends Thread{
  
    private JTextArea area;
    private BufferedReader br;
    private volatile boolean finished;
  
    public InputStreamThread(JTextArea area) {
		this.area = area;	
	}

	public void setReader(BufferedReader in) {
        br = in;

		if (in == null)
            System.out.println("NULL READER");
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
			while (!finished) {
				if (br.ready())
					area.append(br.readLine() + "\n");
			}
	    } catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
