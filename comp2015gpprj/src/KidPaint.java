
public class KidPaint {
	public KidPaint(int width, int hight, UI ui){
		ui.setData(new int[width][hight], 20);	// set the data array and block size. comment this statement to use the default data array and block size.
		ui.setVisible(true);				// set the ui
	}

	public static void main(String[] args) {
		UI ui = UI.getInstance();		// get the instance of UI
	}
}
