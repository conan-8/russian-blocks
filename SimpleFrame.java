import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimpleFrame {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Tetris Game - Background Changer");

        String imagePath1 = "./res/bg/mainmenu.png";
        String imagePath2 = "./res/bg/game.png"; 

        ImagePanel backgroundPanel = null;
        try {
            backgroundPanel = new ImagePanel(imagePath1, imagePath2);
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
             backgroundPanel = new ImagePanel();
        }

        frame.setContentPane(backgroundPanel);
        frame.setSize(720, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class ImagePanel extends JPanel implements ActionListener {

    private Image image1;
    private Image image2;
    private Image currentImage;
    private JButton changeButton;

    public ImagePanel() {
        super();
         this.image1 = null;
         this.image2 = null;
         this.currentImage = null;
         setLayout(null);
         setupButton();
    }

    public ImagePanel(String imagePath1, String imagePath2) throws IOException {
        File file1 = new File(imagePath1);
        if (file1.exists()) {
            this.image1 = ImageIO.read(file1);
        } else {
            System.err.println("Error: Image file not found at " + file1.getAbsolutePath());
            throw new IOException("File not found: " + imagePath1);
        }
        this.currentImage = this.image1;

        try {
             File file2 = new File(imagePath2);
             if (file2.exists()) {
                this.image2 = ImageIO.read(file2);
             } else {
                 System.err.println("Warning: Second image file not found at " + file2.getAbsolutePath());
                 this.image2 = null;
             }
        } catch (IOException e) {
             System.err.println("Warning: Error loading second image: " + e.getMessage());
             this.image2 = null;
        }

        setLayout(null);
        setupButton();
    }

     private void setupButton() {
        changeButton = new JButton("Change BG");
        // *** ADJUST THESE VALUES to position the button correctly below your logo ***
        // The y-coordinate (second parameter) has been increased by 250 (200 + 250 = 450)
        changeButton.setBounds(310, 450, 100, 30); // Position updated
        changeButton.addActionListener(this);
        this.add(changeButton);
     }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentImage != null) {
            g.drawImage(currentImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g.drawString("Background image failed to load.", 50, 50);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeButton && image2 != null) {
            if (currentImage == image1) {
                currentImage = image2;
            } else {
                currentImage = image1;
            }
            repaint();
        } else if (image2 == null) {
             System.out.println("Button clicked, but second image is not loaded.");
        }
    }
}
