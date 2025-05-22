import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import javax.swing.JPanel;

class GameRendererPanel extends JPanel {
    private ImagePanel imagePanel;
    public static final int BLOCK_SIZE = 33; 
    public static final int GAME_AREA_X_OFFSET = ((720 - (ImagePanel.GRID_COLS * BLOCK_SIZE)) / 2) - 178; 
    public static final int GAME_AREA_Y_OFFSET = ((720 - (ImagePanel.GRID_ROWS * BLOCK_SIZE)) / 2) -14 ;  


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
