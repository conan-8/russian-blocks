import java.awt.Graphics;
import java.awt.Graphics2D; // Added for rotation
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

// TetrisBlock class now represents a simple movable, rotatable image
class TetrisBlock {
    public double x;      // The x-coordinate of the image's top-left corner on the game grid
    public double y;      // The y-coordinate of the image's top-left corner on the game grid
    public Image blockImage; // Image for the entire block/piece
    public int rotationAngle = 0; // Current rotation angle in degrees (0, 90, 180, 270)
    public double sourceImagePivotX; // X pivot point on the original source image (in pixels)
    public double sourceImagePivotY; // Y pivot point on the original source image (in pixels)


    public TetrisBlock() { 
        this.x = 1.0;  // User-defined initial X
        this.y = 0.85; // User-defined initial Y
        this.blockImage = null; // Will be set after creation by ImagePanel
        this.sourceImagePivotX = 0; // Default pivot, will be set in spawnNewBlock
        this.sourceImagePivotY = 0; // Default pivot
    }

    // Method to move the block down
    public void moveDown() {
        y += 1.0; 
    }

    public void moveLeft() {
        x -= 1.12; 
    }

    public void moveRight() {
        x += 1.12; 
    }

    // Cycles through 0, 90, 180, 270 degrees
    public void rotate() {
        rotationAngle = (rotationAngle + 90) % 360;
    }
}

