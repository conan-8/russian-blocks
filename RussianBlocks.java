import javax.swing.JFrame;

public class RussianBlocks {
    public static void main(String[] args) {
        // get the file paths for each png
        String backgroundPath = "./res/bg/mainmenu.png";
        


        // Jframe setups
        JFrame frame = new JFrame("Tetris");
        ImagePanel logicController = new ImagePanel(backgroundPath);

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

