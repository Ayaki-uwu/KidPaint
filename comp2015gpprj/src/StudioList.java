import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

public class StudioList extends JFrame {
	Set<Socket> studio = new HashSet<Socket>();
	Set<Socket> studioName = new HashSet<Socket>();
	int width, height;
	String userName;

	public StudioList(String userName) {
		this.setTitle("Studiolist");
		this.setSize(new Dimension(400, 240));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.userName = userName;

		Container container = this.getContentPane();

		JPanel panel = new JPanel();
		JFrame listFrame= new JFrame();
		// ask for opening a new Studio
		JLabel AskForNewStudio = new JLabel("Click to open a new studio: ");
		JButton NewStudio = new JButton("New studio");
		NewStudio.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent comp = (JComponent) e.getSource();
				Window win = SwingUtilities.getWindowAncestor(comp);
				NewStudio studio = new NewStudio();
				win.dispose();
			}
		});

		JLabel ExistStudio = new JLabel("Here is the Studio you can choose");
		AskForNewStudio.setPreferredSize((new Dimension(60,30)));
		NewStudio.setPreferredSize((new Dimension(60,30)));
		ExistStudio.setPreferredSize((new Dimension(60,30)));
		
		panel.add(AskForNewStudio);
		panel.add(NewStudio);
		panel.add(ExistStudio);
		this.getContentPane().add(panel);
		this.setVisible(true);


	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
