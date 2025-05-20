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

class GameRendererPanel extends JPanel {
    private ImagePanel imagePanel;
    public static final int BLOCK_SIZE = 33; 
    public static final int GAME_AREA_X_OFFSET = ((720 - (ImagePanel.GRID_COLS * BLOCK_SIZE)) / 2) - 168; 
    public static final int GAME_AREA_Y_OFFSET = ((720 - (ImagePanel.GRID_ROWS * BLOCK_SIZE)) / 2) -17 ;  


    public GameRendererPanel(ImagePanel imagePanel) {
        super();
        this.imagePanel = imagePanel;
        this.setLayout(null);
        this.setFocusable(true);
        this.setBackground(java.awt.Color.DARK_GRAY); 
    }

    private java.awt.Color getColorForType(int type) {
        switch (type) {
            case 1: return java.awt.Color.decode("#00FFFF"); 
            case 2: return java.awt.Color.GREEN;   
            case 3: return java.awt.Color.ORANGE;  
            case 4: return java.awt.Color.BLUE;    
            case 5: return java.awt.Color.RED;     
            case 6: return java.awt.Color.YELLOW;  
            case 7: return java.awt.Color.MAGENTA; 
            default: return java.awt.Color.LIGHT_GRAY;
        }
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

            g2d.setColor(java.awt.Color.GRAY);
            g2d.drawRect(GAME_AREA_X_OFFSET -1, GAME_AREA_Y_OFFSET -1,
                         ImagePanel.GRID_COLS * BLOCK_SIZE +1, ImagePanel.GRID_ROWS * BLOCK_SIZE +1);

            int[][] gameGrid = imagePanel.getGameGrid();
            Image[] squareBlockImages = imagePanel.getSquareBlockImages();
            if (gameGrid != null && squareBlockImages != null) {
                for (int r = 0; r < ImagePanel.GRID_ROWS; r++) {
                    for (int c = 0; c < ImagePanel.GRID_COLS; c++) {
                        if (gameGrid[r][c] != 0) {
                            int blockType = gameGrid[r][c];
                            if (blockType > 0 && blockType < squareBlockImages.length) {
                                if (squareBlockImages[blockType] != null) {
                                    g2d.drawImage(squareBlockImages[blockType],
                                            GAME_AREA_X_OFFSET + c * BLOCK_SIZE,
                                            GAME_AREA_Y_OFFSET + r * BLOCK_SIZE,
                                            BLOCK_SIZE, BLOCK_SIZE, this);
                                } else {
                                    g2d.setColor(getColorForType(blockType));
                                    g2d.fillRect(GAME_AREA_X_OFFSET + c * BLOCK_SIZE,
                                                 GAME_AREA_Y_OFFSET + r * BLOCK_SIZE,
                                                 BLOCK_SIZE, BLOCK_SIZE);
                                }
                            }
                        }
                    }
                }
            }

            int[][] currentPieceShape = imagePanel.getCurrentPieceShape();
            if (currentPieceShape != null && squareBlockImages != null) {
                int currentPieceGridX = imagePanel.getCurrentPieceGridX();
                int currentPieceGridY = imagePanel.getCurrentPieceGridY();
                int currentPieceType = imagePanel.getCurrentPieceType();

                if (currentPieceType > 0 && currentPieceType < squareBlockImages.length) {
                    Image blockImageToDraw = squareBlockImages[currentPieceType];
                    for (int r = 0; r < currentPieceShape.length; r++) {
                        for (int c = 0; c < currentPieceShape[r].length; c++) {
                            if (currentPieceShape[r][c] != 0) {
                                if (blockImageToDraw != null) {
                                    g2d.drawImage(blockImageToDraw,
                                            GAME_AREA_X_OFFSET + (currentPieceGridX + c) * BLOCK_SIZE,
                                            GAME_AREA_Y_OFFSET + (currentPieceGridY + r) * BLOCK_SIZE,
                                            BLOCK_SIZE, BLOCK_SIZE, this);
                                } else {
                                    g2d.setColor(getColorForType(currentPieceType));
                                    g2d.fillRect(GAME_AREA_X_OFFSET + (currentPieceGridX + c) * BLOCK_SIZE,
                                                 GAME_AREA_Y_OFFSET + (currentPieceGridY + r) * BLOCK_SIZE,
                                                 BLOCK_SIZE, BLOCK_SIZE);
                                }
                            }
                        }
                    }
                }
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

        JFrame frame = new JFrame("Tetris Game Example");
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
    private int gameSpeedDelay = 1000; // Default to 1 second (Easy)

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

    private void initializePieceDefinitions() {
        pieceDefinitions = new ArrayList<>();
        
        List<int[][]> tPiece = new ArrayList<>();
        tPiece.add(new int[][]{{0,1,0}, {1,1,1}});
        tPiece.add(new int[][]{{1,0}, {1,1}, {1,0}});
        tPiece.add(new int[][]{{1,1,1}, {0,1,0}});
        tPiece.add(new int[][]{{0,1}, {1,1}, {0,1}});
        pieceDefinitions.add(tPiece);

        List<int[][]> sPiece = new ArrayList<>();
        sPiece.add(new int[][]{{0,2,2}, {2,2,0}});
        sPiece.add(new int[][]{{2,0}, {2,2}, {0,2}});
        pieceDefinitions.add(sPiece);

        List<int[][]> lPiece = new ArrayList<>();
        lPiece.add(new int[][]{{0,0,3}, {3,3,3}});
        lPiece.add(new int[][]{{3,0}, {3,0}, {3,3}});
        lPiece.add(new int[][]{{3,3,3}, {3,0,0}});
        lPiece.add(new int[][]{{3,3}, {0,3}, {0,3}});
        pieceDefinitions.add(lPiece);

        List<int[][]> jPiece = new ArrayList<>();
        jPiece.add(new int[][]{{4,0,0}, {4,4,4}});
        jPiece.add(new int[][]{{4,4}, {4,0}, {4,0}});
        jPiece.add(new int[][]{{4,4,4}, {0,0,4}});
        jPiece.add(new int[][]{{0,4}, {0,4}, {4,4}});
        pieceDefinitions.add(jPiece);

        List<int[][]> iPiece = new ArrayList<>();
        iPiece.add(new int[][]{{5,5,5,5}});
        iPiece.add(new int[][]{{5},{5},{5},{5}});
        pieceDefinitions.add(iPiece);

        List<int[][]> oPiece = new ArrayList<>();
        oPiece.add(new int[][]{{6,6}, {6,6}});
        pieceDefinitions.add(oPiece);

        List<int[][]> zPiece = new ArrayList<>();
        zPiece.add(new int[][]{{7,7,0}, {0,7,7}});
        zPiece.add(new int[][]{{0,7}, {7,7}, {7,0}});
        pieceDefinitions.add(zPiece);
    }

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

    private int[][] getPieceShape(int typeIndex, int rotationIndex) {
        List<int[][]> rotations = pieceDefinitions.get(typeIndex);
        return rotations.get(rotationIndex % rotations.size());
    }

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
                this.gameSpeedDelay = 1000; // Default to easy if somehow an unknown selection
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
            gameTimer = new Timer(this.gameSpeedDelay, ae -> { // Use the stored gameSpeedDelay
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
