import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	ServerSocket srvSocket;
	ArrayList<Socket> list = new ArrayList<Socket>();
	ArrayList<Studio> studioList = new ArrayList<Studio>();

	public Server() throws IOException {
		int port = 12345;
		System.out.println("Listening at port " + port);
		srvSocket = new ServerSocket(port);
		// handle diff client connection
		while (true) {
			Socket cSocket = srvSocket.accept();

			synchronized (list) {
				list.add(cSocket);

				System.out.printf("Total %d clients are connected.\n", list.size());

				DataInputStream in = new DataInputStream(cSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());

				in.readInt();
				sendStudioList(out);

				int type = in.readInt();

				switch (type) {
					case 0:
						// new studio
						addStudio(cSocket, in);
						break;
					case 1:
						// join studio
						addSocketToStudio(cSocket, in);
						break;
				}
			}
		}
	}

	private void sendStudioList(DataOutputStream out) throws IOException{
		out.writeInt(studioList.size());

		for(int i = 0; i < studioList.size(); i++){
			int len = studioList.get(i).studioName.length();
			out.writeInt(len);
			out.write(studioList.get(i).studioName.getBytes());
		}

	}

	private boolean addSocketToStudio(Socket cSocket, DataInputStream in) throws IOException{
		int studioNum = in.readInt();
		DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());
		out.writeInt(studioList.get(studioNum).width);
		out.writeInt(studioList.get(studioNum).hight);

		studioList.get(studioNum).addSocket(cSocket);

		Thread t = new Thread(() -> {
			try {
				studioList.get(studioNum).serve(cSocket);
			} catch (IOException e) {
				System.err.println("connection dropped.");
			} finally {
				synchronized (list) {
					list.remove(cSocket);
					studioList.get(studioNum).studioSocketList.remove(cSocket);
				}
			}
		});
		t.start();
		return true;
	}

	private boolean addStudio(Socket cSocket, DataInputStream in) throws IOException{
		int width = in.readInt();
		int height = in.readInt();

		byte[] buffer = new byte[1024];
		int nameLen = in.readInt();
		in.read(buffer, 0, nameLen);
		String name = new String(buffer, 0, nameLen);
		System.out.println("studio setting: " + width + " " + height + " " + name);
		Studio studio = new Studio(width, height, name);
		studioList.add(studio);
		studio.addSocket(cSocket);
		Thread t = new Thread(() -> {
			try {
				studio.serve(cSocket);
			} catch (IOException e) {
				System.err.println("connection dropped.");
			} finally {
				synchronized (list) {
					list.remove(cSocket);
					studioList.remove(cSocket);
				}
			}
		});
		t.start();
		return true;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new Server();
	}

}
