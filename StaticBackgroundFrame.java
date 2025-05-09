import java.awt.Graphics;
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

// TetrisBlock class to represent a single Tetris piece
class TetrisBlock {
    public int[][] shape; // The 2D array representing the block's logical shape and bounding box
    public double x;      // The x-coordinate of the block on the game grid (top-left of bounding box)
    public double y;      // The y-coordinate of the block on the game grid (top-left of bounding box)
    public Image blockImage; // Image for the entire block

    public TetrisBlock(int[][] shape) {
        this.shape = shape;
        this.x = 0.9; 
        this.y = 0.0; 
        this.blockImage = null; // Will be set after creation by ImagePanel
    }

    // Method to move the block down
    public void moveDown() {
        y += 1.0; 
    }

    public void moveLeft() {
        x -= 1.13; 
    }

    public void moveRight() {
        x += 1.13; 
    }

    public void rotate() {
        // Rotation logic still manipulates the 'shape' array to redefine the bounding box
        // and how the block interacts with the grid logically.
        if (shape == null || shape.length == 0) return;
        int rows = shape.length;
        int cols = shape[0].length;
        if (cols == 0) return; 
        int[][] newShape = new int[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                newShape[c][rows - 1 - r] = shape[r][c];
            }
        }
        this.shape = newShape;
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
    // --- Configuration for Tetris Piece Images ---
    // !!! EDIT THIS LINE TO CHANGE THE DIRECTORY FOR FULL TETRIS BLOCK PNGs !!!
    private static final String TETRIS_BLOCK_IMAGE_DIRECTORY = "./res/full_block_pieces/"; 
    // Assumes piece images are named block_type_0.png, block_type_1.png, ..., block_type_6.png
    // Each PNG should be the image for the *entire* block shape.

    private boolean inGameMode = false;
    private TetrisBlock currentBlock = null;

    private Image backgroundImage;
    private Image[] overlayImages;
    private int currentOverlayIndex = 0;
    private final int overlayYPosition = 275;

    private final List<Image> bobbingImages = new ArrayList<>(); // These are still segment images for the menu
    private final int[] bobbingOffsets = new int[7]; 
    private final int[] bobbingSpeeds = new int[7];  
    private final int bobbingAmplitude = 20;
    private int timerTick = 0;

    private final Random random = new Random();
    private final ActualDisplayPanel drawingPanel;

    // Array to store the images for each of the 7 Tetris block types
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
                    int blockSize = 30; // This now primarily defines the grid unit size for positioning
                    TetrisBlock block = ImagePanel.this.currentBlock;
                    
