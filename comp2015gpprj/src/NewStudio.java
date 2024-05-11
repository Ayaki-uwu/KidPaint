import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

public class NewStudio extends JFrame {
	public NewStudio() {
		this.setTitle("NewStudio");
		this.setSize(new Dimension(400, 240));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container container = this.getContentPane();

		JPanel panel = new JPanel();
		JLabel StudioName = new JLabel("Studio name: ");
		
		JLabel Height = new JLabel("Height: ");
		JLabel Width = new JLabel("Width: ");
		JTextField getStudioName = new JTextField();
		JTextField getheight = new JTextField();
		JTextField getwidth = new JTextField();
		getStudioName.setPreferredSize(new Dimension(60, 30));
		getheight.setPreferredSize(new Dimension(60, 30));
		getwidth.setPreferredSize(new Dimension(60, 30));
		JButton btn = new JButton("Submit");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)  {
//            ui.userName = getUserName.getText();
				
				JComponent comp = (JComponent) e.getSource();
				Window win = SwingUtilities.getWindowAncestor(comp);
				String name = getStudioName.getText();
				int height = Integer.parseInt(getheight.getText());
				int width = Integer.parseInt(getwidth.getText());

				System.out.print(name + " " + height + " " + width);
				win.dispose();
			}
		});
		panel.add(StudioName);
		panel.add(getStudioName);
		panel.add(Height);
		panel.add(getheight);
		panel.add(Width);
		panel.add(getwidth);
		panel.add(btn);
		this.getContentPane().add(panel);
		this.setVisible(true);

	}

	public static void main(String[] args) {
		new NewStudio();
	}

}
