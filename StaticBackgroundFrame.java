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

final class GridConfig {
    public static final int GRID_WIDTH = 10;
    public static final int GRID_HEIGHT = 20;
    public static final double BLOCK_WIDTH = 1.0;
    public static final double BLOCK_HEIGHT = 1.0;
}

class TetrisBlock {
    public double x;      // The x-coordinate of the image's top-left corner on the game grid
    public double y;      // The y-coordinate of the image's top-left corner on the game grid
    public Image blockImage; // Image for the entire block/piece
    public int rotationAngle = 0; // Current rotation angle in degrees (0, 90, 180, 270)
    public double sourceImagePivotX; // X pivot point on the original source image (in pixels)
    public double sourceImagePivotY; // Y pivot point on the original source image (in pixels)


    public TetrisBlock() { 
        this.x = 1.0;  //  initial X, defined by user
        this.y = 0.85; //  initial Y
        this.blockImage = null; // Will be set after creation by ImagePanel
        this.sourceImagePivotX = 0; // Default pivot, will be set in spawnNewBlock
        this.sourceImagePivotY = 0; // Default pivot
    }

    // Moves the block down by one grid unit if possible.
    public void moveDown() {
        if (y + GridConfig.BLOCK_HEIGHT < GridConfig.GRID_HEIGHT) {
            y += GridConfig.BLOCK_HEIGHT;
        }
    }

    // Moves the block left by one grid unit if possible.
    public void moveLeft() {
    if (x - GridConfig.BLOCK_WIDTH >= minX) {
        x -= GridConfig.BLOCK_WIDTH;
        }
    }

    // Moves the block right by one grid unit if possible.
    public void moveRight() {
    if (x + visualWidthInGridUnits + GridConfig.BLOCK_WIDTH / 2.0 <= maxX) {
        x += GridConfig.BLOCK_WIDTH;
        }
    }

    // Rotates the block 90 degrees clockwise.
    public void rotate() {
        rotationAngle = (rotationAngle + 90) % 360;
    }
}

public class StaticBackgroundFrame {
    public static void main(String[] args) {
        // Define paths for resources
        String backgroundPath = "./res/bg/mainmenu.png";
        String[] overlayPaths = {
            "./res/options/easy.png",
            "./res/options/medium.png",
            "./res/options/hard.png",
            "./res/options/credits.png",
            "./res/options/quit.png"
        };

        // Create the main frame
        JFrame frame = new JFrame("Tetris Game");
        // ImagePanel acts as the main logic controller and custom drawing panel provider
        ImagePanel logicController = new ImagePanel(backgroundPath, overlayPaths);

        // Set up the frame
        frame.setContentPane(logicController.getDrawingPanel());
        frame.setSize(720, 720); // Fixed size
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);

        // Request focus for the drawing panel once the window is opened to ensure key events are captured.
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                logicController.getDrawingPanel().requestFocusInWindow();
            }
        });
        // Also request focus immediately, in case windowOpened is not timely enough.
        logicController.getDrawingPanel().requestFocusInWindow();
    }
}

class ImagePanel implements KeyListener { 
    
    // !!! EDIT THIS LINE TO CHANGE THE DIRECTORY FOR THE 7 BLOCK PNGs !!!
    private static final String TETRIS_BLOCK_IMAGE_DIRECTORY = "./res/pieces/"; 
    // Assumes piece images are named piece_0.png, piece_1.png, ..., piece_6.png

    private boolean inGameMode = false;
    private TetrisBlock currentBlock = null;
    private List<TetrisBlock> landedBlocks = new ArrayList<>(); // Stores all blocks that have landed

    private Image backgroundImage;
    private Image[] overlayImages;
    private int currentOverlayIndex = 0;
    private final int overlayYPosition = 275;

    private final List<Image> bobbingImages = new ArrayList<>(); // For menu decoration
    private final int[] bobbingOffsets = new int[7]; 
    private final int[] bobbingSpeeds = new int[7];  
    private final int bobbingAmplitude = 20;
    private int timerTick = 0;

    private final Random random = new Random(); // Random number generator for selecting blocks
    private final ActualDisplayPanel drawingPanel;

    // Array to store the 7 images for the different "block" types/overlays
    private Image[] tetrisBlockTypeImages = new Image[7];