                    // Draw the single image for the entire block
                    if (block.blockImage != null) {
                        // block.x and block.y are grid coordinates for the top-left of the block's bounding box
                        int drawX = (int)(block.x * blockSize); 
                        int drawY = (int)(block.y * blockSize);
                        
                        // Draw the image using its natural width and height.
                        // The PNG itself should be sized correctly (e.g., an I-block PNG might be 120x30 if blockSize is 30).
                        g.drawImage(block.blockImage, drawX, drawY, this); 
                    } else {
                        // Fallback: if the full block image is missing, you could draw its shape with rectangles
                        // For simplicity, this fallback is basic.
                        if (block.shape != null) {
                            g.setColor(java.awt.Color.DARK_GRAY);
                            for (int r = 0; r < block.shape.length; r++) {
                                if (block.shape[r] != null) { 
                                    for (int c = 0; c < block.shape[r].length; c++) {
                                        if (block.shape[r][c] == 1) {
                                            int segmentDrawX = (int)((block.x + c) * blockSize);
                                            int segmentDrawY = (int)((block.y + r) * blockSize);
                                            g.fillRect(segmentDrawX, segmentDrawY, blockSize, blockSize);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return;
            }

            // --- Menu Mode Rendering (Corrected) ---
            if (ImagePanel.this.backgroundImage != null) {
                g.drawImage(ImagePanel.this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                g.setColor(java.awt.Color.GRAY); // Fallback color for background
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(java.awt.Color.RED);
                g.drawString("Background image failed to load.", 20, 30);
            }

            // Draw the currently selected menu option overlay
            if (ImagePanel.this.overlayImages != null && 
                ImagePanel.this.currentOverlayIndex >= 0 && 
                ImagePanel.this.currentOverlayIndex < ImagePanel.this.overlayImages.length && 
                ImagePanel.this.overlayImages[ImagePanel.this.currentOverlayIndex] != null) {
                Image currentOverlay = ImagePanel.this.overlayImages[ImagePanel.this.currentOverlayIndex];
                int overlayWidth = currentOverlay.getWidth(this);
                int panelWidth = this.getWidth();
                int x = (panelWidth - overlayWidth) / 2; // Center the overlay horizontally
                g.drawImage(currentOverlay, x, ImagePanel.this.overlayYPosition, this);
            } else if (ImagePanel.this.currentOverlayIndex != -1 && ImagePanel.this.overlayImages != null && ImagePanel.this.overlayImages.length > 0) { 
                // Only show "failed to load" if there were supposed to be overlays
                g.setColor(java.awt.Color.YELLOW);
                g.drawString("Selected overlay image failed to load.", 20, 50);
            }

            // Define fixed positions for bobbing images (ensure these arrays are correctly sized if you change number of bobbing images)
            int[] bobx = {-100, -60, 430, -250, 300, 550, -140}; 
            int[] boby = {225, -160, 0, 0, 500, 300, 460}; 

            // Draw bobbing images
            for (int i = 0; i < ImagePanel.this.bobbingImages.size(); i++) {
                Image img = ImagePanel.this.bobbingImages.get(i);
                if (img != null) {
                    // Ensure bobx, boby, and bobbingOffsets have enough elements for the current image
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
        loadBobbingImages(); // For menu
        loadTetrisBlockTypeImages(); // Load images for full Tetris blocks

        // Initialize currentOverlayIndex, skipping nulls
        if (overlayImages == null || overlayImages.length == 0) {
            currentOverlayIndex = -1;
        } else {
            currentOverlayIndex = 0; // Start at the first one
            while (currentOverlayIndex < overlayImages.length && overlayImages[currentOverlayIndex] == null) {
                currentOverlayIndex++;
            }
            if (currentOverlayIndex >= overlayImages.length) { // All overlays were null
                currentOverlayIndex = -1;
            }
        }
        startBobbingAnimation();
    }

    // Method to load the 7 PNGs for *full* Tetris block types
    private void loadTetrisBlockTypeImages() {
        String basePath = TETRIS_BLOCK_IMAGE_DIRECTORY; 
        for (int i = 0; i < 7; i++) {
            // Expecting names like block_type_0.png, block_type_1.png, etc.
            // These are images of the *entire block*, not segments.
            String imagePath = basePath + "block_type_" + i + ".png"; 
            try {
                File imageFile = new File(imagePath);
                this.tetrisBlockTypeImages[i] = ImageIO.read(imageFile);
                if (this.tetrisBlockTypeImages[i] == null && imageFile.exists()) {
                     System.err.println("Warning: Full Tetris block image loaded as null from existing file: " + imagePath);
                } else if (!imageFile.exists()) {
                     System.err.println("Error: Full Tetris block image file not found: " + imagePath);
                     this.tetrisBlockTypeImages[i] = null; 
                } else {
                    System.out.println("Loaded full Tetris block image: " + imagePath);
                }
            } catch (IOException e) {
                System.err.println("IOException occurred loading full Tetris block image: " + imagePath + " - " + e.getMessage());
                this.tetrisBlockTypeImages[i] = null; 
            }
        }
    }
    
    public JPanel getDrawingPanel() { return this.drawingPanel; }
    
    private void startBobbingAnimation() { 
        Timer timer = new Timer(16, e -> { // Roughly 60 FPS
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
        String bobbingBasePath = "./res/pieces/glow/"; // Path for bobbing images
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
            case 0: case 1: case 2: // Easy, Medium, Hard
                openGameScreen(); break;
            case 3: // Credits
                showCreditsOverlay(); break;
            case 4: // Quit
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

    private TetrisBlock spawnNewBlock() {
        // The 'shapes' array still defines the logical structure and bounding box of the pieces.
        // This is important for rotation and potential collision detection.
        int[][][] shapes = { 
            {{1, 1, 1, 1}},         // I (block_type_0.png)
            {{1, 1}, {1, 1}},       // O (block_type_1.png)
            {{0, 1, 0}, {1, 1, 1}}, // T (block_type_2.png)
            {{0, 1, 1}, {1, 1, 0}}, // S (block_type_3.png)
            {{1, 1, 0}, {0, 1, 1}}, // Z (block_type_4.png)
            {{1, 0, 0}, {1, 1, 1}}, // L (block_type_5.png)
            {{0, 0, 1}, {1, 1, 1}}  // J (block_type_6.png)
        };
        int shapeIndex = random.nextInt(shapes.length);
        TetrisBlock newBlock = new TetrisBlock(shapes[shapeIndex]); // Pass the logical shape
        
        // Assign the pre-loaded image for this entire block type
        if (shapeIndex < tetrisBlockTypeImages.length) {
            newBlock.blockImage = tetrisBlockTypeImages[shapeIndex];
        } else {
            System.err.println("Error: Shape index out of bounds for full block type images.");
            newBlock.blockImage = null; 
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
                 // If all images are null or only one null image exists, stay put.
            } else if (overlayImages[initialIndex] != null) {
                // If we couldn't find another valid image but the current one is fine, stay.
                currentOverlayIndex = initialIndex; 
            }
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            handleSelection();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
}
