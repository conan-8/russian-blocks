import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StaticBackgroundFrame {

    public static void main(String[] args) {
        String backgroundPath = "./res/bg/mainmenu.png";
        String[] overlayPaths = {
            "./res/options/easy.png",
            "./res/options/medium.png",
            "./res/options/hard.png",
            "./res/options/credits.png",
            "./res/options/quit.png"
        };

        JFrame frame = new JFrame("Tetris Game");
        ImagePanel backgroundPanel = new ImagePanel(backgroundPath, overlayPaths);

        frame.setContentPane(backgroundPanel);
        frame.setSize(720, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                 backgroundPanel.requestFocusInWindow();
            }
        });
         backgroundPanel.requestFocusInWindow();
    }
}

class ImagePanel extends JPanel implements KeyListener {

    private Image backgroundImage;
    private Image[] overlayImages;
    private int currentOverlayIndex = 0;
    private final int overlayYPosition = 275;

    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        super();
        setLayout(null);

        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);

        this.addKeyListener(this);
        this.setFocusable(true);

        if (overlayImages == null || overlayImages.length == 0) {
            currentOverlayIndex = -1;
        } else {
             while(currentOverlayIndex < overlayImages.length && overlayImages[currentOverlayIndex] == null) {
                 currentOverlayIndex++;
             }
             if (currentOverlayIndex >= overlayImages.length) {
                 currentOverlayIndex = -1;
             }
        }
    }

    private void loadBackgroundImage(String path) {
        try {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                this.backgroundImage = ImageIO.read(imageFile);
                System.out.println("Background image loaded: " + imageFile.getAbsolutePath());
            } else {
                System.err.println("Error: Background image not found: " + imageFile.getAbsolutePath());
                this.backgroundImage = null;
            }
        } catch (IOException e) {
            System.err.println("Error loading background image '" + path + "': " + e.getMessage());
            e.printStackTrace();
            this.backgroundImage = null;
        }
    }

    private void loadOverlayImages(String[] paths) {
        if (paths == null || paths.length == 0) {
            System.err.println("Warning: No overlay image paths provided.");
            this.overlayImages = new Image[0];
            return;
        }

        this.overlayImages = new Image[paths.length];
        for (int i = 0; i < paths.length; i++) {
            try {
                File imageFile = new File(paths[i]);
                if (imageFile.exists()) {
                    this.overlayImages[i] = ImageIO.read(imageFile);
                    System.out.println("Overlay image loaded: " + imageFile.getAbsolutePath());
                } else {
                    System.err.println("Error: Overlay image not found: " + imageFile.getAbsolutePath());
                    this.overlayImages[i] = null;
                }
            } catch (IOException e) {
                System.err.println("Error loading overlay image '" + paths[i] + "': " + e.getMessage());
                e.printStackTrace();
                this.overlayImages[i] = null;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        } else {
            g.setColor(java.awt.Color.GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(java.awt.Color.RED);
            g.drawString("Background image failed to load.", 20, 30);
        }

        if (currentOverlayIndex >= 0 && currentOverlayIndex < overlayImages.length
            && overlayImages[currentOverlayIndex] != null)
        {
            Image currentOverlay = overlayImages[currentOverlayIndex];
            int overlayWidth = currentOverlay.getWidth(this);
            int panelWidth = this.getWidth();
            int x = (panelWidth - overlayWidth) / 2;

            g.drawImage(currentOverlay, x, overlayYPosition, this);
        } else if (currentOverlayIndex != -1) {
             g.setColor(java.awt.Color.YELLOW);
             g.drawString("Selected overlay image failed to load.", 20, 50);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int numOverlays = overlayImages.length;

        if (numOverlays <= 0) return;

        int initialIndex = currentOverlayIndex;
        int nextIndex = currentOverlayIndex;

        if (keyCode == KeyEvent.VK_DOWN) {
            do {
                nextIndex = (nextIndex + 1) % numOverlays;
            } while (overlayImages[nextIndex] == null && nextIndex != initialIndex);

        } else if (keyCode == KeyEvent.VK_UP) {
             do {
                nextIndex = (nextIndex - 1 + numOverlays) % numOverlays;
             } while (overlayImages[nextIndex] == null && nextIndex != initialIndex);
        }

        if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) && overlayImages[nextIndex] != null) {
             currentOverlayIndex = nextIndex;
             System.out.println("Overlay changed to index: " + currentOverlayIndex);
             repaint();
        } else if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN)) {
             System.out.println("Could not find a valid overlay to switch to.");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
