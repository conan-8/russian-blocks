import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO; // Import for ImageIO
import javax.swing.JPanel;




class GameRendererPanel extends JPanel {
  private ImagePanel imagePanel;
  public static final int BLOCK_SIZE = 33;
  public static final int GAME_AREA_X_OFFSET = ((720 - (ImagePanel.GRID_COLS * BLOCK_SIZE)) / 2) - 178;
  public static final int GAME_AREA_Y_OFFSET = ((720 - (ImagePanel.GRID_ROWS * BLOCK_SIZE)) / 2) - 14;
  private Font scoreFont;
  // <<< NEW FIELD FOR END SCREEN IMAGE >>>
  private Image endScreenImage;




  public GameRendererPanel(ImagePanel imagePanel) {
      super();
      this.imagePanel = imagePanel;
      this.setLayout(null);
      this.setFocusable(true);
      this.setBackground(java.awt.Color.DARK_GRAY);




      try {
          scoreFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/PressStart2P-Regular.ttf")).deriveFont(28f);
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          ge.registerFont(scoreFont);
      } catch (FontFormatException | IOException e) {
          e.printStackTrace();
          scoreFont = new Font("SansSerif", Font.BOLD, 20);
      }
      // <<< LOAD END SCREEN IMAGE >>>
      loadEndScreenImage();
  }


  // <<< NEW METHOD TO LOAD END SCREEN IMAGE >>>
  private void loadEndScreenImage() {
      try {
          endScreenImage = ImageIO.read(new File("./res/bg/endscreen.png"));
      } catch (IOException e) {
          System.err.println("Error loading end screen image: ./res/bg/endscreen.png - " + e.getMessage());
          endScreenImage = null; // Set to null if loading fails
      }
  }




  private java.awt.Color getColorForType(int type) {
      switch (type) {
          case 1: return java.awt.Color.decode("#00FFFF");
          case 2: return java.awt.Color.decode("#00FF00");
          case 3: return java.awt.Color.decode("#FFA500");
          case 4: return java.awt.Color.decode("#0000FF");
          case 5: return java.awt.Color.decode("#FF0000");
          case 6: return java.awt.Color.decode("#FFFF00");
          case 7: return java.awt.Color.decode("#FF00FF");
          default: return java.awt.Color.LIGHT_GRAY;
      }
  }




  @Override
  protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g.create();


