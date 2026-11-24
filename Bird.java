
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
// import java.io.InputStream;
// import java.io.IOException;
// import javax.imageio.ImageIO;

public class Bird {

    private int x;
    private double y;
    private int width;
    private int height;

    private double velocity = 0;
    private double jumpStrength = 8.2;
    private double gravity = 0.42;
    private double maxFallVelocity = 11.0;

    private BufferedImage birdImage;

    public Bird(int x, double y, int width, int height) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        System.out.println(
                new File("assets/angry-bird.png").getAbsolutePath());
        File f = new File("angry-bird.png");

        try {
            if (f.exists()) {
                birdImage = ImageIO.read(f);
                System.out.println("Bird image loaded successfully!");
            } else {
                throw new IOException("Image not found: " + f.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getY() {
        return (int) y;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getVelocity() {
        return velocity;
    }

    public void reset() {
        y = 250;
        velocity = 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, (int) y, width, height);
    }

    // Normal drawing
    void draw(Graphics g) {
        draw(g, 0, 0);
    }

    // Drawing with rotation and vertical offset
    void draw(Graphics g, double angle, double yOffset) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double drawY = y + yOffset;

        AffineTransform old = g2.getTransform();

        g2.rotate(
                Math.toRadians(angle),
                x + width / 2.0,
                drawY + height / 2.0);

        if (birdImage != null) {

            g2.drawImage(
                    birdImage,
                    x,
                    (int) drawY,
                    width,
                    height,
                    null);

        } else {

            // Fallback if image cannot load
            g2.setColor(Color.RED);
            g2.fillOval(x, (int) drawY, width, height);
        }

        g2.setTransform(old);
        g2.dispose();
    }

    void update() {

        velocity = Math.min(
                velocity + gravity,
                maxFallVelocity);

        y += velocity;
    }

    void jump() {
        velocity = -jumpStrength;
    }
}
