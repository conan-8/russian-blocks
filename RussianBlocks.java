import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

class GameRendererPanel extends JPanel {
    private ImagePanel imagePanel;

    public GameRendererPanel(ImagePanel imagePanel) {
        super();
        this.imagePanel = imagePanel;
        this.setLayout(null);
        this.setFocusable(true);
        this.setBackground(java.awt.Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        if (this.imagePanel.isInGameMode()) {
            if (this.imagePanel.getBackgroundImage() != null) {
                g2d.drawImage(this.imagePanel.getBackgroundImage(), 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g2d.setColor(java.awt.Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        } else {
            if (this.imagePanel.getBackgroundImage() != null) {
                g2d.drawImage(this.imagePanel.getBackgroundImage(), 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g2d.setColor(java.awt.Color.GRAY);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            Image[] overlayImages = this.imagePanel.getOverlayImages();
            int currentOverlayIndex = this.imagePanel.getCurrentOverlayIndex();
            if (overlayImages != null &&
                currentOverlayIndex >= 0 &&
                currentOverlayIndex < overlayImages.length &&
                overlayImages[currentOverlayIndex] != null) {
                Image currentOverlay = overlayImages[currentOverlayIndex];
                int overlayWidth = currentOverlay.getWidth(this);
                int panelWidth = this.getWidth();
                int x = (panelWidth - overlayWidth) / 2;
                g2d.drawImage(currentOverlay, x, this.imagePanel.getOverlayYPosition(), this);
            }

            int[] bobx = {-100, -60, 430, -250, 300, 550, -140};
            int[] boby = {225, -160, 0, 0, 500, 300, 460};
            List<Image> bobbingImages = this.imagePanel.getBobbingImages();
            int[] bobbingOffsets = this.imagePanel.getBobbingOffsets();

            for (int i = 0; i < bobbingImages.size(); i++) {
                Image img = bobbingImages.get(i);
                if (img != null) {
                    if (i < bobx.length && i < boby.length && i < bobbingOffsets.length) {
                        int x_pos = bobx[i];
                        int y_pos = boby[i] + bobbingOffsets[i];
                        g2d.drawImage(img, x_pos, y_pos, this);
                    }
                }
            }
        }
        g2d.dispose();
    }
}


public class RussianBlocks {
    public static void main(String[] args) {
        String backgroundPath = "./res/bg/mainmenu.png";
        String[] overlayPaths = {
            "./res/options/easy.png",
            "./res/options/medium.png",
            "./res/options/hard.png",
            "./res/options/credits.png",
            "./res/options/quit.png"
        };

        JFrame frame = new JFrame("Main Menu Example");
        ImagePanel logicController = new ImagePanel(backgroundPath, overlayPaths);

        frame.setContentPane(logicController.getDrawingPanel());
        frame.setSize(720, 720);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                logicController.getDrawingPanel().requestFocusInWindow();
            }
        });
        logicController.getDrawingPanel().requestFocusInWindow();
    }
}

class ImagePanel implements KeyListener {

    boolean inGameMode = false;
    Image backgroundImage;
    Image[] overlayImages;
    int currentOverlayIndex = 0;
    final int overlayYPosition = 275;
    List<Image> bobbingImages = new ArrayList<>();
    int[] bobbingOffsets = new int[7];
    int[] bobbingSpeeds = new int[7];
    final int bobbingAmplitude = 20;
    int timerTick = 0;

    private GameRendererPanel drawingPanel;

    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        this.drawingPanel = new GameRendererPanel(this);
        this.drawingPanel.addKeyListener(this);

        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);
        loadBobbingImages();

        if (this.overlayImages == null || this.overlayImages.length == 0) {
            this.currentOverlayIndex = -1;
        } else {
            this.currentOverlayIndex = 0;
            while (this.currentOverlayIndex < this.overlayImages.length && this.overlayImages[this.currentOverlayIndex] == null) {
                this.currentOverlayIndex++;
            }
            if (this.currentOverlayIndex >= this.overlayImages.length) {
                this.currentOverlayIndex = -1;
            }
        }
        startBobbingAnimation();
    }

    public boolean isInGameMode() { return inGameMode; }
    public Image getBackgroundImage() { return backgroundImage; }
    public Image[] getOverlayImages() { return overlayImages; }
    public int getCurrentOverlayIndex() { return currentOverlayIndex; }
    public int getOverlayYPosition() { return overlayYPosition; }
    public List<Image> getBobbingImages() { return bobbingImages; }
    public int[] getBobbingOffsets() { return bobbingOffsets; }

    public GameRendererPanel getDrawingPanel() { return this.drawingPanel; }

    private void startBobbingAnimation() {
        Timer animationTimer = new Timer(16, e -> {
            timerTick++;
            for (int i = 0; i < bobbingOffsets.length; i++) {
                if (i < bobbingSpeeds.length && bobbingSpeeds[i] > 0) {
                    bobbingOffsets[i] = (int) (Math.sin((timerTick + i * 20) / (double) bobbingSpeeds[i]) * bobbingAmplitude);
                }
            }
            if (!inGameMode) {
                drawingPanel.repaint();
            }
        });
        animationTimer.start();
    }

    private void loadBackgroundImage(String path) {
        File bgFile = new File(path);
        if (!bgFile.exists()) {
            System.err.println("Warning: Background image not found: " + path);
            this.backgroundImage = null;
            return;
        }
        try {
            this.backgroundImage = ImageIO.read(bgFile);
        } catch (IOException e) {
            System.err.println("Error loading background image: " + path + " - " + e.getMessage());
            this.backgroundImage = null;
        }
    }

    private void loadOverlayImages(String[] paths) {
        if (paths == null || paths.length == 0) {
            this.overlayImages = new Image[0];
            return;
        }
        this.overlayImages = new Image[paths.length];
        for (int i = 0; i < paths.length; i++) {
            File imageFile = new File(paths[i]);
            if (!imageFile.exists()) {
                System.err.println("Warning: Overlay image not found: " + paths[i]);
                this.overlayImages[i] = null;
                continue;
            }
            try {
                this.overlayImages[i] = ImageIO.read(imageFile);
            } catch (IOException e) {
                System.err.println("Error loading overlay image: " + paths[i] + " - " + e.getMessage());
                this.overlayImages[i] = null;
            }
        }
    }

    private void loadBobbingImages() {
        bobbingImages.clear();
        String bobbingBasePath = "./res/pieces/glow/";
        for (int i = 1; i <= 7; i++) {
            String imagePath = bobbingBasePath + i + ".png";
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Warning: Bobbing image not found: " + imagePath);
                bobbingImages.add(null);
                continue;
            }
            try {
                Image loadedImage = ImageIO.read(imageFile);
                if (loadedImage != null) {
                    bobbingImages.add(loadedImage);
                    if (bobbingImages.size() - 1 < bobbingOffsets.length && bobbingImages.size() - 1 < bobbingSpeeds.length) {
                        bobbingOffsets[bobbingImages.size() - 1] = 0;
                        bobbingSpeeds[bobbingImages.size() - 1] = (int) (Math.random() * 50 + 50);
                    }
                } else {
                    bobbingImages.add(null);
                }
            } catch (IOException e) {
                System.err.println("Error loading bobbing image: " + imagePath + " - " + e.getMessage());
                bobbingImages.add(null);
            }
        }
    }

