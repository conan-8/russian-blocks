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

class ImagePanel implements KeyListener {

    // setup the 2d arrays for block location
    public static final int GRID_ROWS = 20;
    public static final int GRID_COLS = 10;
    private int[][] gameGrid = new int[GRID_ROWS][GRID_COLS];
    private Image[] squareBlockImages = new Image[8]; 

    private List<List<int[][]>> pieceDefinitions;
    private int currentPieceType; 
    private int currentPieceRotation;
    private int currentPieceGridX;
    private int currentPieceGridY;
    private int[][] currentPieceShape; 

    private Random random = new Random();
    private Timer gameTimer;
    private int gameSpeedDelay = 1000; 

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

    // constructor method for images
    public ImagePanel(String backgroundPath, String[] overlayPaths) {
        this.drawingPanel = new GameRendererPanel(this);
        this.drawingPanel.addKeyListener(this);

        loadBackgroundImage(backgroundPath);
        loadOverlayImages(overlayPaths);
        loadBobbingImages(); 
        loadSquareBlockImages(); 
        initializePieceDefinitions();

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

    //manually adds the rotations and shapes for the pieces
    private void initializePieceDefinitions() {
        pieceDefinitions = new ArrayList<>();
        // T
        List<int[][]> tPiece = new ArrayList<>();
        tPiece.add(new int[][]{{0,1,0}, {1,1,1}});
        tPiece.add(new int[][]{{1,0}, {1,1}, {1,0}});
        tPiece.add(new int[][]{{1,1,1}, {0,1,0}});
        tPiece.add(new int[][]{{0,1}, {1,1}, {0,1}});
        pieceDefinitions.add(tPiece);
        // S
        List<int[][]> sPiece = new ArrayList<>();
        sPiece.add(new int[][]{{0,2,2}, {2,2,0}});
        sPiece.add(new int[][]{{2,0}, {2,2}, {0,2}});
        pieceDefinitions.add(sPiece);
        // L
        List<int[][]> lPiece = new ArrayList<>();
        lPiece.add(new int[][]{{0,0,3}, {3,3,3}});
        lPiece.add(new int[][]{{3,0}, {3,0}, {3,3}});
        lPiece.add(new int[][]{{3,3,3}, {3,0,0}});
        lPiece.add(new int[][]{{3,3}, {0,3}, {0,3}});
        pieceDefinitions.add(lPiece);
        // J
        List<int[][]> jPiece = new ArrayList<>();
        jPiece.add(new int[][]{{4,0,0}, {4,4,4}});
        jPiece.add(new int[][]{{4,4}, {4,0}, {4,0}});
        jPiece.add(new int[][]{{4,4,4}, {0,0,4}});
        jPiece.add(new int[][]{{0,4}, {0,4}, {4,4}});
        pieceDefinitions.add(jPiece);
        // I
        List<int[][]> iPiece = new ArrayList<>();
        iPiece.add(new int[][]{{5,5,5,5}});
        iPiece.add(new int[][]{{5},{5},{5},{5}});
        pieceDefinitions.add(iPiece);
        // O
        List<int[][]> oPiece = new ArrayList<>();
        oPiece.add(new int[][]{{6,6}, {6,6}});
        pieceDefinitions.add(oPiece);
        // Z
        List<int[][]> zPiece = new ArrayList<>();
        zPiece.add(new int[][]{{7,7,0}, {0,7,7}});
        zPiece.add(new int[][]{{0,7}, {7,7}, {7,0}});
        pieceDefinitions.add(zPiece);
    }

    // gets the image file paths and displays them in the proper places
    private void loadSquareBlockImages() {
        String basePath = "./res/square/";
        for (int i = 1; i <= 7; i++) {
            String imagePath = basePath + i + ".png";
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Warning: Square block image not found: " + imagePath);
                squareBlockImages[i] = null;
                continue;
            }
            try {
                squareBlockImages[i] = ImageIO.read(imageFile);
            } catch (IOException e) {
                System.err.println("Error loading square block image: " + imagePath + " - " + e.getMessage());
                squareBlockImages[i] = null;
            }
        }
    }
    

    // Spawn a new piece at the top of the game area
    private void spawnNewPiece() {
        currentPieceType = random.nextInt(7) + 1; 
        currentPieceRotation = 0;
        currentPieceShape = getPieceShape(currentPieceType -1 , currentPieceRotation); 
        currentPieceGridX = GRID_COLS / 2 - currentPieceShape[0].length / 2;
        currentPieceGridY = 0; 

        if (!canMove(currentPieceGridX, currentPieceGridY, currentPieceShape)) {
            inGameMode = false; 
            if(gameTimer != null) gameTimer.stop();
            System.out.println("GAME OVER");
            loadBackgroundImage("./res/bg/mainmenu.png"); 
            drawingPanel.repaint(); 
        }
    }

    // Gets a piece's size and rotation
    private int[][] getPieceShape(int typeIndex, int rotationIndex) {
        List<int[][]> rotations = pieceDefinitions.get(typeIndex);
        return rotations.get(rotationIndex % rotations.size());
    }


    // checks can a block move here without phasing through wall or other blocks?
    private boolean canMove(int targetX, int targetY, int[][] pieceShape) {
        for (int r = 0; r < pieceShape.length; r++) {
            for (int c = 0; c < pieceShape[r].length; c++) {
                if (pieceShape[r][c] != 0) { 
                    int actualGridX = targetX + c;
                    int actualGridY = targetY + r;

                    if (actualGridX < 0 || actualGridX >= GRID_COLS || actualGridY < 0 || actualGridY >= GRID_ROWS) {
                        return false; 
                    }
                    if (actualGridY >=0 && gameGrid[actualGridY][actualGridX] != 0) { 
                        return false; 
                    }
                }
            }
        }
        return true;
    }

