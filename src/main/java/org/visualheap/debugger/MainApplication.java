import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainApplication {

  private static final JFileChooser fc = new JFileChooser();

  private static void buildGUI() {
  	JFrame frame = new JFrame("Visual Heap Analyser");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
		JPanel contentPane = new JPanel(new BorderLayout());
		frame.setContentPane(contentPane);

		final JLabel yellowLabel = new JLabel();
		yellowLabel.setOpaque(true);
		yellowLabel.setBackground(new Color(248, 213, 131));
		yellowLabel.setPreferredSize(new Dimension(200, 180));
    frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);
    
    JButton button = new JButton("Choose class");
		button.setPreferredSize(new Dimension(150, 40));
		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				//In response to a button click:
				
				int returnVal = fc.showOpenDialog(null);
			  if (returnVal == JFileChooser.APPROVE_OPTION) {
          System.out.println("File found: " +   fc.getSelectedFile().getPath());
				  yellowLabel.setText("File found: " +   fc.getSelectedFile().getPath());
				}
			}
		});
    frame.getContentPane().add(button, BorderLayout.PAGE_START);

    JButton btnDebug = new JButton("Debug");
		btnDebug.setPreferredSize(new Dimension(150, 40));
		btnDebug.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
			}
		});
    frame.getContentPane().add(btnDebug, BorderLayout.PAGE_END);

		frame.pack();
		frame.setVisible(true);
	}


	public static void main (String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		    buildGUI();
		  }
		});
	}

}
