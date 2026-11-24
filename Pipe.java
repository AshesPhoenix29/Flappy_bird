import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;


public class Pipe {
    private int Pipe_Width = 60;
    private int Gap_Height = 150;
    private int x;
    private double preciseX;
    private int gapY;
    private boolean scored = false;
    private final Rectangle birdRect = new Rectangle();
    private final Rectangle upperPipe = new Rectangle();
    private final Rectangle lowerPipe = new Rectangle();

    Pipe(int x, int gapY) {
        this.x = x;
        this.preciseX = x;
        this.gapY = gapY;
    }

    // Random random = new Random();
    // int gapY = random.nextInt(250) + 100;
    // public int getHeight(){
    //     return Gap_Height;
    // }

    // public int getWidth(){
    //     return Pipe_Width;
    // }

    public void update(double speed){
        preciseX -= speed;
        x = (int) Math.floor(preciseX);
    }

    // public int getX(){
    //     return x;
    // }

    public boolean shouldScore(int birdX){
        if(!scored && x + Pipe_Width / 2 < birdX){
            scored = true;
            return true;
        }
        return false;
    }

    public boolean collidesWith(Bird bird,int screenHeight){
        birdRect.setBounds(bird.getX() + 3, bird.getY() + 3,
                Math.max(0, bird.getWidth() - 6), Math.max(0, bird.getHeight() - 6));

        upperPipe.setBounds(x, 0, Pipe_Width, gapY);

        lowerPipe.setBounds(x, gapY + Gap_Height, Pipe_Width,
                screenHeight - (gapY + Gap_Height));

        return birdRect.intersects(upperPipe) || birdRect.intersects(lowerPipe);
    }

    void draw(Graphics g,int screenHeight){
        Graphics2D g2 = (Graphics2D) g.create();
        int lowerY = gapY + Gap_Height;
        int lowerHeight = Math.max(0, screenHeight - lowerY);

        g2.setPaint(new GradientPaint(x, 0, new Color(124, 225, 49),
                x + Pipe_Width, 0, new Color(57, 153, 30)));
        g2.fillRect(x, 0, Pipe_Width, gapY);
        g2.fillRect(x, lowerY, Pipe_Width, lowerHeight);

        g2.setColor(new Color(177, 245, 82));
        g2.fillRect(x + 8, 0, 8, gapY);
        g2.fillRect(x + 8, lowerY, 8, lowerHeight);
        g2.setColor(new Color(43, 112, 28));
        g2.fillRect(x + Pipe_Width - 10, 0, 7, gapY);
        g2.fillRect(x + Pipe_Width - 10, lowerY, 7, lowerHeight);

        g2.setColor(new Color(15, 25, 16));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRect(x, 0, Pipe_Width, gapY);
        g2.drawRect(x, lowerY, Pipe_Width, lowerHeight);
        drawCap(g2, gapY - 17);
        drawCap(g2, lowerY - 15);
        g2.dispose();
    } 

    private void drawCap(Graphics2D g, int y){
        RoundRectangle2D cap = new RoundRectangle2D.Double(
                x - 5, y, Pipe_Width + 10, 34, 3, 3);
        g.setPaint(new GradientPaint(x - 5, y, new Color(140, 235, 58),
                x + Pipe_Width + 5, y, new Color(70, 177, 31)));
        g.fill(cap);
        g.setColor(new Color(15, 25, 16));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(cap);
        g.setColor(new Color(177, 245, 82));
        g.fillRect(x + 3, y + 3, 8, 28);
    }

    boolean isOffScreen(){
        return x + Pipe_Width < 0;
    }
}