    // lands the piece on the game, block cannot be moves after
    private void landPiece() {
        for (int r = 0; r < currentPieceShape.length; r++) {
            for (int c = 0; c < currentPieceShape[r].length; c++) {
                if (currentPieceShape[r][c] != 0) {
                    int actualGridX = currentPieceGridX + c;
                    int actualGridY = currentPieceGridY + r;
                    if (actualGridY >= 0 && actualGridY < GRID_ROWS && actualGridX >=0 && actualGridX < GRID_COLS) {
                        gameGrid[actualGridY][actualGridX] = currentPieceType;
                    }
                }
            }
        }
        clearLines();
    }
    

    // clear lines when row is filled, move everything else down
    private void clearLines() {
        for (int r = GRID_ROWS - 1; r >= 0; r--) {
            boolean lineFull = true;
            for (int c = 0; c < GRID_COLS; c++) {
                if (gameGrid[r][c] == 0) {
                    lineFull = false;
                    break;
                }
            }
            if (lineFull) {
                for (int rowToShift = r; rowToShift > 0; rowToShift--) {
                    for (int col = 0; col < GRID_COLS; col++) {
                        gameGrid[rowToShift][col] = gameGrid[rowToShift - 1][col];
                    }
                }
                for (int col = 0; col < GRID_COLS; col++) {
                    gameGrid[0][col] = 0;
                }
                r++; 
            }
        }
    }

    // move the block down by 1 square
    private void movePieceDown() {
        if (currentPieceShape == null) return; 
        if (canMove(currentPieceGridX, currentPieceGridY + 1, currentPieceShape)) {
            currentPieceGridY++;
        } else {
            landPiece();
            spawnNewPiece();
        }
    }


    public boolean isInGameMode() { return inGameMode; }
    public Image getBackgroundImage() { return backgroundImage; }
    public Image[] getOverlayImages() { return overlayImages; }
    public int getCurrentOverlayIndex() { return currentOverlayIndex; }
    public int getOverlayYPosition() { return overlayYPosition; }
    public List<Image> getBobbingImages() { return bobbingImages; }
    public int[] getBobbingOffsets() { return bobbingOffsets; }
    public GameRendererPanel getDrawingPanel() { return this.drawingPanel; }

    public int[][] getGameGrid() { return gameGrid; }
    public Image[] getSquareBlockImages() { return squareBlockImages; }
    public int[][] getCurrentPieceShape() { return currentPieceShape; }
    public int getCurrentPieceGridX() { return currentPieceGridX; }
    public int getCurrentPieceGridY() { return currentPieceGridY; }
    public int getCurrentPieceType() { return currentPieceType; }


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
            case 0: // Easy
                this.gameSpeedDelay = 1000;
                openGameScreen(); 
                break;
            case 1: // Medium
                this.gameSpeedDelay = 300;
                openGameScreen(); 
                break;
            case 2: // Hard
                this.gameSpeedDelay = 100;
                openGameScreen(); 
                break;
            case 3:
                showCreditsOverlay(); break;
            case 4:
                System.out.println("Quitting game via menu.");
                System.exit(0); break;
            default:
                System.out.println("Unknown menu selection.");
                this.gameSpeedDelay = 1000; 
                openGameScreen();
                break;
        }
    }

    public void openGameScreen() {
        inGameMode = true;
        loadBackgroundImage("./res/bg/game.png"); 
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                gameGrid[r][c] = 0;
            }
        }
        spawnNewPiece(); 
        
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        if (inGameMode) { 
            gameTimer = new Timer(this.gameSpeedDelay, ae -> { 
                if (inGameMode) { 
                    movePieceDown();
                    drawingPanel.repaint();
                }
            });
            gameTimer.start();
        }
        
        System.out.println("Switched to game mode with speed: " + this.gameSpeedDelay + "ms");
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
            if (currentPieceShape == null) return; 

            boolean needsRepaint = false;
            if (keyCode == KeyEvent.VK_LEFT) {
                if (canMove(currentPieceGridX - 1, currentPieceGridY, currentPieceShape)) {
                    currentPieceGridX--;
                    needsRepaint = true;
                }
            } else if (keyCode == KeyEvent.VK_RIGHT) {
                if (canMove(currentPieceGridX + 1, currentPieceGridY, currentPieceShape)) {
                    currentPieceGridX++;
                    needsRepaint = true;
                }
            } else if (keyCode == KeyEvent.VK_DOWN) { 
                movePieceDown(); 
                needsRepaint = true; 
            } else if (keyCode == KeyEvent.VK_UP) { 
                int nextRotationIndex = (currentPieceRotation + 1) % pieceDefinitions.get(currentPieceType - 1).size();
                int[][] nextShape = getPieceShape(currentPieceType - 1, nextRotationIndex);
                if (canMove(currentPieceGridX, currentPieceGridY, nextShape)) {
                    currentPieceRotation = nextRotationIndex;
                    currentPieceShape = nextShape;
                    needsRepaint = true;
                }
            } else if (keyCode == KeyEvent.VK_SPACE) { 
                 while(canMove(currentPieceGridX, currentPieceGridY + 1, currentPieceShape)) {
                    currentPieceGridY++;
                }
                landPiece();
                spawnNewPiece(); 
                needsRepaint = true;
            }


            if (keyCode == KeyEvent.VK_ESCAPE) {
                inGameMode = false;
                if (gameTimer != null) gameTimer.stop();
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
                needsRepaint = true; 
            }
            
            if (needsRepaint) {
                drawingPanel.repaint();
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
