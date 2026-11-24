import javax.swing.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;
import java.util.Random;

public class Game extends JPanel {
    Random random = new Random();
    private GameState state = GameState.START;
    public int score = 0;
    // int gapY = random.nextInt(250) + 100;
    private int pipeTimer = 0;
    private int nextPipeInterval = 100;

    private Bird b = new Bird(150, 250, 45, 45);

    private ArrayList<Pipe> pipes = new ArrayList<>();
    private final Preferences preferences = Preferences.userNodeForPackage(Game.class);
    private int bestScore;
    private boolean newBest;
    private long frameCount;
    private int groundOffset;
    private double groundScroll;
    private boolean paused;
    private final Rectangle pauseButtonBounds = new Rectangle();
    private final Rectangle playButtonBounds = new Rectangle();

    public Game() {
        bestScore = preferences.getInt("bestScore", 0);
        pipes.add(new Pipe(360, 180));

        pipes.removeIf(Pipe::isOffScreen);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "jump");

        getActionMap().put("jump", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

                if (state == GameState.START) {
                    state = GameState.PLAYING;
                    b.jump();
                } else if (state == GameState.PLAYING) {
                    if (paused) {
                        paused = false;
                    } else {
                        b.jump();
                    }
                }
            }
        });
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("R"), "restart");

        getActionMap().put("restart", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (state == GameState.GAME_OVER) {
                    restartGame();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == GameState.PLAYING && pauseButtonBounds.contains(e.getPoint())) {
                    paused = !paused;
                } else if (state == GameState.START) {
                    state = GameState.PLAYING;
                    b.jump();
                } else if (state == GameState.PLAYING && !paused) {
                    b.jump();
                } else if (state == GameState.GAME_OVER && playButtonBounds.contains(e.getPoint())) {
                    restartGame();
                }
            }
        });
        setBackground(Color.CYAN);
        Timer timer = new Timer(16, e -> {
            if (!paused) {
                frameCount++;
            }

            if (state == GameState.PLAYING && !paused) {

                b.update();

                double pipeSpeed = 2.8;
                groundScroll = (groundScroll + pipeSpeed) % 28;
                groundOffset = (int) groundScroll;

                for (Pipe p : pipes) {
                    p.update(pipeSpeed);

                    if (p.shouldScore(b.getX() + b.getWidth() / 2)) {
                        score++;
                    }
                }

                // Collision detection
                for (Pipe p : pipes) {
                    if (p.collidesWith(b, getHeight() - 80)) {
                        state = GameState.GAME_OVER;
                        break;
                    }
                }

                if (b.getY() + b.getHeight() >= getHeight() - 80) {
                    state = GameState.GAME_OVER;
                }

                if (state == GameState.GAME_OVER) {
                    newBest = score > bestScore;
                    if (newBest) {
                        bestScore = score;
                        preferences.putInt("bestScore", bestScore);
                    }
                }

                // Pipe spawning
                pipeTimer++;

                if (pipeTimer >= nextPipeInterval) {
                    int playfieldHeight = getHeight() - 80;
                    int minGapY = 60;
                    int maxGapY = playfieldHeight - 150 - 60;
                    int gapY = random.nextInt(maxGapY - minGapY + 1) + minGapY;
                    pipes.add(new Pipe(getWidth(), gapY));
                    pipeTimer = 0;
                    nextPipeInterval = 100;
                }

                pipes.removeIf(Pipe::isOffScreen);
            }

            repaint();
        });

        timer.start();
    }

    private void restartGame() {
    state = GameState.START;
    paused = false;
    score = 0;
    pipeTimer = 0;
    nextPipeInterval = 100;
    newBest = false;
    groundOffset = 0;
    groundScroll = 0;

    b.reset();

    pipes.clear();
    pipes.add(new Pipe(360, 180));

    repaint();
}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawSky(g2);
        for (Pipe p : pipes) {
            p.draw(g2, getHeight() - 80);
        }
        drawGround(g2);

        if (state == GameState.START) {
            drawOverlay(g2);
            b.draw(g2, 0, Math.sin(frameCount * 0.08) * 8);
        } else if (state == GameState.PLAYING) {
            double angle = Math.max(-25, Math.min(70, b.getVelocity() * 9));
            b.draw(g2, angle, 0);
            drawScore(g2);
            drawPauseButton(g2);
            if (paused) {
                drawPauseOverlay(g2);
            }
        } else {
            b.draw(g2, 0, 0);
            drawOverlay(g2);
        }

        g2.dispose();
    }

    private void drawSky(Graphics2D g) {
        int groundY = getHeight() - 80;
        Color skyTop;
        Color skyBottom;
        if (score >= 25) {
            skyTop = new Color(20, 25, 54);
            skyBottom = new Color(29, 42, 74);
        } else if (score >= 10) {
            skyTop = new Color(226, 123, 88);
            skyBottom = new Color(255, 184, 140);
        } else {
            skyTop = new Color(78, 192, 202);
            skyBottom = new Color(169, 232, 237);
        }
        if (score < 10) {
            g.setColor(skyTop);
            g.fillRect(0, 0, getWidth(), groundY);
        } else {
            g.setPaint(new GradientPaint(0, 0, skyTop, 0, groundY, skyBottom));
            g.fillRect(0, 0, getWidth(), groundY);
        }

        if (score >= 25) {
            for (int i = 0; i < 28; i++) {
                int starX = (i * 67 + 23) % Math.max(1, getWidth());
                int starY = (i * 43 + 31) % Math.max(1, groundY - 30);
                int starSize = (i % 5 == 0) ? 3 : 2;
                int alpha = 150 + (int) (Math.sin(frameCount * 0.08 + i) * 70);
                g.setColor(new Color(255, 255, 220, Math.max(70, Math.min(220, alpha))));
                g.fillOval(starX, starY, starSize, starSize);
            }
            g.setColor(new Color(255, 247, 205, 35));
            g.fillOval(getWidth() - 79, 45, 54, 54);
            g.setColor(new Color(255, 247, 205));
            g.fillOval(getWidth() - 72, 39, 43, 43);
        } else {
            int sunY = score >= 10 ? groundY / 2 : 88;
            g.setColor(new Color(255, 237, 167, 190));
            g.fillOval(getWidth() - 76, sunY, 42, 42);
            Color cloudColor = score >= 10
                    ? new Color(255, 204, 165, 135)
                    : new Color(255, 255, 255, 130);
            drawCloud(g, (int) ((getWidth() * 0.18 + frameCount * 0.25) % (getWidth() + 90)) - 65,
                    (int) (getHeight() * 0.19), 1.0, cloudColor);
            drawCloud(g, (int) ((getWidth() * 0.72 + frameCount * 0.18) % (getWidth() + 100)) - 70,
                    (int) (getHeight() * 0.36), 0.75, cloudColor);
            drawCloud(g, (int) ((getWidth() * 0.40 + frameCount * 0.12) % (getWidth() + 110)) - 80,
                    (int) (getHeight() * 0.27), 0.9, cloudColor);
            if (score >= 10) {
                g.setColor(new Color(117, 75, 82, 90));
                g.fillOval(-35, groundY - 82, getWidth() / 2, 100);
                g.fillOval(getWidth() / 2, groundY - 62, getWidth() / 2 + 40, 84);
            }
        }
        drawScenery(g, groundY);
    }

    private void drawCloud(Graphics2D g, int x, int y, double scale, Color color) {
        int width = (int) (76 * scale);
        int height = (int) (25 * scale);
        g.setColor(color);
        g.fillOval(x, y, width / 2, height);
        g.fillOval((int) (x + width * 0.22), (int) (y - height * 0.48),
                (int) (width * 0.48), (int) (height * 1.25));
        g.fillOval((int) (x + width * 0.48), (int) (y - height * 0.12),
                (int) (width * 0.52), height);
    }

    private void drawGround(Graphics2D g) {
        int groundY = getHeight() - 80;
        Color grass = score >= 25 ? new Color(54, 105, 78)
                : score >= 10 ? new Color(111, 143, 81) : new Color(112, 190, 74);
        Color dirtTop = score >= 25 ? new Color(67, 75, 89)
                : score >= 10 ? new Color(172, 119, 90) : new Color(218, 218, 147);
        Color dirtBottom = score >= 25 ? new Color(42, 49, 65)
                : score >= 10 ? new Color(119, 80, 78) : new Color(218, 218, 147);
        g.setColor(grass);
        g.fillRect(0, groundY, getWidth(), 16);
        g.setPaint(new GradientPaint(0, groundY + 16, dirtTop, 0, getHeight(), dirtBottom));
        g.fillRect(0, groundY + 16, getWidth(), 64);

        g.setColor(score >= 25 ? new Color(139, 158, 153)
                : score >= 10 ? new Color(210, 170, 127) : new Color(190, 185, 113));
        for (int x = -groundOffset; x < getWidth(); x += 12) {
            g.drawLine(x, groundY + 29, x + 5, groundY + 17);
        }
        g.setColor(score >= 25 ? new Color(37, 78, 64) : new Color(82, 157, 57));
        g.fillRect(0, groundY, getWidth(), 4);
        for (int x = -groundOffset; x < getWidth(); x += 14) {
            g.fillPolygon(new int[] {x, x + 4, x + 8},
                    new int[] {groundY + 13, groundY + 4, groundY + 13}, 3);
        }
    }

    private void drawScenery(Graphics2D g, int groundY) {
        Color hills = score >= 25 ? new Color(36, 61, 76)
                : score >= 10 ? new Color(130, 119, 102) : new Color(142, 213, 147);
        Color buildings = score >= 25 ? new Color(27, 48, 62)
                : score >= 10 ? new Color(116, 117, 101) : new Color(87, 174, 139);
        g.setColor(hills);
        g.fillOval(-35, groundY - 62, 125, 68);
        g.fillOval(35, groundY - 56, 110, 63);
        g.fillOval(120, groundY - 72, 140, 80);
        g.fillOval(255, groundY - 58, 135, 67);
        g.setColor(buildings);
        g.fillRect(18, groundY - 32, 27, 32);
        g.fillRect(58, groundY - 45, 24, 45);
        g.fillRect(101, groundY - 26, 34, 26);
        g.fillRect(159, groundY - 38, 29, 38);
        g.fillRect(205, groundY - 27, 27, 27);
        g.fillRect(253, groundY - 43, 31, 43);
        g.fillRect(300, groundY - 31, 25, 31);
    }

    private void drawScore(Graphics2D g) {
        String text = Integer.toString(score);
        g.setFont(new Font("Arial Black", Font.BOLD, 50));
        g.setColor(Color.BLACK);
        for (int offsetY = -2; offsetY <= 2; offsetY++) {
            for (int offsetX = -2; offsetX <= 2; offsetX++) {
                drawCenteredString(g, text, getWidth() / 2 + offsetX, 70 + offsetY);
            }
        }
        g.setColor(Color.WHITE);
        drawCenteredString(g, text, getWidth() / 2, 70);
    }

    private void drawPauseButton(Graphics2D g) {
        int x = 16;
        int y = 18;
        pauseButtonBounds.setBounds(x, y, 38, 38);
        g.setColor(new Color(20, 32, 38));
        g.fillRoundRect(x - 2, y - 2, 42, 42, 8, 8);
        g.setColor(new Color(245, 112, 14));
        g.fillRoundRect(x, y, 38, 38, 7, 7);
        g.setColor(new Color(255, 255, 255));
        if (paused) {
            g.fillPolygon(new int[] {x + 14, x + 14, x + 27},
                    new int[] {y + 9, y + 29, y + 19}, 3);
        } else {
            g.fillRoundRect(x + 10, y + 9, 6, 20, 2, 2);
            g.fillRoundRect(x + 22, y + 9, 6, 20, 2, 2);
        }
    }

    private void drawPauseOverlay(Graphics2D g) {
        int cardWidth = 230;
        int cardHeight = 120;
        int x = (getWidth() - cardWidth) / 2;
        int y = (getHeight() - cardHeight) / 2;
        drawCard(g, x, y, cardWidth, cardHeight);
        g.setColor(new Color(39, 74, 83));
        g.setFont(new Font("Arial", Font.BOLD, 25));
        drawCenteredString(g, "PAUSED", getWidth() / 2, y + 48);
        g.setColor(new Color(92, 116, 121));
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        drawCenteredString(g, "CLICK THE PAUSE BUTTON TO RESUME",
                getWidth() / 2, y + 80);
    }

    private void drawBestScore(Graphics2D g) {
        String text = "BEST " + bestScore;
        g.setFont(new Font("Arial", Font.BOLD, 13));
        int width = g.getFontMetrics().stringWidth(text) + 22;
        g.setColor(new Color(27, 55, 69, 150));
        g.fill(new RoundRectangle2D.Float(10, 14, width, 29, 15, 15));
        g.setColor(Color.WHITE);
        drawCenteredString(g, text, 10 + width / 2, 33);
    }

    private void drawOverlay(Graphics2D g) {
        if (state == GameState.START) {
            drawBestScore(g);
            int titleY = 76;
            int titleWidth = Math.min(310, getWidth() - 28);
            g.setColor(new Color(31, 76, 87, 190));
            g.fill(new RoundRectangle2D.Float((getWidth() - titleWidth) / 2, titleY - 38,
                    titleWidth, 52, 18, 18));
            g.setColor(new Color(255, 194, 66));
            g.setFont(new Font("Arial", Font.BOLD, 29));
            drawCenteredString(g, "FLAPPY BIRD", getWidth() / 2, titleY);

            drawCard(g, 14, 108, getWidth() - 28, 132);
            g.setColor(new Color(39, 74, 83));
            g.setFont(new Font("Arial", Font.BOLD, 22));
            drawCenteredString(g, "GET READY!", getWidth() / 2, 140);
            g.setColor(new Color(92, 116, 121));
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            drawCenteredString(g, "TAP TO FLAP", getWidth() / 2, 164);
            drawCenteredString(g, "SPACE OR CLICK TO LAUNCH", getWidth() / 2, 183);
            drawButton(g, "PLAY", getWidth() / 2 - 52, 193, 104, 36);
        } else {
            int cardWidth = Math.min(306, getWidth() - 28);
            int cardHeight = 286;
            int x = (getWidth() - cardWidth) / 2;
            int y = (getHeight() - cardHeight) / 2;
            drawCard(g, x, y, cardWidth, cardHeight);

            g.setColor(new Color(206, 75, 67));
            g.setFont(new Font("Arial", Font.BOLD, 29));
            drawCenteredString(g, "GAME OVER!", getWidth() / 2, y + 43);
            g.setColor(new Color(110, 125, 130));
            g.setFont(new Font("Arial", Font.BOLD, 13));
            drawCenteredString(g, "SCORE", getWidth() / 2, y + 72);
            g.setColor(new Color(39, 74, 83));
            g.setFont(new Font("Arial", Font.BOLD, 38));
            drawCenteredString(g, Integer.toString(score), getWidth() / 2, y + 112);

            if (newBest) {
                g.setColor(new Color(244, 169, 48));
                g.setFont(new Font("Arial", Font.BOLD, 13));
                drawCenteredString(g, "\u2605 NEW BEST!", getWidth() / 2, y + 135);
            }

            g.setColor(new Color(110, 125, 130));
            g.setFont(new Font("Arial", Font.BOLD, 14));
            drawCenteredString(g, "BEST  " + bestScore, getWidth() / 2, y + 163);
            drawMedal(g, getWidth() / 2, y + 202);
            drawButton(g, "PLAY AGAIN", getWidth() / 2 - 78, y + 226, 156, 42);
        }
    }

    private void drawCard(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(new Color(20, 43, 54, 55));
        g.fill(new RoundRectangle2D.Float(x + 3, y + 6, width, height, 24, 24));
        g.setColor(new Color(255, 255, 255, 238));
        g.fill(new RoundRectangle2D.Float(x, y, width, height, 24, 24));
    }

    private void drawButton(Graphics2D g, String text, int x, int y, int width, int height) {
        playButtonBounds.setBounds(x, y, width, height);
        g.setColor(new Color(233, 160, 48));
        g.fill(new RoundRectangle2D.Float(x, y + 3, width, height, 20, 20));
        g.setColor(new Color(255, 194, 66));
        g.fill(new RoundRectangle2D.Float(x, y, width, height - 3, 20, 20));
        g.setColor(new Color(88, 66, 36));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        drawCenteredString(g, text, x + width / 2, y + height / 2 + 5);
    }

    private void drawMedal(Graphics2D g, int centerX, int y) {
        String medal;
        Color medalColor;
        if (score >= 50) {
            medal = "PLATINUM MEDAL";
            medalColor = new Color(133, 159, 177);
        } else if (score >= 35) {
            medal = "GOLD MEDAL";
            medalColor = new Color(226, 171, 49);
        } else if (score >= 20) {
            medal = "SILVER MEDAL";
            medalColor = new Color(143, 157, 166);
        } else if (score >= 10) {
            medal = "BRONZE MEDAL";
            medalColor = new Color(180, 111, 70);
        } else {
            medal = "NO MEDAL YET";
            medalColor = new Color(130, 142, 145);
        }
        g.setColor(medalColor);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        drawCenteredString(g, medal, centerX, y);
    }

    private void drawCenteredString(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

}
