package debugger.testprogs;
import java.util.LinkedList;
import java.util.List;


public class Array {
	
	private static int SIZE = 1000;

	public static void main(String[] args) {
		
		List<Integer> list = new LinkedList<Integer>();
	
		Array outerArray = new Array();
		System.out.println("starting");
  
		outerArray.sum();
		
		System.out.println("after");
	}
	
	public void sum() {
		SimpleReference[] arr = new SimpleReference[SIZE];
		
		arr[0] = new SimpleReference(3);
        arr[1] = new SimpleReference(3);

	}

}
