package debugger.testprogs;

/**
 * Created with IntelliJ IDEA.
 * User: Anna
 * Date: 14/01/14
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
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
