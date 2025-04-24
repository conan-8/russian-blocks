import java.awt.Graphics; // Import the JFrame class
import java.awt.Image;  // Import the JPanel class
import java.io.File; // Import ImageIcon for handling images
import java.io.IOException; // Import Graphics for drawing
import javax.imageio.ImageIO;    // Import Image class
import javax.swing.JFrame;      // Import File class for file handling
import javax.swing.JPanel; // Import IOException for error handling

public class SimpleFrame {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Tetris Game");
         Image backgroundImage = null;
        try {
 
            File imageFile = new File("./res/bg/mainmenu.png");
            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
            } else {
                System.err.println("Error: Image file not found at " + imageFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }

        ImagePanel backgroundPanel = new ImagePanel(backgroundImage);
        frame.setContentPane(backgroundPanel);
        frame.setSize(720, 720); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class ImagePanel extends JPanel {

    private Image backgroundImage;

    public ImagePanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
           ;
        }
    }
}
