import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;

public class Studio {
     public ArrayList<Socket> studioSocketList = new ArrayList<Socket>();
    int[][] currentData;
    int hight;
    int width;
    String studioName;

    public Studio(int width, int hight, String studioName){
        this.width = width;
        this.hight = hight;
        this.studioName = studioName;
        currentData = new int[width][hight];
    }

    public void addSocket(Socket socket) throws IOException{
        int num = studioSocketList.size();
        studioSocketList.add(num, socket);
    }

    public void serve(Socket cSocket) throws IOException {
        DataInputStream in = new DataInputStream(cSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(studioSocketList.get(0).getOutputStream());
        out.writeInt(2);
        while (true) {
            int type = in.readInt(); // type represents the message type
            System.out.print(" srvType: " + type);
            switch (type) {
                case -999:
                    forwardResetFrame();
                    break;
                case -1:
                    updateFrame(studioSocketList.get(0));
                    for(int i = 0; i < currentData.length; i++){
                        for(int j = 0; j < currentData[i].length; j++){
                            System.out.print(currentData[i][j]);
                        }
                        System.out.println();
                    }
                    break;
                case 0:
                    // test message
                    forwardTestMessage(in);
                    break;
                case 1:
                    // drawing message
                    forwardDrawingMessage(in);
                    break;
                case 5:
                    forwardBucketgMessage(in);
                    break;
                default:
                    // other
            }
        }
    }

    private void updateFrame(Socket socket) throws IOException{
        DataInputStream in = new DataInputStream(socket.getInputStream());
        for(int i = 0; i < currentData.length; i++){
            for(int j = 0; j < currentData[i].length; j++){
                currentData[i][j] = in.readInt();
            }
        }
        System.out.println("updated");
        forwardFrame();
    }

    private void forwardBucketgMessage(DataInputStream in) throws IOException {
        int color = in.readInt();
        int x = in.readInt();
        int y= in.readInt();
//        System.out.printf("%d @(%d, %d)\n", color,x,y);

        synchronized(studioSocketList) {
            for (int i = 0; i < studioSocketList.size(); i++) {
                Socket s = studioSocketList.get(i);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeInt(5);
                out.writeInt(color);
                out.writeInt(x);
                out.writeInt(y);
                out.flush();
            }
        }
    }

    private void forwardTestMessage(DataInputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.readInt();
        in.read(buffer, 0, len);
        System.out.println(new String(buffer, 0, len));

        synchronized (studioSocketList) {
            for (int i = 0; i < studioSocketList.size(); i++) {
                Socket socket = studioSocketList.get(i);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(0);
                out.writeInt(len);
                out.write(buffer, 0, len);
                out.flush();
            }
        }
    }

    private void forwardDrawingMessage(DataInputStream in) throws IOException {
        int color = in.readInt();
        int x = in.readInt();
        int y= in.readInt();
//		System.out.printf("%d @(%d, %d)\n", color,x,y);

        synchronized(studioSocketList) {
            for (int i = 0; i < studioSocketList.size(); i++) {
                Socket s = studioSocketList.get(i);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeInt(1);
                out.writeInt(color);
                out.writeInt(x);
                out.writeInt(y);
                out.flush();
            }
        }
    }

    private void forwardFrame() throws IOException {
        synchronized(studioSocketList) {
            for (int i = 0; i < studioSocketList.size(); i++) {
                Socket s = studioSocketList.get(i);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeInt(3);
                for(int x = 0; x < currentData.length; x++){
                    for(int y = 0; y < currentData[x].length; y++){
                        out.writeInt(currentData[x][y]);
                    }
                }
            }

        }
    }

    private void forwardResetFrame() throws IOException {
        synchronized(studioSocketList) {
            for (int i = 0; i < studioSocketList.size(); i++) {
                Socket s = studioSocketList.get(i);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeInt(-999);
            }
        }
    }

    class StudioUI extends JFrame {
        public StudioUI() {
            this.setTitle("NewStudio");
            this.setSize(new Dimension(400, 240));
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            String username;

            Container container = this.getContentPane();

            JPanel panel = new JPanel();
            JLabel UserName = new JLabel("User name: ");
            JLabel Height = new JLabel("Height: ");
            JLabel Width = new JLabel("Width: ");
            JTextField getUserName = new JTextField();
            JTextField getheight = new JTextField();
            JTextField getwidth = new JTextField();
            getUserName.setPreferredSize(new Dimension(60, 30));
            getheight.setPreferredSize(new Dimension(60, 30));
            getwidth.setPreferredSize(new Dimension(60, 30));
            JButton btn = new JButton("Submit");
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)  {
//            ui.userName = getUserName.getText();

                    JComponent comp = (JComponent) e.getSource();
                    Window win = SwingUtilities.getWindowAncestor(comp);
                    String name = getUserName.getText();
                    int height = Integer.parseInt(getheight.getText());
                    int width = Integer.parseInt(getwidth.getText());

                    System.out.print(name + " " + height + " " + width);
                    win.dispose();
                }
            });
            panel.add(UserName);
            panel.add(getUserName);
            panel.add(Height);
            panel.add(getheight);
            panel.add(Width);
            panel.add(getwidth);
            panel.add(btn);
            this.getContentPane().add(panel);
            this.setVisible(true);

            this.setVisible(true);
        }
    }

}