    private void handleSelection() {
        if (overlayImages == null || currentOverlayIndex < 0 || currentOverlayIndex >= overlayImages.length || overlayImages[currentOverlayIndex] == null) {
            System.out.println("No valid menu item selected.");
            return;
        }
        switch (currentOverlayIndex) {
            case 0:
            case 1:
            case 2:
                openGameScreen(); break;
            case 3:
                showCreditsOverlay(); break;
            case 4:
                System.out.println("Quitting game via menu.");
                System.exit(0); break;
            default:
                System.out.println("Unknown menu selection.");
                break;
        }
    }

    public void openGameScreen() {
        inGameMode = true;
        loadBackgroundImage("./res/bg/game.png");
        System.out.println("Switched to game mode (empty screen).");
        drawingPanel.repaint();
        drawingPanel.requestFocusInWindow();
    }

    private void showCreditsOverlay() {
        System.out.println("Showing credits...");
        JFrame creditsFrame = new JFrame("Credits");
        ImagePanel creditsLogic = new ImagePanel("./res/credits.png", null);

        creditsFrame.setContentPane(creditsLogic.getDrawingPanel());
        creditsFrame.setSize(720, 720);
        creditsFrame.setResizable(false);
        creditsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        creditsFrame.setLocationRelativeTo(this.drawingPanel);
        creditsFrame.setVisible(true);

        creditsLogic.getDrawingPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                creditsFrame.dispose();
            }
        });
        creditsLogic.getDrawingPanel().requestFocusInWindow();
    }

    @Override public void keyTyped(KeyEvent e) { }

    @Override public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (inGameMode) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                inGameMode = false;
                loadBackgroundImage("./res/bg/mainmenu.png");
                if (overlayImages != null && overlayImages.length > 0) {
                     currentOverlayIndex = 0;
                     while(currentOverlayIndex < overlayImages.length && overlayImages[currentOverlayIndex] == null) {
                         currentOverlayIndex++;
                     }
                     if (currentOverlayIndex >= overlayImages.length) {
                         currentOverlayIndex = -1;
                     }
                } else {
                    currentOverlayIndex = -1;
                }
                System.out.println("Returning to main menu.");
                drawingPanel.repaint();
                drawingPanel.requestFocusInWindow();
            }

        } else {
            if (overlayImages == null || overlayImages.length == 0) return;

            int numOverlays = overlayImages.length;
            int initialIndex = currentOverlayIndex;
            int nextIndex = currentOverlayIndex;

            if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                if (initialIndex == -1 && numOverlays > 0) {
                    nextIndex = 0;
                    while(nextIndex < numOverlays && overlayImages[nextIndex] == null) nextIndex++;
                    if (nextIndex >= numOverlays) nextIndex = -1;
                } else if (initialIndex != -1) {
                    int searchStartIndex = (initialIndex + 1) % numOverlays;
                    nextIndex = searchStartIndex;
                    do {
                        if (overlayImages[nextIndex] != null) break;
                        nextIndex = (nextIndex + 1) % numOverlays;
                    } while (nextIndex != searchStartIndex);
                    if (overlayImages[nextIndex] == null) nextIndex = initialIndex;
                }
            } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                 if (initialIndex == -1 && numOverlays > 0) {
                    nextIndex = numOverlays - 1;
                    while(nextIndex >= 0 && overlayImages[nextIndex] == null) nextIndex--;
                    if (nextIndex < 0) nextIndex = -1;
                } else if (initialIndex != -1) {
                    int searchStartIndex = (initialIndex - 1 + numOverlays) % numOverlays;
                    nextIndex = searchStartIndex;
                    do {
                        if (overlayImages[nextIndex] != null) break;
                        nextIndex = (nextIndex - 1 + numOverlays) % numOverlays;
                    } while (nextIndex != searchStartIndex);
                    if (overlayImages[nextIndex] == null) nextIndex = initialIndex;
                }
            }

            if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)) {
                if (nextIndex != -1 && nextIndex < numOverlays && overlayImages[nextIndex] != null) {
                    currentOverlayIndex = nextIndex;
                    drawingPanel.repaint();
                } else if (initialIndex != -1 && initialIndex < numOverlays && overlayImages[initialIndex] != null) {
                    currentOverlayIndex = initialIndex;
                } else {
                    boolean foundValid = false;
                    for(int i=0; i<numOverlays; i++) {
                        if(overlayImages[i] != null) {
                            currentOverlayIndex = i;
                            foundValid = true;
                            break;
                        }
                    }
                    if(!foundValid) currentOverlayIndex = -1;
                    drawingPanel.repaint();
                }
            } else if (keyCode == KeyEvent.VK_ENTER) {
                if (currentOverlayIndex != -1 && currentOverlayIndex < numOverlays && overlayImages[currentOverlayIndex] != null) {
                    handleSelection();
                } else {
                    System.out.println("Enter pressed but no valid menu item is currently selected.");
                }
            }
        }
    }
    @Override public void keyReleased(KeyEvent e) { }
}
