import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class GetUserName extends JFrame {
    public GetUserName(UI ui){
        this.setTitle("UserName");
        this.setSize(new Dimension(320, 240));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        JTextField getUserName = new JTextField();
        getUserName.setPreferredSize(new Dimension(200, 30));
        JButton btn = new JButton("Submit");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ui.userName = getUserName.getText();

                JComponent comp = (JComponent) e.getSource();
                Window win = SwingUtilities.getWindowAncestor(comp);
                win.dispose();
            }
        });

        panel.add(getUserName);
        panel.add(btn);
        this.getContentPane().add(panel);
        this.setVisible(true);
    }

}
