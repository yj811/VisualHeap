package debugger.testprogs;

public class BinaryTree {

    BinaryTree left;
    BinaryTree right;
    int i;

    BinaryTree(int depth) {
        i = depth;
        if(depth > 0) {
            left = new BinaryTree(depth - 1);
            right = new BinaryTree(depth - 1);
        }
    }

    public static void main(String[] args) {
        BinaryTree first = new BinaryTree(10);
        System.out.println("Tree created");
    }

    public void foo() {};
    public void bar() {};

}
