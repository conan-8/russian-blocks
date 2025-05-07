import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

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
        frame.setResizable(false);
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

    private final List<Image> bobbingImages = new ArrayList<>();
    private final int[] bobbingOffsets = new int[7];
    private final int[] bobbingSpeeds = new int[7];
    private final int bobbingAmplitude = 20;
    private int timerTick = 0;

    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        super();
        setLayout(null);

        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);
        loadBobbingImages();

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

        startBobbingAnimation();
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

    private void loadBobbingImages() {
        for (int i = 1; i <= 7; i++) {
            try {
                File imageFile = new File("./res/pieces/glow/" + i + ".png");
                if (imageFile.exists()) {
                    bobbingImages.add(ImageIO.read(imageFile));
                    bobbingOffsets[i - 1] = 0;
                    bobbingSpeeds[i - 1] = (int) (Math.random() * 50 + 50); // Random speed for each image
                } else {
                    System.err.println("Error: Bobbing image not found: " + imageFile.getAbsolutePath());
                    bobbingImages.add(null);
                }
            } catch (IOException e) {
                System.err.println("Error loading bobbing image: " + e.getMessage());
                bobbingImages.add(null);
            }
        }
    }

    private void startBobbingAnimation() {
        Timer timer = new Timer(16, new ActionListener() { // ~60 FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                timerTick++;
                for (int i = 0; i < bobbingOffsets.length; i++) {
                    bobbingOffsets[i] = (int) (Math.sin((timerTick + i * 20) / (double) bobbingSpeeds[i]) * bobbingAmplitude);
                }
                repaint();
            }
        });
        timer.start();
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

        // Draw bobbing images
        // int panelWidth = this.getWidth();
        // int spacing = panelWidth / (bobbingImages.size() + 1);
        int[] bobx = {-100, -60,430,-250,300, 550,-140};
        int[] boby = {225, -160,0,0,500, 300,460};

        for (int i = 0; i < bobbingImages.size(); i++) {
            Image img = bobbingImages.get(i);
            if (img != null) {
                // int imgWidth = img.getWidth(this);
                // int imgHeight = img.getHeight(this);
                // int x = spacing * (i + 1) - imgWidth / 2;
                // int y = 500 + bobbingOffsets[i]; // Base Y position + bobbing offset
                int x = bobx[i];
                int y = boby[i] + bobbingOffsets[i]; // Base Y position + bobbing offset
                g.drawImage(img, x, y, this);
            }
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
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            handleSelection();
        }
    }

    private void handleSelection() {
        switch (currentOverlayIndex) {
            case 0: // Easy
            case 1: // Medium
            case 2: // Hard
                openGameScreen();
                break;
            case 3: // Credits
                showCreditsOverlay();
                break;
            case 4: // Quit
                System.exit(0);
                break;
            default:
                System.out.println("Invalid selection.");
        }
    }

    private void openGameScreen() {
        JFrame gameFrame = new JFrame("Game Screen");
        ImagePanel gamePanel = new ImagePanel("./res/bg/game.png", null);

        gameFrame.setContentPane(gamePanel);
        gameFrame.setSize(720, 720);
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

        gamePanel.requestFocusInWindow();
    }

    private void showCreditsOverlay() {
        JFrame creditsFrame = new JFrame("Credits");
        ImagePanel creditsPanel = new ImagePanel("./res/credits.png", null);

        creditsFrame.setContentPane(creditsPanel);
        creditsFrame.setSize(720, 720);
        creditsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        creditsFrame.setLocationRelativeTo(null);
        creditsFrame.setVisible(true);

        creditsPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                creditsFrame.dispose();
            }
        });

        creditsPanel.requestFocusInWindow();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
