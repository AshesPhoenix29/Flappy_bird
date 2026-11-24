import javax.swing.*;

public class Main {
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setTitle("Flappy Bird");
        frame.setSize(360,640);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Game game = new Game();
        frame.add(game);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
