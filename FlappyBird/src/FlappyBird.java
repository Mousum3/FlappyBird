import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener {
    // Board settings
    private final int BOARD_WIDTH = 360;
    private final int BOARD_HEIGHT = 640;

    // Bird settings
    private final int BIRD_WIDTH = 34;
    private final int BIRD_HEIGHT = 24;
    private int birdX = BOARD_WIDTH / 8;
    private int birdY = BOARD_HEIGHT / 2;
    private int velocityY = 0;
    private final int GRAVITY = 1;
    private Image birdImage;

    // Pipe settings
    private final int PIPE_WIDTH = 64;
    private final int PIPE_HEIGHT = 512;
    private int velocityX = -2;
    private List<Pipe> pipes = new ArrayList<>();
    private final int OPENING_SPACE = BOARD_HEIGHT / 4;
    private Image topPipeImage, bottomPipeImage;

    // Background settings
    private Image backgroundImage;

    // Game settings
    private Timer timer;
    private boolean gameOver = false;
    private int score = 0;

    public FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                moveBird(e);
            }
        });

        loadImages();

        timer = new Timer(20, this); // 20 ms delay for ~50 fps
        timer.start();

        placePipes();
    }

    private void loadImages() {
        // Load bird image
        birdImage = new ImageIcon("flappybird.png").getImage();

        // Load pipe images
        topPipeImage = new ImageIcon("toppipe.png").getImage();
        bottomPipeImage = new ImageIcon("bottompipe.png").getImage();

        // Load background image
        backgroundImage = new ImageIcon("background.png").getImage(); // Ensure the image is in the project directory
    }

    private void moveBird(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_X) {
            velocityY = -10;

            if (gameOver) {
                resetGame();
            }
        }
    }

    private void resetGame() {
        birdY = BOARD_HEIGHT / 2;
        pipes.clear();
        score = 0;
        gameOver = false;
        velocityY = 0;
    }

    private void placePipes() {
        if (gameOver) return;

        Random rand = new Random();
        int randomPipeY = -PIPE_HEIGHT / 4 - rand.nextInt(PIPE_HEIGHT / 2);

        pipes.add(new Pipe(BOARD_WIDTH, randomPipeY, PIPE_WIDTH, PIPE_HEIGHT, true));
        pipes.add(new Pipe(BOARD_WIDTH, randomPipeY + PIPE_HEIGHT + OPENING_SPACE, PIPE_WIDTH, PIPE_HEIGHT, false));

        // Schedule next pipe placement
        Timer pipeTimer = new Timer(2500, e -> placePipes());
        pipeTimer.setRepeats(false);
        pipeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        g.drawImage(backgroundImage, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // Draw bird using the bird image
        g.drawImage(birdImage, birdX, birdY, BIRD_WIDTH, BIRD_HEIGHT, this);

        // Draw pipes using the pipe images
        for (Pipe pipe : pipes) {
            if (pipe.isTopPipe()) {
                g.drawImage(topPipeImage, pipe.getX(), pipe.getY(), pipe.getWidth(), pipe.getHeight(), this);
            } else {
                g.drawImage(bottomPipeImage, pipe.getX(), pipe.getY(), pipe.getWidth(), pipe.getHeight(), this);
            }
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 10, 30);

        if (gameOver) {
            g.drawString("Game Over", 100, 100);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Apply gravity
        velocityY += GRAVITY;
        birdY += velocityY;

        // Check for collision with ground
        if (birdY > BOARD_HEIGHT) {
            gameOver = true;
        }

        // Move pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.move(velocityX);

            if (pipe.isPassed() && birdX > pipe.getX() + pipe.getWidth()) {
                score++;
                pipe.setPassed(false);
            }

            if (detectCollision(birdX, birdY, BIRD_WIDTH, BIRD_HEIGHT, pipe)) {
                gameOver = true;
            }
        }

        // Remove off-screen pipes
        pipes.removeIf(pipe -> pipe.getX() + pipe.getWidth() < 0);

        repaint();
    }

    private boolean detectCollision(int x1, int y1, int w1, int h1, Pipe pipe) {
        return x1 < pipe.getX() + pipe.getWidth() &&
                x1 + w1 > pipe.getX() &&
                y1 < pipe.getY() + pipe.getHeight() &&
                y1 + h1 > pipe.getY();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Pipe {
    private int x, y, width, height;
    private boolean passed;
    private boolean topPipe;

    public Pipe(int x, int y, int width, int height, boolean topPipe) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.passed = true;
        this.topPipe = topPipe;
    }

    public void move(int velocityX) {
        x += velocityX;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public boolean isTopPipe() { return topPipe; }
}
