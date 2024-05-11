import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.util.Scanner;

enum PaintMode {
	Pixel, Area
};

public class UI extends JFrame {
	Socket socket;
	DataInputStream in;
	DataOutputStream out;

	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel pnlDefaultColor;
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglReset;
	private JToggleButton tglBucket;

	private static UI instance;
	private int selectedColor = -50; // golden -543230

	int[][] data = new int[50][50]; // pixel color data array
	int dataWidth;
	int dataHeight;
	int blockSize = 16;

	public String userName;

	PaintMode paintMode = PaintMode.Pixel;

	/**
	 * get the instance of UI. Singleton design pattern.
	 * 
	 * @return
	 */
	public static UI getInstance() {
		if (instance == null)
			try {
				instance = new UI();
			} catch (IOException ex) {
			}

		return instance;
	}

	private void recive(DataInputStream in) {
		// byte[] buffer = new byte[1024];
		try {
			while (true) {
				int type = in.readInt();
				System.out.println("type: " + type);
				switch (type) {
				case -999:
					//receive rest message
					resetFrame();
					break;
				case 0:
					// receive text message
					receiveTextMessage(in);
					break;
				case 1:
					receivePixelData(in);
					// receive pixel message
					break;
				case 2:
					outputFrame(data);
					break;
				case 3:
					getFrame(in);
					break;
				case 5:
					receiveBucketData(in);
					break;
				default:
					// other
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void studioChoosing(DataInputStream in) throws IOException {
		int studios = in.readInt();
		Scanner scan = new Scanner(System.in);
		//Show GUI
		if(studios == 0){
			//can only create
			System.out.println("No studio now, you can only create a new one");
			chooseCreateStudio(scan);
		}else {
			//show studios to the user
			for(int i = 0; i < studios; i++){
				byte[] buffer = new byte[1024];
				int len = in.readInt();
				in.read(buffer, 0, len);

				String studioName = new String(buffer, 0, len);
				System.out.println("Studio " + i + ": " + studioName);
			}
			System.out.println("Do you want to create a new studio(C) or join an existing one(J)?");
			String resp = scan.nextLine();

			if (resp.compareTo("C") == 0){
				chooseCreateStudio(scan);
			}else {
				System.out.println("Please input the number of studio you want to join");
				int studioNum = scan.nextInt();
				out.writeInt(1); //tell the server to join studio
				out.writeInt(studioNum);
				out.flush();
				this.dataWidth = in.readInt();
				this.dataHeight = in.readInt();
				data = new int[dataWidth][dataHeight];
			}
		}
	}

	private void chooseCreateStudio(Scanner scan) throws IOException{
		out.writeInt(0); //tell the server to create new studio
		String name = null;
		int width = -1;
		int height = -1;
		while(name == null){
			System.out.print("Please input the width, height and name of the studio: ");
			width = scan.nextInt();
			height = scan.nextInt();
			name = scan.nextLine();
		}

		out.writeInt(width);
		out.writeInt(height);
		out.writeInt(name.length());
		out.write(name.getBytes());
		out.flush();
		this.dataWidth = width;
		this.dataHeight = height;
		data = new int[dataWidth][dataHeight];
	}

	private void receiveBucketData(DataInputStream in) throws IOException {
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		// Update Screen
		paintArea(x,y,color);
	}

	private void receiveTextMessage(DataInputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.readInt();
		in.read(buffer, 0, len);

		String msg = new String(buffer, 0, len);
		System.out.println(msg);

		SwingUtilities.invokeLater(() -> {
			chatArea.append(msg + "\n");
		});

	}
	
	private void receivePixelData(DataInputStream in) throws IOException{
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();
		//Update Screen
		paintPixel(color,x,y);
	}

	/**
	 * private constructor. To create an instance of UI, call UI.getInstance()
	 * instead.
	 */
	private UI() throws IOException {
		GetUserName getUserName = new GetUserName(this);
		while (userName == null){
			System.out.print("");
		}

		socket = new Socket("158.182.6.165", 12345);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());

		out.writeInt(-3);

		studioChoosing(in);

		// create a new thread for receiving data
		Thread t = new Thread(() -> {
			recive(in);
		});
		t.start();

		setTitle("KidPaint");

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));

		paintPanel = new JPanel() {

			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method

			// enable anti-aliasing
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHints(rh);

			// clear the paint panel using black
			g2.setColor(Color.black);
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());

			// draw and fill circles with the specific colors stored in the data array
			for (int x = 0; x < data.length; x++) {
				for (int y = 0; y < data[0].length; y++) {
					g2.setColor(new Color(data[x][y]));
					g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
					g2.setColor(Color.darkGray);
					g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
				}
			}
			}
		};

		paintPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0){
					paintArea(e.getX() / blockSize, e.getY() / blockSize);
					try {
						out.writeInt(5);
						out.writeInt(selectedColor);
						out.writeInt(e.getX() / blockSize);
						out.writeInt(e.getY() / blockSize);
						out.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace(); // for debugging. remove it in production
					}
				}
			}
		});

		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0)
					 paintPixel(e.getX() / blockSize, e.getY() / blockSize);
					try {
						out.writeInt(1);
						out.writeInt(selectedColor);
						out.writeInt(e.getX() / blockSize);
						out.writeInt(e.getY() / blockSize);
						out.flush();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace(); // for debugging. remove it in production
					}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));

		JScrollPane scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);

		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}

		});

		toolPanel.add(pnlColorPicker);

		final int[] defaultColors = new int[]{-322047, -97776, -135838, -8192977, -15599388, -16485902, -10345552};
		for(int i = 0; i < defaultColors.length; i++){
			pnlDefaultColor = new JPanel();
			int color = defaultColors[i];
			pnlDefaultColor.setPreferredSize(new Dimension(24, 24));
			pnlDefaultColor.setBackground(new Color(color));
			pnlDefaultColor.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					selectedColor = color;
				}
			});

			toolPanel.add(pnlDefaultColor);
		}



		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);

		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);

		tglReset = new JToggleButton("Reset");
		toolPanel.add(tglReset);

		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				paintMode = PaintMode.Pixel;
			}
		});

		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(true);
				paintMode = PaintMode.Area;
			}
		});

		tglReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0){
				resetFrame();
				tglReset.setSelected(false);
			}
		});

		JPanel msgPanel = new JPanel();

		getContentPane().add(msgPanel, BorderLayout.EAST);

		msgPanel.setLayout(new BorderLayout(0, 0));

		msgField = new JTextField(); // text field for inputting message

		msgPanel.add(msgField, BorderLayout.SOUTH);

		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) { // if the user press ENTER
					onTextInputted(msgField.getText());
					msgField.setText("");
				}
			}

		});

		chatArea = new JTextArea(); // the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		JScrollPane scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);

		this.setSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		new KidPaint(dataWidth, dataHeight, this);
	}

	/**
	 * it will be invoked if the user selected the specific color through the color
	 * picker
	 * 
	 * @param colorValue - the selected color
	 */
	public void selectColor(int colorValue) {
		SwingUtilities.invokeLater(() -> {
			selectedColor = colorValue;
			pnlColorPicker.setBackground(new Color(colorValue));
		});
	}

	/**
	 * it will be invoked if the user inputted text in the message field
	 * 
	 * @param text - user inputted text
	 */
	private void onTextInputted(String text) {
		try {
			out.writeInt(0); // 0 means this is a chat message
			System.out.println(0); // To test the message
			String newText = userName + ": " + text;
			out.writeInt(newText.length());
			out.write(newText.getBytes());
			out.flush();
		} catch (IOException ex) {
			// you need handle it here

		}
	}

	public void outputFrame(int[][] data){
		try {
			out.writeInt(-1);
			for(int i = 0; i < data.length; i++){
				for(int j = 0; j < data[i].length; j++){
					out.writeInt(data[i][j]);
				}
			}
			System.out.println("finish output");
		} catch (IOException ex) {
			// you need handle it here
		}
	}

	public void getFrame(DataInputStream in) throws IOException{
		System.out.println(data.length + " " +data[0].length);
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < data[i].length; j++){
				data[i][j] = in.readInt();
			}
		}
		System.out.println("frame updated");
	}

	private void resetFrame(){
		try {
			out.writeInt(-999);
			for(int i = 0; i < data.length; i++){
				for(int j = 0; j < data[i].length; j++){
					data[i][j] = 0;
				}
			}
		} catch (IOException e){

		}

	}


	/**
	 * change the color of a specific pixel
	 * 
	 * @param col, row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = selectedColor;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}
	
	public void paintPixel(int color ,int col, int row) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = color;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}
	

	/**
	 * change the color of a specific area
	 * 
	 * @param col, row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public List paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = selectedColor;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}

	public List paintArea(int col, int row, int color) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != color) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = color;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}

	/**
	 * set pixel data and block size
	 * 
	 * @param data
	 * @param blockSize
	 */
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		paintPanel.repaint();
	}
}
