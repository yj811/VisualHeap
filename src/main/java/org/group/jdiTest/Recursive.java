package org.group.jdiTest;

public class Recursive {

	private Recursive inner = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Recursive outer = new Recursive();
	}

	public Recursive(int i) {
		if(i < 1000) {
            try {
		        Thread.sleep(15);
		    } catch(InterruptedException ex) {
		        Thread.currentThread().interrupt();
		    }
			inner = new Recursive(i+1);
		}
	}
	
	public Recursive() {
		inner = new Recursive(1);
	}
}
