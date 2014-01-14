package debugger.testprogs;
import java.util.LinkedList;
import java.util.List;


public class Array {
	
	private static int SIZE = 10;

	public static void main(String[] args) {
		
		List<Integer> list = new LinkedList<Integer>();
	
		Array outerArray = new Array();
		System.out.println("starting");
  
		outerArray.sum();
		outerArray.obj();
		
		System.out.println("after");
	}
	
	private void obj() {
		Object[] arr = new Object[SIZE];
		for(int i = 0; i < SIZE; i++) {
			arr[i] = new Object();
		}
		System.out.println("break here");
	}

	public int sum() {
		int[] arr = new int[SIZE];
		
		Array innerArray = new Array();
		
		char c = 'c';
		int integer = 0;
		int sum = 0;
		
		arr[0] = 1;
		
		for(int i = 0; i < SIZE; i++) {
			for(int j = 0; j < i; j++) {
				arr[i] += arr[j];
			}
			sum += arr[i];
		}
		return sum;
		
	}

}
