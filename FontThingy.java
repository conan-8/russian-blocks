import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class FontThingy {
    public static void main(String[] args) {
        try {
           
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P-Regular.ttf")).deriveFont(18f); // Set font size

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

            JFrame frame = new JFrame("Custom Font Demo");
            JLabel label = new JLabel("Hello, Custom Font!");
            label.setFont(customFont); 

            // Frame setup
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLayout(new FlowLayout());
            frame.add(label);
            frame.setVisible(true);

        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
