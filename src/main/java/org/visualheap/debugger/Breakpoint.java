package org.visualheap.debugger;

public class Breakpoint {
	private int line;

	private String className;
	
	public Breakpoint(String className, int line) {
		this.line = line;
		this.className = className;
	}
	
	public int getLine() {
		return line;
	}

	public String getClassName() {
		return className;
	}

  @Override
  public boolean equals(Object o) {
    if (o instanceof Breakpoint) {
       Breakpoint other = (Breakpoint) o;
       return ((other.getLine() == this.getLine()) && 
               (other.getClassName().equals(this.getClassName())));
    } else {
      return false;
    }
  }

}
