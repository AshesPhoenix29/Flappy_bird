
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

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

        // Load bird image from inside the JAR
        try (InputStream input =
                Bird.class.getResourceAsStream("/angry-bird.png")) {

            if (input == null) {
                throw new IOException(
                    "angry-bird.png not found inside application JAR!"
                );
            }

            birdImage = ImageIO.read(input);

            if (birdImage == null) {
                throw new IOException(
                    "Unable to decode angry-bird.png"
                );
            }

            System.out.println("Bird image loaded successfully!");

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

    void draw(Graphics g) {
        draw(g, 0, 0);
    }

    void draw(Graphics g, double angle, double yOffset) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        double drawY = y + yOffset;

        AffineTransform old = g2.getTransform();

        g2.rotate(
            Math.toRadians(angle),
            x + width / 2.0,
            drawY + height / 2.0
        );

        if (birdImage != null) {

            g2.drawImage(
                birdImage,
                x,
                (int) drawY,
                width,
                height,
                null
            );

        } else {

            g2.setColor(Color.RED);
            g2.fillOval(x, (int) drawY, width, height);
        }

        g2.setTransform(old);
        g2.dispose();
    }

    void update() {

        velocity = Math.min(
            velocity + gravity,
            maxFallVelocity
        );

        y += velocity;
    }

    void jump() {
        velocity = -jumpStrength;
    }
}
