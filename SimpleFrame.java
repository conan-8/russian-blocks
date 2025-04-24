import java.awt.Graphics; // Import the JFrame class
import java.awt.Image;  // Import the JPanel class
import java.io.File; // Import ImageIcon for handling images
import java.io.IOException; // Import Graphics for drawing
import javax.imageio.ImageIO;    // Import Image class
import javax.swing.JFrame;      // Import File class for file handling
import javax.swing.JPanel; // Import IOException for error handling

public class SimpleFrame {

    public static void main(String[] args) {
        // Create a new JFrame instance
        JFrame frame = new JFrame("Frame with Image Background");

        // --- Image Loading ---
        Image backgroundImage = null;
        try {
            // IMPORTANT: Replace "path/to/your/image.jpg" with the actual path to your image file
            // Example paths:
            // - Absolute: "C:/Users/YourUser/Pictures/background.jpg" (Windows)
            // - Absolute: "/home/youruser/images/background.png" (Linux/macOS)
            // - Relative (if image is in the same folder as the compiled .class file): "background.gif"
            // - Relative (if image is in an 'images' subfolder): "images/background.jpg"
            File imageFile = new File("./res/bg/mainmenu.png");
            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
            } else {
                System.err.println("Error: Image file not found at " + imageFile.getAbsolutePath());
                // Optionally, set a default background color or exit
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
            // Handle the error appropriately, e.g., show an error message, use a default color
        }

        // --- Create Custom Panel with Background Image ---
        // We pass the loaded image to the panel's constructor
        ImagePanel backgroundPanel = new ImagePanel(backgroundImage);

        // Set the custom panel as the content pane of the frame
        frame.setContentPane(backgroundPanel);

        // Set the size of the frame
        frame.setSize(1000, 1000); // Width = 1000 pixels, Height = 1000 pixels

        // Set the default close operation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the frame visible
        frame.setVisible(true);
    }
}

/**
 * A custom JPanel that draws a background image.
 */
class ImagePanel extends JPanel {

    private Image backgroundImage;

    // Constructor to receive the image
    public ImagePanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paint the default background (important!)

        // Draw the background image if it's loaded successfully
        if (backgroundImage != null) {
            // Draw the image, scaling it to cover the entire panel area
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            // Optional: Draw something else if the image failed to load,
            // like a solid color or a message.
            // The default background painted by super.paintComponent(g) might be sufficient.
        }
    }
}
