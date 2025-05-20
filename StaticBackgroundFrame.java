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
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

class TetrisBlock {
    public double x;
    public double y;
    public Image blockImage;
    public int rotationAngle = 0;
    public double sourceImagePivotX;
    public double sourceImagePivotY;
    public double visualWidthInGridUnits;
    public int pieceType; // Added to identify the type of Tetris block

    public TetrisBlock() {
        this.x = 1.0;
        this.y = 1.0;
        this.blockImage = null;
        this.sourceImagePivotX = 0;
        this.sourceImagePivotY = 0;
        this.visualWidthInGridUnits = 1.0;
        this.pieceType = -1; // Default unset pieceType
    }

    public void moveDown() {
        y += 0.96;
    }

    public void moveLeft() {
        x -= 1.0;
    }

    public void moveRight() {
        x += 1.0;
    }

    public void rotate() {
        rotationAngle = (rotationAngle + 90) % 360;
    }
}


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

            final double blockSize = 33.6;

            for (TetrisBlock landedBlock : this.imagePanel.getLandedBlocks()) {
                if (landedBlock != null && landedBlock.blockImage != null) {
                    Graphics2D g2d_landed = (Graphics2D) g.create();
                    int drawX = (int)(landedBlock.x * blockSize);
                    int drawY = (int)(landedBlock.y * blockSize);
                    int naturalWidth = landedBlock.blockImage.getWidth(this);
                    int naturalHeight = landedBlock.blockImage.getHeight(this);

                    if (naturalWidth > 0 && naturalHeight > 0) {
                        int scaledWidth = (int)(naturalWidth * 0.71);
                        int scaledHeight = (int)(naturalHeight * 0.71);
                        double absolutePivotX = drawX + (landedBlock.sourceImagePivotX * 0.71);
                        double absolutePivotY = drawY + (landedBlock.sourceImagePivotY * 0.71);
                        g2d_landed.rotate(Math.toRadians(landedBlock.rotationAngle), absolutePivotX, absolutePivotY);
                        g2d_landed.drawImage(landedBlock.blockImage, drawX, drawY, scaledWidth, scaledHeight, this);
                    }
                    g2d_landed.dispose();
                }
            }

            TetrisBlock currentBlock = this.imagePanel.getCurrentBlock();
            if (currentBlock != null && currentBlock.blockImage != null) {
                Graphics2D g2d_current = (Graphics2D) g.create();
                int drawX = (int)(currentBlock.x * blockSize);
                int drawY = (int)(currentBlock.y * blockSize);
                int naturalWidth = currentBlock.blockImage.getWidth(this);
                int naturalHeight = currentBlock.blockImage.getHeight(this);

                if (naturalWidth > 0 && naturalHeight > 0) {
                    int scaledWidth = (int)(naturalWidth * 0.71);
                    int scaledHeight = (int)(naturalHeight * 0.71);
                    double absolutePivotX = drawX + (currentBlock.sourceImagePivotX * 0.71);
                    double absolutePivotY = drawY + (currentBlock.sourceImagePivotY * 0.71);
                    g2d_current.rotate(Math.toRadians(currentBlock.rotationAngle), absolutePivotX, absolutePivotY);
                    g2d_current.drawImage(currentBlock.blockImage, drawX, drawY, scaledWidth, scaledHeight, this);
                }
                g2d_current.dispose();
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

    private static final String TETRIS_BLOCK_IMAGE_DIRECTORY = "./res/pieces/";

    boolean inGameMode = false;
    TetrisBlock currentBlock = null;
    List<TetrisBlock> landedBlocks = new ArrayList<>();
    Image backgroundImage;
    Image[] overlayImages;
    int currentOverlayIndex = 0;
    final int overlayYPosition = 275;
    List<Image> bobbingImages = new ArrayList<>();
    int[] bobbingOffsets = new int[7];
    int[] bobbingSpeeds = new int[7];
    final int bobbingAmplitude = 20;
    int timerTick = 0;

    private final Random random = new Random();
    private GameRendererPanel drawingPanel;
    private Image[] tetrisBlockTypeImages = new Image[7];
    private Timer gameTimer;
    private int moveDownCount = 0;

    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        this.drawingPanel = new GameRendererPanel(this);
        this.drawingPanel.addKeyListener(this);

        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);
        loadBobbingImages();
        loadTetrisBlockTypeImages();

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
    public TetrisBlock getCurrentBlock() { return currentBlock; }
    public List<TetrisBlock> getLandedBlocks() { return landedBlocks; }
    public Image getBackgroundImage() { return backgroundImage; }
    public Image[] getOverlayImages() { return overlayImages; }
    public int getCurrentOverlayIndex() { return currentOverlayIndex; }
    public int getOverlayYPosition() { return overlayYPosition; }
    public List<Image> getBobbingImages() { return bobbingImages; }
    public int[] getBobbingOffsets() { return bobbingOffsets; }


    private void loadTetrisBlockTypeImages() {
        String basePath = TETRIS_BLOCK_IMAGE_DIRECTORY;
        for (int i = 0; i < 7; i++) {
            String imagePath = basePath + "piece_" + i + ".png";
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                this.tetrisBlockTypeImages[i] = null;
                continue;
            }
            try {
                this.tetrisBlockTypeImages[i] = ImageIO.read(imageFile);
            } catch (IOException e) {
                this.tetrisBlockTypeImages[i] = null;
            }
        }
    }

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
            this.backgroundImage = null;
            return;
        }
        try {
            this.backgroundImage = ImageIO.read(bgFile);
        } catch (IOException e) {
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
                this.overlayImages[i] = null;
                continue;
            }
            try {
                this.overlayImages[i] = ImageIO.read(imageFile);
            } catch (IOException e) {
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
                bobbingImages.add(null);
            }
        }
    }

    private void handleSelection() {
        if (overlayImages == null || currentOverlayIndex < 0 || currentOverlayIndex >= overlayImages.length || overlayImages[currentOverlayIndex] == null) {
            return;
        }
        switch (currentOverlayIndex) {
            case 0: case 1: case 2:
                openGameScreen(); break;
            case 3:
                showCreditsOverlay(); break;
            case 4:
                System.out.println("Quitting game.");
                System.exit(0); break;
        }
    }

    private void handleBlockLanded() {
        if (this.currentBlock != null) {
            this.landedBlocks.add(this.currentBlock);
        }
        currentBlock = spawnNewBlock();
        moveDownCount = 0;
        drawingPanel.repaint();

        if (gameTimer != null) {
            if (gameTimer.isRunning()) {
                gameTimer.stop();
            }
            gameTimer.start();
        }
    }

    public void openGameScreen() {
        inGameMode = true;
        loadBackgroundImage("./res/bg/game.png");
        landedBlocks.clear();
        currentBlock = spawnNewBlock();
        moveDownCount = 0;

        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }

        gameTimer = new Timer(1000, ae -> {
            if (inGameMode && currentBlock != null) {
                currentBlock.moveDown();
                moveDownCount++;
                drawingPanel.repaint();
                int landingThreshold = getLandingThreshold(currentBlock);
                if (moveDownCount >= landingThreshold) {
                    if (gameTimer != null && gameTimer.isRunning()) {
                        gameTimer.stop();
                    }
                    handleBlockLanded();
                }
            }
        });
        gameTimer.start();
        System.out.println("Switched to game mode.");
        drawingPanel.repaint();
        drawingPanel.requestFocusInWindow();
    }

    private TetrisBlock spawnNewBlock() {
        TetrisBlock newBlock = new TetrisBlock();
        int imageIndex = -1;

        if (tetrisBlockTypeImages != null && tetrisBlockTypeImages.length > 0) {
            List<Integer> validIndices = new ArrayList<>();
            for(int i=0; i < tetrisBlockTypeImages.length; i++) {
                if (tetrisBlockTypeImages[i] != null) {
                    validIndices.add(i);
                }
            }
            if (!validIndices.isEmpty()) {
                imageIndex = validIndices.get((int)(Math.random() * validIndices.size()));
                newBlock.blockImage = tetrisBlockTypeImages[imageIndex];
            } else {
                 newBlock.blockImage = null;
                 imageIndex = 0;
            }
        } else {
            newBlock.blockImage = null;
            imageIndex = 0;
        }
        newBlock.pieceType = imageIndex;


        switch (imageIndex) {
            case 0: // Z piece
                newBlock.sourceImagePivotX = 46; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 3.0;
                break;
            case 1: // T piece
                newBlock.sourceImagePivotX = 1.56 * 45; newBlock.sourceImagePivotY = 0.5 * 45;
                newBlock.visualWidthInGridUnits = 3.0;
                break;
            case 2: // S piece
                newBlock.sourceImagePivotX = 45; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 3.0;
                break;
            case 3: // L piece
                newBlock.sourceImagePivotX = 45; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 2.0;
                break;
            case 4: // J piece
                newBlock.sourceImagePivotX = 45.5; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 2.0;
                break;
            case 5: // I piece
                newBlock.sourceImagePivotX = 68.36; newBlock.sourceImagePivotY = 22.38;
                newBlock.visualWidthInGridUnits = 4.0;
                break;
            case 6: // O piece
                newBlock.sourceImagePivotX = 46; newBlock.sourceImagePivotY = 46;
                newBlock.visualWidthInGridUnits = 2.0;
                break;
            default:
                newBlock.sourceImagePivotX = 0; newBlock.sourceImagePivotY = 0;
                newBlock.visualWidthInGridUnits = 1.0;
                break;
        }

        newBlock.x = 4.9;
        if (newBlock.x < 0) newBlock.x = 0;
        newBlock.y = 0.7;
        return newBlock;
    }

    private int getLandingThreshold(TetrisBlock block) {
        if (block == null) return 18; // Default if block is null
        int pieceType = block.pieceType;
        int rotationAngle = block.rotationAngle;
        int defaultThreshold = 18;

        // I-piece (pieceType 5)
        if (pieceType == 5) {
            if (rotationAngle == 0 || rotationAngle == 180) { // Horizontal I-piece is 3 squares too high
                return defaultThreshold + 3; // Lands 3 units lower (21)
            } else { // Vertical I-piece (rotationAngle == 90 || rotationAngle == 270) - was 1 unit too low
                return defaultThreshold - 1; // Lands 1 unit higher (17)
            }
        }

        // L-piece (pieceType 3)
        if (pieceType == 3) {
            if (rotationAngle == 90 || rotationAngle == 270) { // Vertical L-piece - was 1 unit too low
                return defaultThreshold - 1; // Lands 1 unit higher (17)
            }
        }

        // J-piece (pieceType 4)
        if (pieceType == 4) {
            if (rotationAngle == 90 ) { // Vertical J-piece (90 deg) - was 1 unit too low
                return defaultThreshold - 1; // Lands 1 unit higher (17)
            } else if (rotationAngle == 270) { // Vertical J-piece (270 deg) - is 1 square too high
                 return defaultThreshold + 1; // Lands 1 unit lower (19)
            }
        }

        // S-piece (pieceType 2)
        if (pieceType == 2) {
            if (rotationAngle == 90) { // Vertical S-piece (90 deg) - was 1 unit too low
                return defaultThreshold - 1; // Lands 1 unit higher (17)
            } else if (rotationAngle == 270) { // Vertical S-piece (270 deg) - is 1 square too high
                 return defaultThreshold + 1; // Lands 1 unit lower (19)
            }
        }

        // Z-piece (pieceType 0)
        if (pieceType == 0) {
            if (rotationAngle == 90) { // Vertical Z-piece (90 deg) - was 1 unit too low
                return defaultThreshold - 1; // Lands 1 unit higher (17)
            } else if (rotationAngle == 270) { // Vertical Z-piece (270 deg) - is 1 square too high
                 return defaultThreshold + 1; // Lands 1 unit lower (19)
            }
        }

        return defaultThreshold; // Default threshold for all other pieces/orientations
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

    @Override public void keyTyped(KeyEvent e) {  }

    @Override public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (inGameMode) {
            if (currentBlock != null) {
                boolean repaintAfterMove = false;
                int landingThreshold = getLandingThreshold(currentBlock);

                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                    currentBlock.moveLeft();
                    repaintAfterMove = true;
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    currentBlock.moveRight();
                    repaintAfterMove = true;
                } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                    currentBlock.rotate();
                    // It's important to get the new landing threshold *after* rotation
                    landingThreshold = getLandingThreshold(currentBlock);
                    repaintAfterMove = true;
                } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) { // Soft drop
                    currentBlock.moveDown();
                    moveDownCount++;
                    if (moveDownCount >= landingThreshold) {
                         if (gameTimer != null && gameTimer.isRunning()) gameTimer.stop();
                         handleBlockLanded();
                    } else {
                        repaintAfterMove = true;
                    }
                }

                if (keyCode == KeyEvent.VK_SPACE) { // Hard drop
                    if (moveDownCount < landingThreshold) { // Check if the block hasn't already landed or passed its threshold
                        if (gameTimer != null && gameTimer.isRunning()) {
                            gameTimer.stop(); // Stop the automatic descent
                        }
                        // Loop to move the block down until it reaches its landing threshold
                        while (moveDownCount < landingThreshold) {
                            currentBlock.moveDown(); // Update block's y-coordinate
                            moveDownCount++;         // Increment the count of moves
                        }
                        // Now currentBlock.y is at its final position and moveDownCount matches landingThreshold
                        handleBlockLanded(); // Process the landing
                        // repaintAfterMove is implicitly true because handleBlockLanded calls repaint.
                    }
                } else if (repaintAfterMove) {
                    drawingPanel.repaint();
                }
            }
        } else { // Menu mode
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
                    do {
                        nextIndex = (nextIndex + 1) % numOverlays;
                    } while (overlayImages[nextIndex] == null && nextIndex != initialIndex);
                }
            } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                 if (initialIndex == -1 && numOverlays > 0) {
                    nextIndex = numOverlays - 1;
                    while(nextIndex >= 0 && overlayImages[nextIndex] == null) nextIndex--;
                    if (nextIndex < 0) nextIndex = -1;
                } else if (initialIndex != -1) {
                    do {
                        nextIndex = (nextIndex - 1 + numOverlays) % numOverlays;
                    } while (overlayImages[nextIndex] == null && nextIndex != initialIndex);
                }
            }

            if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)) {
                if (nextIndex != -1 && overlayImages[nextIndex] != null) {
                    currentOverlayIndex = nextIndex;
                    drawingPanel.repaint();
                } else if (nextIndex == initialIndex && (initialIndex == -1 || overlayImages[initialIndex] == null)) {
                    // No change if no valid selection found and current is invalid
                } else if (initialIndex != -1 && overlayImages[initialIndex] != null) {
                    // Revert to initial if new selection is invalid but initial was valid
                    currentOverlayIndex = initialIndex;
                } else if (initialIndex == -1 && nextIndex == -1) {
                     // Stay at -1 if no valid options at all
                    currentOverlayIndex = -1;
                }
            } else if (keyCode == KeyEvent.VK_ENTER) {
                if (currentOverlayIndex != -1) {
                    handleSelection();
                }
            }
        }
    }
    @Override public void keyReleased(KeyEvent e) { /* Not used */ }
}
