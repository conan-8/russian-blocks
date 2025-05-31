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
import javax.swing.Timer;


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
   boolean inPauseMenu = false;
   int timerTick = 0;


   private GameRendererPanel drawingPanel;
   private int score = 0;
   private boolean spaceBarActionProcessed = false;


   public ImagePanel(String backgroundPath) {
       String[] overlayPaths = {
           "./res/options/easy.png",
           "./res/options/medium.png",
           "./res/options/hard.png",
           "./res/options/credits.png",
           "./res/options/quit.png"
       };
       String[] inGameOverlayPaths = {
           "./res/options/pause/mainmenu.png",
           "./res/options/pause/newgame.png",
           "./res/options/pause/resume.png"
       };
       this.drawingPanel = new GameRendererPanel(this);
       this.drawingPanel.addKeyListener(this);


       loadBackgroundImage(backgroundPath);
       loadOverlayImages(overlayPaths);
       loadOverlayImages(inGameOverlayPaths);
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
           System.out.println("GAME OVER - Score: " + score);
           loadBackgroundImage("./res/bg/mainmenu.png");
           currentOverlayIndex = 0;
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
       this.score += 5;
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
               this.score += 100;
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
   public int getScore() { return score; }


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
           return;
       }
       int oldLength = (overlayImages == null) ? 0 : overlayImages.length;
       Image[] newImages = new Image[oldLength + paths.length];
       if (overlayImages != null) {
           for (int i = 0; i < oldLength; i++) {
               newImages[i] = overlayImages[i];
           }
       }
       for (int i = 0; i < paths.length; i++) {
           File imageFile = new File(paths[i]);
           if (!imageFile.exists()) {
               newImages[oldLength + i] = null;
               continue;
           }
           try {
               newImages[oldLength + i] = ImageIO.read(imageFile);
           } catch (IOException e) {
               newImages[oldLength + i] = null;
           }
       }
       overlayImages = newImages;
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
           case 0:
               this.gameSpeedDelay = 1000;
               openGameScreen();
               break;
           case 1:
               this.gameSpeedDelay = 300;
               openGameScreen();
               break;
           case 2:
               this.gameSpeedDelay = 100;
               openGameScreen();
               break;
           case 3:
               showCreditsOverlay(); break;
           case 4:
               System.exit(0); break;
           default:
               this.gameSpeedDelay = 1000;
               openGameScreen();
               break;
       }
   }


   public void openGameScreen() {
       inGameMode = true;
       this.score = 0;
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
               if (inGameMode && !inPauseMenu) {
                   movePieceDown();
                   drawingPanel.repaint();
               }
           });
           gameTimer.start();
       }
       drawingPanel.repaint();
       drawingPanel.requestFocusInWindow();
   }


   private void showCreditsOverlay() {
       JFrame creditsFrame = new JFrame("Credits");
       ImagePanel creditsLogic = new ImagePanel("./res/credits.png");
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
       boolean needsRepaint = false;


       if (inGameMode) {
           if (inPauseMenu) {
               int pauseStart = 5, pauseEnd = 8;
               if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                   currentOverlayIndex--;
                   if (currentOverlayIndex < pauseStart) currentOverlayIndex = pauseEnd - 1;
                   needsRepaint = true;
               } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                   currentOverlayIndex++;
                   if (currentOverlayIndex >= pauseEnd) currentOverlayIndex = pauseStart;
                   needsRepaint = true;
               } else if (keyCode == KeyEvent.VK_ENTER) {
                   switch (currentOverlayIndex) {
                       case 5:
                           inPauseMenu = false;
                           inGameMode = false;
                           if (gameTimer != null) gameTimer.stop();
                           loadBackgroundImage("./res/bg/mainmenu.png");
                           currentOverlayIndex = 0;
                           needsRepaint = true;
                           break;
                       case 6:
                           inPauseMenu = false;
                           openGameScreen();
                           needsRepaint = true;
                           break;
                       case 7:
                           inPauseMenu = false;
                           if (gameTimer != null && !gameTimer.isRunning()) gameTimer.start();
                           needsRepaint = true;
                           break;
                   }
               } else if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE) {
                   inPauseMenu = false;
                   if (gameTimer != null && !gameTimer.isRunning()) gameTimer.start();
                   needsRepaint = true;
               }
           } else {
               if (currentPieceShape == null) return;
               if (gameTimer != null && gameTimer.isRunning()) {
                   if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
                       if (canMove(currentPieceGridX - 1, currentPieceGridY, currentPieceShape)) {
                           currentPieceGridX--;
                           needsRepaint = true;
                       }
                   } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
                       if (canMove(currentPieceGridX + 1, currentPieceGridY, currentPieceShape)) {
                           currentPieceGridX++;
                           needsRepaint = true;
                       }
                   } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                       movePieceDown();
                       needsRepaint = true;
                   } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                       int nextRotationIndex = (currentPieceRotation + 1) % pieceDefinitions.get(currentPieceType - 1).size();
                       int[][] nextShape = getPieceShape(currentPieceType - 1, nextRotationIndex);
                       if (canMove(currentPieceGridX, currentPieceGridY, nextShape)) {
                           currentPieceRotation = nextRotationIndex;
                           currentPieceShape = nextShape;
                           needsRepaint = true;
                       }
                   } else if (keyCode == KeyEvent.VK_SPACE) {
                       if (!spaceBarActionProcessed) {
                           while(canMove(currentPieceGridX, currentPieceGridY + 1, currentPieceShape)) {
                               currentPieceGridY++;
                           }
                           landPiece();
                           spawnNewPiece();
                           needsRepaint = true;
                           spaceBarActionProcessed = true;
                       }
                   } else if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE) {
                       if (gameTimer != null) gameTimer.stop();
                       inPauseMenu = true;
                       currentOverlayIndex = 7;
                       needsRepaint = true;
                   }
               }
           }
       } else {
           if (overlayImages == null || overlayImages.length == 0) return;
           int numMainMenuOverlays = 5;
           if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
               int initialIndex = currentOverlayIndex;
               do {
                   currentOverlayIndex = (currentOverlayIndex + 1) % numMainMenuOverlays;
               } while (overlayImages[currentOverlayIndex] == null && currentOverlayIndex != initialIndex);
               needsRepaint = true;
           } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
                int initialIndex = currentOverlayIndex;
                do {
                   currentOverlayIndex = (currentOverlayIndex - 1 + numMainMenuOverlays) % numMainMenuOverlays;
                } while (overlayImages[currentOverlayIndex] == null && currentOverlayIndex != initialIndex);
               needsRepaint = true;
           } else if (keyCode == KeyEvent.VK_ENTER) {
               if (currentOverlayIndex != -1 && currentOverlayIndex < numMainMenuOverlays && overlayImages[currentOverlayIndex] != null) {
                   handleSelection();
               }
           }
       }


       if (needsRepaint) {
           drawingPanel.repaint();
       }
   }


   @Override public void keyReleased(KeyEvent e) {
       int keyCode = e.getKeyCode();
       if (keyCode == KeyEvent.VK_SPACE) {
           spaceBarActionProcessed = false;
       }
   }
}