    private Timer gameTimer; // Timer for game ticks (e.g., block falling)
    private int moveDownCount = 0; // Counter for how many times the block has moved down

    // Inner class for the actual drawing surface
    private class ActualDisplayPanel extends JPanel {
        public ActualDisplayPanel() {
            super();
            this.setLayout(null); // Using null layout for absolute positioning of drawn elements
            this.setFocusable(true); // Essential for receiving KeyEvents
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create(); // Work with a Graphics2D copy

            if (ImagePanel.this.inGameMode) {
                // Draw background image first
                if (ImagePanel.this.backgroundImage != null) {
                    g2d.drawImage(ImagePanel.this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
                } else {
                    g.setColor(java.awt.Color.BLACK); // Fallback background
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                final int blockSize = 30; // Define block size for consistent rendering

                // --- Start: Draw all landed blocks ---
                for (TetrisBlock landedBlock : ImagePanel.this.landedBlocks) {
                    if (landedBlock != null && landedBlock.blockImage != null) {
                        Graphics2D g2d_landed = (Graphics2D) g.create(); // Use a new graphics context for each landed block
                        
                        int drawX = (int)(landedBlock.x * blockSize);
                        int drawY = (int)(landedBlock.y * blockSize);

                        int naturalWidth = landedBlock.blockImage.getWidth(this);
                        int naturalHeight = landedBlock.blockImage.getHeight(this);

                        if (naturalWidth > 0 && naturalHeight > 0) { 
                            int scaledWidth = (int)(naturalWidth * 0.7); // Apply scaling
                            int scaledHeight = (int)(naturalHeight * 0.7); // Apply scaling

                            double absolutePivotX = drawX + (landedBlock.sourceImagePivotX * 0.7);
                            double absolutePivotY = drawY + (landedBlock.sourceImagePivotY * 0.7);

                            g2d_landed.rotate(Math.toRadians(landedBlock.rotationAngle), absolutePivotX, absolutePivotY);
                            g2d_landed.drawImage(landedBlock.blockImage, drawX, drawY, scaledWidth, scaledHeight, this);
                        }
                        g2d_landed.dispose(); 
                    }
                }
                // --- End: Draw all landed blocks ---

                if (ImagePanel.this.currentBlock != null) {
                    TetrisBlock block = ImagePanel.this.currentBlock;
                    if (block.blockImage != null) {
                        Graphics2D g2d_current = (Graphics2D) g.create(); // Use a new graphics context for the current block

                        int drawX = (int)(block.x * blockSize);
                        int drawY = (int)(block.y * blockSize);

                        int naturalWidth = block.blockImage.getWidth(this);
                        int naturalHeight = block.blockImage.getHeight(this);

                        if (naturalWidth > 0 && naturalHeight > 0) {
                            int scaledWidth = (int)(naturalWidth * 0.7); // Apply scaling
                            int scaledHeight = (int)(naturalHeight * 0.7); // Apply scaling

                            // Calculate pivot for rotation relative to the block's position on the panel
                            double absolutePivotX = drawX + (block.sourceImagePivotX * 0.7);
                            double absolutePivotY = drawY + (block.sourceImagePivotY * 0.7);

                            g2d_current.rotate(Math.toRadians(block.rotationAngle), absolutePivotX, absolutePivotY);
                            g2d_current.drawImage(block.blockImage, drawX, drawY, scaledWidth, scaledHeight, this);
                        } else {
                            System.err.println("Warning: currentBlock.blockImage has invalid dimensions (0 or less).");
                        }
                        g2d_current.dispose(); // Dispose of the graphics context copy
                    } 
                }
                return; // End of inGameMode drawing
            }

            // --- Menu Mode Rendering (remains unchanged) ---
            if (ImagePanel.this.backgroundImage != null) {
                g.drawImage(ImagePanel.this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            } else {
                // --- Menu Mode Rendering ---
                if (ImagePanel.this.backgroundImage != null) {
                    g2d.drawImage(ImagePanel.this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
                } else {
                    g2d.setColor(java.awt.Color.GRAY);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(java.awt.Color.RED);
                    g2d.drawString("Menu background image failed to load.", 20, 30);
                }

                // Draw current menu overlay image
                if (ImagePanel.this.overlayImages != null &&
                    ImagePanel.this.currentOverlayIndex >= 0 &&
                    ImagePanel.this.currentOverlayIndex < ImagePanel.this.overlayImages.length &&
                    ImagePanel.this.overlayImages[ImagePanel.this.currentOverlayIndex] != null) {
                    Image currentOverlay = ImagePanel.this.overlayImages[ImagePanel.this.currentOverlayIndex];
                    int overlayWidth = currentOverlay.getWidth(this);
                    int panelWidth = this.getWidth();
                    int x = (panelWidth - overlayWidth) / 2; // Center the overlay
                    g2d.drawImage(currentOverlay, x, ImagePanel.this.overlayYPosition, this);
                } else if (ImagePanel.this.currentOverlayIndex != -1 && ImagePanel.this.overlayImages != null && ImagePanel.this.overlayImages.length > 0) {
                    g2d.setColor(java.awt.Color.YELLOW);
                    g2d.drawString("Selected overlay image failed to load.", 20, 50);
                }

                // Draw bobbing images for menu decoration
                int[] bobx = {-100, -60, 430, -250, 300, 550, -140}; // X positions for bobbing images
                int[] boby = {225, -160, 0, 0, 500, 300, 460};   // Base Y positions for bobbing images

                for (int i = 0; i < ImagePanel.this.bobbingImages.size(); i++) {
                    Image img = ImagePanel.this.bobbingImages.get(i);
                    if (img != null) {
                        if (i < bobx.length && i < boby.length && i < ImagePanel.this.bobbingOffsets.length) {
                            int x = bobx[i];
                            int y = boby[i] + ImagePanel.this.bobbingOffsets[i]; // Apply bobbing offset
                            g2d.drawImage(img, x, y, this);
                        }
                    }
                }
            }
            g2d.dispose(); // Dispose the main Graphics2D copy
        }
    }

    // Constructor for ImagePanel
    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        this.drawingPanel = new ActualDisplayPanel();
        this.drawingPanel.addKeyListener(this); // Register this ImagePanel to listen for key events from drawingPanel

        // Load all necessary image resources
        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);
        loadBobbingImages();
        loadTetrisBlockTypeImages();

        // Initialize the current overlay index, ensuring it points to a valid, loaded image if possible
        if (overlayImages == null || overlayImages.length == 0) {
            currentOverlayIndex = -1; // No overlays available
        } else {
            currentOverlayIndex = 0; // Start with the first overlay
            // Find the first non-null overlay image
            while (currentOverlayIndex < overlayImages.length && overlayImages[currentOverlayIndex] == null) {
                currentOverlayIndex++;
            }
            if (currentOverlayIndex >= overlayImages.length) { // If all overlay images failed to load
                currentOverlayIndex = -1;
            }
        }
        startBobbingAnimation(); // Start the menu decoration animation
    }

    // Loads the images for the 7 Tetris block types
    private void loadTetrisBlockTypeImages() {
        String basePath = TETRIS_BLOCK_IMAGE_DIRECTORY;
        for (int i = 0; i < 7; i++) { // Assuming 7 block types, named piece_0.png to piece_6.png
            String imagePath = basePath + "piece_" + i + ".png";
            try {
                File imageFile = new File(imagePath);
                this.tetrisBlockTypeImages[i] = ImageIO.read(imageFile);
                if (this.tetrisBlockTypeImages[i] == null && imageFile.exists()) {
                    System.err.println("Warning: Block image loaded as null from existing file: " + imagePath);
                } else if (!imageFile.exists()) {
                    System.err.println("Error: Block image file not found: " + imagePath);
                    this.tetrisBlockTypeImages[i] = null; // Ensure it's null if not found
                } else {
                    System.out.println("Loaded block image: " + imagePath);
                }
            } catch (IOException e) {
                System.err.println("IOException occurred loading block image: " + imagePath + " - " + e.getMessage());
                this.tetrisBlockTypeImages[i] = null; // Ensure it's null on error
            }
        }
    }

    // Provides access to the drawing panel for the JFrame
    public JPanel getDrawingPanel() { return this.drawingPanel; }

    // Starts the animation timer for the bobbing images in the menu
    private void startBobbingAnimation() {
        Timer animationTimer = new Timer(16, e -> { // Approximately 60 FPS
            timerTick++;
            for (int i = 0; i < bobbingOffsets.length; i++) {
                if (i < bobbingSpeeds.length && bobbingSpeeds[i] > 0) { // Ensure speed is valid
                    // Calculate bobbing offset using a sine wave
                    bobbingOffsets[i] = (int) (Math.sin((timerTick + i * 20) / (double) bobbingSpeeds[i]) * bobbingAmplitude);
                }
            }
            drawingPanel.repaint(); // Redraw the panel to show updated positions
        });
        animationTimer.start();
    }

    // Loads the background image
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
            System.err.println("IOException occurred during background image load: " + path + " - " + e.getMessage());
            this.backgroundImage = null; // Set to null on error
        }
    }