// Main class to launch the application
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
    
    // !!! EDIT THIS LINE TO CHANGE THE DIRECTORY FOR THE 7 BLOCK PNGs !!!
    private static final String TETRIS_BLOCK_IMAGE_DIRECTORY = "./res/pieces/"; 
    // Assumes piece images are named piece_0.png, piece_1.png, ..., piece_6.png

    private boolean inGameMode = false;
    private TetrisBlock currentBlock = null;

    private Image backgroundImage;
    private Image[] overlayImages;
    private int currentOverlayIndex = 0;
    private final int overlayYPosition = 275;

    private final List<Image> bobbingImages = new ArrayList<>(); // For menu decoration
    private final int[] bobbingOffsets = new int[7]; 
    private final int[] bobbingSpeeds = new int[7];  
    private final int bobbingAmplitude = 20;
    private int timerTick = 0;

    private final Random random = new Random();
    private final ActualDisplayPanel drawingPanel;

    // Array to store the 7 images for the different "block" types/overlays
    private Image[] tetrisBlockTypeImages = new Image[7];

    private class ActualDisplayPanel extends JPanel {
        public ActualDisplayPanel() {
            super();
            this.setLayout(null); 
            this.setFocusable(true); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 

            if (ImagePanel.this.inGameMode) {
                if (ImagePanel.this.backgroundImage != null) {
                    g.drawImage(ImagePanel.this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
                } else {
                    g.setColor(java.awt.Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                if (ImagePanel.this.currentBlock != null) {
                    int blockSize = 30; // Defines the grid unit size for positioning
                    TetrisBlock block = ImagePanel.this.currentBlock;
                    
                    if (block.blockImage != null) {
                        Graphics2D g2d = (Graphics2D) g.create(); // Work on a copy

                        int drawX = (int)(block.x * blockSize); 
                        int drawY = (int)(block.y * blockSize);
                        
                        int naturalWidth = block.blockImage.getWidth(this);
                        int naturalHeight = block.blockImage.getHeight(this);

                        if (naturalWidth > 0 && naturalHeight > 0) { 
                            int scaledWidth = (int)(naturalWidth * 0.7);
                            int scaledHeight = (int)(naturalHeight * 0.7);

                            // Calculate the absolute pivot point on the panel
                            // The pivot is defined on the source image, then scaled, then offset by drawX/drawY
                            double absolutePivotX = drawX + (block.sourceImagePivotX * 0.7);
                            double absolutePivotY = drawY + (block.sourceImagePivotY * 0.7);

                            g2d.rotate(Math.toRadians(block.rotationAngle), absolutePivotX, absolutePivotY);
                            g2d.drawImage(block.blockImage, drawX, drawY, scaledWidth, scaledHeight, this); 
                        } else {
                            System.err.println("Warning: currentBlock.blockImage has invalid dimensions (0 or less).");
                        }
                        g2d.dispose(); // Release the copied graphics context
                    } 
                }
                return;
            }

            // --- Menu Mode Rendering ---
            if (ImagePanel.this.backgroundImage != null) {
                g.drawImage(ImagePanel.this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g.setColor(java.awt.Color.GRAY); 
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(java.awt.Color.RED);
                g.drawString("Background image failed to load.", 20, 30);
            }

            if (ImagePanel.this.overlayImages != null && 
                ImagePanel.this.currentOverlayIndex >= 0 && 
                ImagePanel.this.currentOverlayIndex < ImagePanel.this.overlayImages.length && 
                ImagePanel.this.overlayImages[ImagePanel.this.currentOverlayIndex] != null) {
                Image currentOverlay = ImagePanel.this.overlayImages[ImagePanel.this.currentOverlayIndex];
                int overlayWidth = currentOverlay.getWidth(this);
                int panelWidth = this.getWidth();
                int x = (panelWidth - overlayWidth) / 2; 
                g.drawImage(currentOverlay, x, ImagePanel.this.overlayYPosition, this);
            } else if (ImagePanel.this.currentOverlayIndex != -1 && ImagePanel.this.overlayImages != null && ImagePanel.this.overlayImages.length > 0) { 
                g.setColor(java.awt.Color.YELLOW);
                g.drawString("Selected overlay image failed to load.", 20, 50);
            }

            int[] bobx = {-100, -60, 430, -250, 300, 550, -140}; 
            int[] boby = {225, -160, 0, 0, 500, 300, 460}; 

            for (int i = 0; i < ImagePanel.this.bobbingImages.size(); i++) {
                Image img = ImagePanel.this.bobbingImages.get(i);
                if (img != null) {
                    if (i < bobx.length && i < boby.length && i < ImagePanel.this.bobbingOffsets.length) {
                        int x = bobx[i];
                        int y = boby[i] + ImagePanel.this.bobbingOffsets[i];
                        g.drawImage(img, x, y, this);
                    }
                }
            }
        }
    } 

    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        this.drawingPanel = new ActualDisplayPanel(); 
        this.drawingPanel.addKeyListener(this); 

        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);
        loadBobbingImages(); 
        loadTetrisBlockTypeImages(); // Load the 7 block/piece images

        if (overlayImages == null || overlayImages.length == 0) {
            currentOverlayIndex = -1;
        } else {
            currentOverlayIndex = 0; 
            while (currentOverlayIndex < overlayImages.length && overlayImages[currentOverlayIndex] == null) {
                currentOverlayIndex++;
            }
            if (currentOverlayIndex >= overlayImages.length) { 
                currentOverlayIndex = -1;
            }
        }
        startBobbingAnimation();
    }

    // Method to load the 7 PNGs for the different "block" types/overlays
    private void loadTetrisBlockTypeImages() {
        String basePath = TETRIS_BLOCK_IMAGE_DIRECTORY; 
        for (int i = 0; i < 7; i++) {
            String imagePath = basePath + "piece_" + i + ".png"; 
            try {
                File imageFile = new File(imagePath);
                this.tetrisBlockTypeImages[i] = ImageIO.read(imageFile);
                if (this.tetrisBlockTypeImages[i] == null && imageFile.exists()) {
                     System.err.println("Warning: Block image loaded as null from existing file: " + imagePath);
                } else if (!imageFile.exists()) {
                     System.err.println("Error: Block image file not found: " + imagePath);
                     this.tetrisBlockTypeImages[i] = null; 
                } else {
                    System.out.println("Loaded block image: " + imagePath);
                }
            } catch (IOException e) {
                System.err.println("IOException occurred loading block image: " + imagePath + " - " + e.getMessage());
                this.tetrisBlockTypeImages[i] = null; 
            }
        }
    }
    
    public JPanel getDrawingPanel() { return this.drawingPanel; }
    
    private void startBobbingAnimation() { 
        Timer timer = new Timer(16, e -> { 
            timerTick++;
            for (int i = 0; i < bobbingOffsets.length; i++) {
                if (i < bobbingSpeeds.length && bobbingSpeeds[i] > 0) { 
                     bobbingOffsets[i] = (int) (Math.sin((timerTick + i * 20) / (double) bobbingSpeeds[i]) * bobbingAmplitude);
                }
            }
            drawingPanel.repaint(); 
        });
        timer.start();
    }

    private void loadBackgroundImage(String path) { 
        try {
            File bgFile = new File(path);
            this.backgroundImage = ImageIO.read(bgFile); 
            if (this.backgroundImage == null && bgFile.exists()) { 
                System.err.println("Warning: Background image loaded as null from existing file: " + path);
            } else if (!bgFile.exists()) {
                System.err.println("Error: Background image file not found: " + path);
                this.backgroundImage = null; 
            } else {
                System.out.println("Background image assumed loaded successfully: " + path);
            }
        } catch (IOException e) {
             System.err.println("IOException occurred during background image load, and was ignored: " + path + " - " + e.getMessage());
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
            try {
                File imageFile = new File(paths[i]);
                this.overlayImages[i] = ImageIO.read(imageFile);
                if (this.overlayImages[i] == null && imageFile.exists()) {
                     System.err.println("Warning: Overlay image loaded as null from existing file: " + paths[i]);
                } else if (!imageFile.exists()) {
                     System.err.println("Error: Overlay image file not found: " + paths[i]);
                     this.overlayImages[i] = null;
                }
            } catch (IOException e) {
                System.err.println("IOException occurred during overlay image load, and was ignored: " + paths[i] + " - " + e.getMessage());
                this.overlayImages[i] = null; 
            }
        }
    }

    private void loadBobbingImages() { 
        bobbingImages.clear(); 
        String bobbingBasePath = "./res/pieces/glow/"; 
        for (int i = 1; i <= 7; i++) { 
            String imagePath = bobbingBasePath + i + ".png";
            try {
                File imageFile = new File(imagePath);
                Image loadedImage = ImageIO.read(imageFile);
                if (loadedImage != null) {
                    bobbingImages.add(loadedImage);
                    if (bobbingImages.size() -1 < bobbingOffsets.length && bobbingImages.size() -1 < bobbingSpeeds.length) {
                        bobbingOffsets[bobbingImages.size() - 1] = 0;
                        bobbingSpeeds[bobbingImages.size() - 1] = (int) (Math.random() * 50 + 50);
                    }
                } else {
                    if (imageFile.exists()) {
                        System.err.println("Warning: Bobbing image loaded as null from existing file: " + imagePath);
                    } else {
                        System.err.println("Error: Bobbing image file not found: " + imagePath);
                    }
                    bobbingImages.add(null); 
                }
            } catch (IOException e) {
                 System.err.println("IOException occurred during bobbing image load, and was ignored: " + imagePath + " - " + e.getMessage());
                bobbingImages.add(null); 
            }
        }
    }

    private void handleSelection() { 
         if (overlayImages == null || currentOverlayIndex < 0 || currentOverlayIndex >= overlayImages.length || overlayImages[currentOverlayIndex] == null) {
            System.out.println("No valid option selected or option image assumed loaded but is null.");
            return;
        }
        switch (currentOverlayIndex) {
            case 0: case 1: case 2: 
                openGameScreen(); break;
            case 3: 
                showCreditsOverlay(); break;
            case 4: 
                System.exit(0); break;
        }
    }

    private void openGameScreen() {
        inGameMode = true;
        loadBackgroundImage("./res/bg/game.png"); 
        overlayImages = null; 
        bobbingImages.clear();
        currentBlock = spawnNewBlock(); 

        System.out.println("Switched to game mode. Block will not fall. Initial X: " + (currentBlock != null ? currentBlock.x : "null"));
        drawingPanel.repaint(); 
    }

    // Spawns a block with a randomly chosen image and sets its pivot point
    private TetrisBlock spawnNewBlock() {
        TetrisBlock newBlock = new TetrisBlock(); 
        
        int imageIndex = random.nextInt(tetrisBlockTypeImages.length); 
        
        if (imageIndex < tetrisBlockTypeImages.length) {
            newBlock.blockImage = tetrisBlockTypeImages[imageIndex];
        } else {
            System.err.println("Error: Random image index out of bounds for block type images.");
            newBlock.blockImage = null; 
            newBlock.sourceImagePivotX = 0; 
            newBlock.sourceImagePivotY = 0;
            return newBlock;
        }

        // Define pivot points (in pixels on the source image) for each piece type
        // User piece order: 0:Z, 1:T, 2:S, 3:L, 4:J, 5:I, 6:O
        // IMPORTANT: These values MUST match how your piece_0.png to piece_6.png are designed.
        // Pivots for Z,S,L,J assume a 30px square unit for their source images.
        // Pivots for I,O,T now assume a 33px square unit for their source images.
        switch (imageIndex) {
            case 0: // Z-shape (e.g., source image is 90x60 if conceptual blockSize=30)
                newBlock.sourceImagePivotX = 45; // Pivot based on 30px unit: (1.5 * 30)
                newBlock.sourceImagePivotY = 45; // Pivot based on 30px unit: (1.5 * 30)
                break;
            case 1: // T-shape (e.g., source image is 99x66 if conceptual blockSize=33)
                newBlock.sourceImagePivotX = 1.5 * 35; // 49.5
                newBlock.sourceImagePivotY = 0.5 * 35; // 16.5
                break;
            case 2: // S-shape (e.g., source image is 90x60 if conceptual blockSize=30)
                newBlock.sourceImagePivotX = 45; // Pivot based on 30px unit
                newBlock.sourceImagePivotY = 45; // Pivot based on 30px unit
                break;
            case 3: // L-shape (e.g., source image is 90x60 if conceptual blockSize=30)
                newBlock.sourceImagePivotX = 45; // Pivot based on 30px unit
                newBlock.sourceImagePivotY = 45; // Pivot based on 30px unit
                break;
            case 4: // J-shape (e.g., source image is 90x60 if conceptual blockSize=30)
                newBlock.sourceImagePivotX = 45; // Pivot based on 30px unit
                newBlock.sourceImagePivotY = 45; // Pivot based on 30px unit
                break;
            case 5: // I-shape (e.g., source image is 132x33 if conceptual blockSize=33, horizontal)
                newBlock.sourceImagePivotX = (4 * 35) / 2.0; // 66.0 (geometric center X)
                newBlock.sourceImagePivotY = (1 * 35) / 2.0; // 16.5 (geometric center Y)
                break;
            case 6: // O-shape (e.g., source image is 66x66 if conceptual blockSize=33)
                newBlock.sourceImagePivotX = (2 * 35) / 2.0; // 33.0 (geometric center X)
                newBlock.sourceImagePivotY = (2 * 35) / 2.0; // 33.0 (geometric center Y)
                break;
            default: 
                newBlock.sourceImagePivotX = 0; 
                newBlock.sourceImagePivotY = 0;
                break;
        }
        return newBlock;
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

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) { 
        if (inGameMode) {
            if (currentBlock != null) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                    currentBlock.moveLeft();
                } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                    currentBlock.moveRight();
                } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_SPACE) { 
                    currentBlock.rotate();
                }
                drawingPanel.repaint(); 
            }
            return;
        }

        int keyCode = e.getKeyCode();
        if (overlayImages == null || overlayImages.length == 0) return;

        int numOverlays = overlayImages.length;
        int initialIndex = currentOverlayIndex;
        int nextIndex = currentOverlayIndex;

        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            do {
                nextIndex = (nextIndex + 1) % numOverlays;
            } while (overlayImages[nextIndex] == null && nextIndex != initialIndex);
        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            do {
                nextIndex = (nextIndex - 1 + numOverlays) % numOverlays;
            } while (overlayImages[nextIndex] == null && nextIndex != initialIndex);
        }

        if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)) {
             if (overlayImages[nextIndex] != null) { 
                 currentOverlayIndex = nextIndex;
                 drawingPanel.repaint(); 
            } else if (nextIndex == initialIndex && overlayImages[initialIndex] == null) {
            } else if (overlayImages[initialIndex] != null) {
                currentOverlayIndex = initialIndex; 
            }
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            handleSelection();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
}