      // <<< UPDATED PAINT LOGIC FOR GAME OVER SCREEN >>>
      if (imagePanel.isInGameOverScreen()) {
          // 1. Draw the underlying game state (background and final grid)
          if (this.imagePanel.getBackgroundImage() != null) { // Should be game.png
              g2d.drawImage(this.imagePanel.getBackgroundImage(), 0, 0, this.getWidth(), this.getHeight(), this);
          } else {
              g2d.setColor(java.awt.Color.BLACK);
              g2d.fillRect(0, 0, getWidth(), getHeight());
          }


          g2d.setColor(java.awt.Color.GRAY); // Border for game area
          g2d.drawRect(GAME_AREA_X_OFFSET - 1, GAME_AREA_Y_OFFSET - 1,
                       ImagePanel.GRID_COLS * BLOCK_SIZE + 1, ImagePanel.GRID_ROWS * BLOCK_SIZE + 1);


          int[][] gameGrid = imagePanel.getGameGrid();
          Image[] squareBlockImages = imagePanel.getSquareBlockImages();
          if (gameGrid != null && squareBlockImages != null) {
              for (int r = 0; r < ImagePanel.GRID_ROWS; r++) {
                  for (int c = 0; c < ImagePanel.GRID_COLS; c++) {
                      if (gameGrid[r][c] != 0) {
                          int blockType = gameGrid[r][c];
                          if (blockType > 0 && blockType < squareBlockImages.length && squareBlockImages[blockType] != null) {
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


          // 2. Overlay the end screen image
          if (endScreenImage != null) {
              g2d.drawImage(endScreenImage, 0, 0, this.getWidth(), this.getHeight(), this);
          } else { // Fallback if endscreen.png didn't load
              g2d.setColor(new java.awt.Color(0, 0, 0, 200)); // Semi-transparent dark overlay
              g2d.fillRect(0, 0, getWidth(), getHeight());
              if (scoreFont != null) { // Draw "Game Over" text if image fails
                   g2d.setColor(java.awt.Color.RED);
                   g2d.setFont(scoreFont.deriveFont(60f));
                   String goText = "GAME OVER";
                   FontMetrics goMetrics = g2d.getFontMetrics();
                   g2d.drawString(goText, (getWidth() - goMetrics.stringWidth(goText)) / 2, getHeight() / 3);
              }
          }


          // 3. Draw the final score in the middle of the screen
          if (scoreFont != null) {
              g2d.setColor(java.awt.Color.WHITE); // Adjust color if needed for visibility on endscreen.png
              Font finalScoreFont = scoreFont.deriveFont(50f); // Same size as regular score value
              g2d.setFont(finalScoreFont);
              String scoreText = String.valueOf(imagePanel.getScore());
              FontMetrics metrics = g2d.getFontMetrics(finalScoreFont);
              int scoreWidth = metrics.stringWidth(scoreText);
              // Center the score text
              int scoreX = (getWidth() - scoreWidth) / 2;
              int scoreY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
              g2d.drawString(scoreText, scoreX, scoreY);
          }


      } else if (this.imagePanel.isInGameMode() && !this.imagePanel.inPauseMenu) { // Active Gameplay
          // Draw background image or fill
          if (this.imagePanel.getBackgroundImage() != null) {
              g2d.drawImage(this.imagePanel.getBackgroundImage(), 0, 0, this.getWidth(), this.getHeight(), this);
          } else {
              g2d.setColor(java.awt.Color.BLACK);
              g2d.fillRect(0, 0, getWidth(), getHeight());
          }


          // Draw score (regular position)
          if (scoreFont != null) {
              g2d.setColor(java.awt.Color.WHITE);
              Font scoreLabelFont = scoreFont.deriveFont(24f);
              Font scoreValueFont = scoreFont.deriveFont(50f);
              String scoreLabelText = "SCORE";
              g2d.setFont(scoreLabelFont);
              FontMetrics labelMetrics = g2d.getFontMetrics();
              int labelWidth = labelMetrics.stringWidth(scoreLabelText);
              int scoreLabelX = 480;
              int scoreLabelY = 400;
              g2d.drawString(scoreLabelText, scoreLabelX, scoreLabelY);
              String scoreValueText = String.valueOf(imagePanel.getScore());
              g2d.setFont(scoreValueFont);
              FontMetrics valueMetrics = g2d.getFontMetrics();
              int valueWidth = valueMetrics.stringWidth(scoreValueText);
              int valueHeight = valueMetrics.getHeight();
              int scoreValueX = scoreLabelX + (labelWidth / 2) - (valueWidth / 2);
              int scoreValueY = scoreLabelY + valueHeight;
              g2d.drawString(scoreValueText, scoreValueX, scoreValueY);
          }


          // Draw game area border
          g2d.setColor(java.awt.Color.GRAY);
          g2d.drawRect(GAME_AREA_X_OFFSET -1, GAME_AREA_Y_OFFSET -1,
                       ImagePanel.GRID_COLS * BLOCK_SIZE +1, ImagePanel.GRID_ROWS * BLOCK_SIZE +1);


          // Draw landed blocks on the grid
          int[][] gameGrid = imagePanel.getGameGrid();
          Image[] squareBlockImages = imagePanel.getSquareBlockImages();
          if (gameGrid != null && squareBlockImages != null) {
              for (int r = 0; r < ImagePanel.GRID_ROWS; r++) {
                  for (int c = 0; c < ImagePanel.GRID_COLS; c++) {
                      if (gameGrid[r][c] != 0) {
                          int blockType = gameGrid[r][c];
                          if (blockType > 0 && blockType < squareBlockImages.length && squareBlockImages[blockType] != null) {
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


          // Draw current falling piece
          int[][] currentPieceShape = imagePanel.getCurrentPieceShape();
          if (currentPieceShape != null && squareBlockImages != null) {
              int currentPieceGridX = imagePanel.getCurrentPieceGridX();
              int currentPieceGridY = imagePanel.getCurrentPieceGridY();
              int currentPieceType = imagePanel.getCurrentPieceType();
              Image blockImageToDraw = (currentPieceType > 0 && currentPieceType < squareBlockImages.length) ?
                                       squareBlockImages[currentPieceType] : null;
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


           // Draw the "Next Block" display
           int[][] nextPieceShape = imagePanel.getNextPieceShape();
           int nextPieceType = imagePanel.getNextPieceType();
           if (nextPieceShape != null && nextPieceType > 0) {
               int nextPieceAreaCenterX = scoreFont != null ? 530 : getWidth() - 80;
               int nextPieceAreaCenterY = scoreFont != null ? (250 - 85) : (100 - 85);
               int pieceRows = nextPieceShape.length;
               int pieceCols = 0;
               if (pieceRows > 0) {
                   for(int[] row : nextPieceShape) {
                       if (row.length > pieceCols) pieceCols = row.length;
                   }
               }
               int nextPiecePixelWidth = pieceCols * BLOCK_SIZE;
               int nextPiecePixelHeight = pieceRows * BLOCK_SIZE;
               int nextPieceDrawX = nextPieceAreaCenterX - (nextPiecePixelWidth / 2);
               int nextPieceDrawY = nextPieceAreaCenterY - (nextPiecePixelHeight / 2);
               Image nextBlockImage = (nextPieceType > 0 && nextPieceType < squareBlockImages.length) ?
                                       squareBlockImages[nextPieceType] : null;
               for (int r = 0; r < nextPieceShape.length; r++) {
                   for (int c = 0; c < nextPieceShape[r].length; c++) {
                       if (nextPieceShape[r][c] != 0) {
                           if (nextBlockImage != null) {
                               g2d.drawImage(nextBlockImage,
                                       nextPieceDrawX + c * BLOCK_SIZE,
                                       nextPieceDrawY + r * BLOCK_SIZE,
                                       BLOCK_SIZE, BLOCK_SIZE, this);
                           } else {
                               g2d.setColor(getColorForType(nextPieceType));
                               g2d.fillRect(nextPieceDrawX + c * BLOCK_SIZE,
                                            nextPieceDrawY + r * BLOCK_SIZE,
                                            BLOCK_SIZE, BLOCK_SIZE);
                           }
                       }
                   }
               }
           }


      } else { // Main Menu or Pause Menu
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
              int overlayHeight = currentOverlay.getHeight(this);
              int panelWidth = this.getWidth();
              int x = (panelWidth - overlayWidth) / 2;
              int y;


              if (this.imagePanel.inPauseMenu && currentOverlayIndex >= 5 && currentOverlayIndex <= 7) {
                   int pauseMenuItemBaseY = (this.getHeight() - (overlayHeight * 3 + 20 * 2)) / 2;
                   y = pauseMenuItemBaseY + (currentOverlayIndex - 5) * (overlayHeight + 20);
              } else if (currentOverlayIndex >= 5 && currentOverlayIndex <=7 && !this.imagePanel.inPauseMenu) {
                   // This case might occur if currentOverlayIndex is a pause menu index but we're not in pause mode
                   // (e.g. after exiting pause to main menu, then game over, then main menu again)
                   // Default to main menu Y if it's a pause index but not in pause mode. Or handle specifically.
                   // For safety, use the main menu's default Y.
                   y = this.imagePanel.getOverlayYPosition();
              } else {
                  y = this.imagePanel.getOverlayYPosition();
              }
              g2d.drawImage(currentOverlay, x, y, overlayWidth, overlayHeight, this);


              if (!this.imagePanel.inPauseMenu && !this.imagePanel.isInGameOverScreen() && currentOverlayIndex >= 0 && currentOverlayIndex <= 4) {
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
          }
      }
      g2d.dispose();
  }
}



