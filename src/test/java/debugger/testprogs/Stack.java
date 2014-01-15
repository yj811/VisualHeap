package debugger.testprogs;

public class Stack {

    private static final int SIZE = 5;
    private Integer[] arr = new Integer[SIZE];
    private int i = 0;
    private int k = 0;

    public void push(Integer t) {
        arr[i++] = t;
    }

    public Integer pop() {
        return arr[i--];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int j = 0; j < i; j++) {
            sb.append(arr[j]);
            if (j < i - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {

        Stack s = new Stack();

        s.push(5);
        s.push(1);
        s.push(2);
        s.push(10);
        s.pop();
        s.pop();
        s.pop();
        s.pop();
        System.out.println("Stack Contents: " + s);
    }

}