    // Loads the overlay images for menu options
    private void loadOverlayImages(String[] paths) {
        if (paths == null || paths.length == 0) {
            this.overlayImages = new Image[0]; // Initialize as empty if no paths
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
                System.err.println("IOException occurred during overlay image load: " + paths[i] + " - " + e.getMessage());
                this.overlayImages[i] = null; // Set to null on error
            }
        }
    }

    // Loads images for the bobbing decoration in the menu
    private void loadBobbingImages() {
        bobbingImages.clear();
        String bobbingBasePath = "./res/pieces/glow/"; // Path to glowing piece images
        for (int i = 1; i <= 7; i++) { // Assuming 7 glowing images, named 1.png to 7.png
            String imagePath = bobbingBasePath + i + ".png";
            try {
                File imageFile = new File(imagePath);
                Image loadedImage = ImageIO.read(imageFile);
                if (loadedImage != null) {
                    bobbingImages.add(loadedImage);
                    // Initialize offset and speed for the newly added bobbing image
                    if (bobbingImages.size() -1 < bobbingOffsets.length && bobbingImages.size() -1 < bobbingSpeeds.length) {
                        bobbingOffsets[bobbingImages.size() - 1] = 0;
                        bobbingSpeeds[bobbingImages.size() - 1] = (int) (Math.random() * 50 + 50); // Random speed
                    }
                } else {
                    if (imageFile.exists()) {
                        System.err.println("Warning: Bobbing image loaded as null from existing file: " + imagePath);
                    } else {
                        System.err.println("Error: Bobbing image file not found: " + imagePath);
                    }
                    bobbingImages.add(null); // Add null if loading failed to maintain list size integrity if needed elsewhere
                }
            } catch (IOException e) {
                System.err.println("IOException occurred during bobbing image load: " + imagePath + " - " + e.getMessage());
                bobbingImages.add(null); // Add null on error
            }
        }
    }

    // Handles menu selection when Enter is pressed
    private void handleSelection() {
        if (overlayImages == null || currentOverlayIndex < 0 || currentOverlayIndex >= overlayImages.length || overlayImages[currentOverlayIndex] == null) {
            System.out.println("No valid option selected or option image is null.");
            return;
        }
        // Actions based on the selected menu item
        switch (currentOverlayIndex) {
            case 0: case 1: case 2: 
                openGameScreen(); break;
            case 3: 
                showCreditsOverlay(); break;
            case 4: 
                System.exit(0); break;
        }
    }

    // Method to handle actions when a block has landed
    private void handleBlockLanded() {
        // Add the just-landed block to the list of landed blocks
        if (this.currentBlock != null) { 
            this.landedBlocks.add(this.currentBlock);
            System.out.println("Added current block to landedBlocks. Total landed: " + landedBlocks.size());
        } else {
            // This case should ideally not happen if currentBlock is managed correctly
            System.err.println("Warning: handleBlockLanded called but currentBlock was null. No block added to landed list.");
        }

        System.out.println("Block landed. Spawning new block.");
        currentBlock = spawnNewBlock();
        moveDownCount = 0;             

        drawingPanel.repaint();         // Repaint to show the new block and updated landed blocks


        if (gameTimer != null) {
            if (gameTimer.isRunning()) {
                gameTimer.stop();
            }
            gameTimer.start(); // Start the timer for the new block
            System.out.println("Timer restarted for new block's fall.");
        } else {
            System.err.println("CRITICAL ERROR: gameTimer is null in handleBlockLanded. Cannot restart block fall.");
        }
    }

    // Switches to the game screen
    public void openGameScreen() {
        inGameMode = true;
        loadBackgroundImage("./res/bg/game.png"); // Load game background
        overlayImages = null; // Clear menu overlays
        bobbingImages.clear(); // Clear menu decorations
        landedBlocks.clear(); // Clear blocks from any previous game session
        currentBlock = spawnNewBlock();
        moveDownCount = 0;

        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }

        gameTimer = new Timer(1000, ae -> {
            if (inGameMode && currentBlock != null) {
                currentBlock.moveDown();
                moveDownCount++;
                if (currentBlock.y >= GridConfig.GRID_HEIGHT - GridConfig.BLOCK_HEIGHT || moveDownCount >= 18) {
                    if (gameTimer != null && gameTimer.isRunning()) {
                        gameTimer.stop();
                    }
                    System.out.println("Block automatically reached bottom or max fall count. Handling landed block.");
                    handleBlockLanded();
                } else {
                    drawingPanel.repaint();
                }
            }
        });
        gameTimer.start();

        System.out.println("Switched to game mode. Initial X: " + (currentBlock != null ? currentBlock.x : "null"));
        drawingPanel.repaint();
    }

    private TetrisBlock spawnNewBlock() {
        TetrisBlock newBlock = new TetrisBlock();
        int imageIndex = -1;

        if (tetrisBlockTypeImages != null && tetrisBlockTypeImages.length > 0) {
            imageIndex = random.nextInt(tetrisBlockTypeImages.length);

            if (tetrisBlockTypeImages[imageIndex] != null) {
                newBlock.blockImage = tetrisBlockTypeImages[imageIndex];
                System.out.println("Spawning block with image index: " + imageIndex);
            } else {
                System.err.println("Warning: Randomly selected image at index " + imageIndex + " is null. Attempting fallback.");
                boolean foundFallback = false;
                for (int i = 0; i < tetrisBlockTypeImages.length; i++) {
                    if (tetrisBlockTypeImages[i] != null) {
                        newBlock.blockImage = tetrisBlockTypeImages[i];
                        imageIndex = i;
                        System.out.println("Fell back to block image index: " + i);
                        foundFallback = true;
                        break;
                    }
                }
                if (!foundFallback) {
                    System.err.println("CRITICAL: No block images available to spawn. newBlock.blockImage will be null.");
                    newBlock.blockImage = null;
                    imageIndex = 0;
                }
            }
        } else {
            System.err.println("CRITICAL: tetrisBlockTypeImages array is null or empty. Cannot spawn new block.");
            newBlock.blockImage = null;
            imageIndex = 0; // Default to prevent switch errors
        }

        switch (imageIndex) {
            case 0: // Z piece
                newBlock.sourceImagePivotX = 45; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 1.0;
                newBlock.minX = 1.0;
                newBlock.maxX = GridConfig.GRID_WIDTH;
                break;
            case 1: // T piece
                newBlock.sourceImagePivotX = 1.55 * 45; newBlock.sourceImagePivotY = 0.5 * 45;
                newBlock.visualWidthInGridUnits = 3.0;
                newBlock.minX = 0.0;
                newBlock.maxX = GridConfig.GRID_WIDTH;
                break;
            case 2: // S piece
                newBlock.sourceImagePivotX = 45; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 0.0;
                newBlock.minX = 0.0;
                newBlock.maxX = GridConfig.GRID_WIDTH;
                break;
            case 3: // L piece
                newBlock.sourceImagePivotX = 45; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 0.0; 
                newBlock.minX = 0.5;
                newBlock.maxX = GridConfig.GRID_WIDTH - 0.5;
                break;
            case 4: // J piece
                newBlock.sourceImagePivotX = 45; newBlock.sourceImagePivotY = 45;
                newBlock.visualWidthInGridUnits = 2.0;
                newBlock.minX = 0.5;
                newBlock.maxX = GridConfig.GRID_WIDTH - 0.5;
                break;
            case 5: // I piece (line)
                newBlock.sourceImagePivotX = (4 * 47) / 2.75; newBlock.sourceImagePivotY = (1 * 47) / 2.1; 
                newBlock.visualWidthInGridUnits = 4.0;
                newBlock.minX = 0.5;
                newBlock.maxX = GridConfig.GRID_WIDTH - 3.5;
                break;
            case 6: // O piece (square)
                newBlock.sourceImagePivotX = (2 * 46) / 2.0; newBlock.sourceImagePivotY = (2 * 46) / 2.0;
                newBlock.visualWidthInGridUnits = 2.0;
                newBlock.minX = 1.0;
                newBlock.maxX = GridConfig.BLOCK_WIDTH - 1.0;
                break;
                
            default:
                System.err.println("Warning: spawnNewBlock imageIndex " + imageIndex + " is unexpected. Using default pivot/width.");
                newBlock.sourceImagePivotX = 0; newBlock.sourceImagePivotY = 0;
                newBlock.visualWidthInGridUnits = 1.0;
                newBlock.minX = 0.0;
                newBlock.maxX = GridConfig.GRID_WIDTH;
                break;
        }

        newBlock.x = (GridConfig.GRID_WIDTH - newBlock.visualWidthInGridUnits) / 2.0;
        newBlock.y = 0.0;

        if (newBlock.blockImage == null) {
            System.err.println("Spawned block with null image. Game might not be playable. ImageIndex was: " + imageIndex);
        }
        return newBlock;
    }


    private void showCreditsOverlay() {
        System.out.println("Showing credits...");
        JFrame creditsFrame = new JFrame("Credits");
        ImagePanel creditsLogic = new ImagePanel("./res/credits.png", null); // Assuming credits.png is a full screen image

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
        creditsLogic.getDrawingPanel().requestFocusInWindow(); // Ensure it can receive events if needed
    }

    @Override public void keyTyped(KeyEvent e) { /* Not used */ }

    @Override public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (inGameMode) {
            if (currentBlock != null) {
                boolean repaintAfterMove = false;

                if (gameTimer != null && gameTimer.isRunning()) {
                    if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                        currentBlock.moveLeft();
                        repaintAfterMove = true;
                    } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                        currentBlock.moveRight();
                        repaintAfterMove = true;
                    } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                        currentBlock.rotate();
                        repaintAfterMove = true;
                    }

                }

                if (keyCode == KeyEvent.VK_SPACE) {
                    if (moveDownCount < 18) { 
                        if (gameTimer != null && gameTimer.isRunning()) {
                            gameTimer.stop();
                        }

                        int remainingSteps = 18 - moveDownCount;
                        currentBlock.y += remainingSteps * GridConfig.BLOCK_HEIGHT;
                        if (currentBlock.y >= GridConfig.GRID_HEIGHT - GridConfig.BLOCK_HEIGHT) {
                             currentBlock.y = GridConfig.GRID_HEIGHT - GridConfig.BLOCK_HEIGHT;
                        }


                        moveDownCount = 18;
                        System.out.println("Block hard dropped. Handling landed block.");
                        handleBlockLanded(); 
                    }
                } else if (repaintAfterMove) {
                    drawingPanel.repaint();
                }
            }
        } else {
            if (overlayImages == null || overlayImages.length == 0) return;

            int numOverlays = overlayImages.length;
            int initialIndex = currentOverlayIndex;
            int nextIndex = currentOverlayIndex;

            if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                do {
                    nextIndex = (nextIndex + 1) % numOverlays;
                } while (overlayImages[nextIndex] == null && nextIndex != initialIndex); // Skip null/unloaded images
            } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                // Move to the previous valid overlay image
                do {
                    nextIndex = (nextIndex - 1 + numOverlays) % numOverlays;
                } while (overlayImages[nextIndex] == null && nextIndex != initialIndex); // Skip null/unloaded images
            }

            // Update current selection if a valid next option was found
            if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)) {
                if (overlayImages[nextIndex] != null) {
                    currentOverlayIndex = nextIndex;
                    drawingPanel.repaint();
                } else if (nextIndex == initialIndex && overlayImages[initialIndex] == null) {
                    // All images might be null, do nothing
                } else if (overlayImages[initialIndex] != null) {
                    currentOverlayIndex = initialIndex; // Stay on current if loop couldn't find other valid
                }
            } else if (keyCode == KeyEvent.VK_ENTER) {
                handleSelection(); // Handle menu item selection
            }
        }
    }
    @Override public void keyReleased(KeyEvent e) { /* Not used */ }
}