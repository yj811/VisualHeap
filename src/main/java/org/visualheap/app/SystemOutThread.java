package org.visualheap.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

public class SystemOutThread extends Thread {
	
	private final JTextArea area;
	
	public SystemOutThread(JTextArea area) {
		this.area = area;
	}
	
	public void run() {
		try {
			System.setErr(System.out);
			PipedOutputStream pOut = new PipedOutputStream();
			System.setOut(new PrintStream(pOut));
			PipedInputStream pIn = new PipedInputStream(pOut);
			BufferedReader br = new BufferedReader(new InputStreamReader(pIn));
			while (true) {
				if (br.ready()) area.append(br.readLine() + "\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
