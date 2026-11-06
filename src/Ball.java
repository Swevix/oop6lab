import java.awt.Color;

public final class Ball implements Runnable {
    public record State(double x, double y, Color color, String threadName, long steps) {}

    public static final int RADIUS = 22;
    private final Object lock = new Object();
    private final double initialX;
    private final double initialY;
    private final double initialVx;
    private final double initialVy;
    private final Color color;
    private final Thread worker;
    private double x;
    private double y;
    private double vx;
    private double vy;
    private int width;
    private int height;
    private double speed = 1;
    private boolean moving;
    private boolean closed;
    private long steps;

    public Ball(int number, Color color, double x, double y, double vx, double vy,
                int width, int height) {
        this.color = color;
        this.x = initialX = x;
        this.y = initialY = y;
        this.vx = initialVx = vx;
        this.vy = initialVy = vy;
        this.width = width;
        this.height = height;
        worker = new Thread(this, "Ball-" + number);
        worker.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (lock) {
                    while (!moving && !closed) {
                        lock.wait();
                    }
                    if (closed) {
                        return;
                    }
                    x += vx * speed;
                    y += vy * speed;
                    double right = Math.max(RADIUS, width - RADIUS);
                    double bottom = Math.max(RADIUS, height - RADIUS);
                    if (x < RADIUS || x > right) {
                        vx = -vx;
                        x = Math.max(RADIUS, Math.min(right, x));
                    }
                    if (y < RADIUS || y > bottom) {
                        vy = -vy;
                        y = Math.max(RADIUS, Math.min(bottom, y));
                    }
                    steps++;
                }
                Thread.sleep(16);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public State snapshot() {
        synchronized (lock) {
            return new State(x, y, color, worker.getName(), steps);
        }
    }

    public void setMoving(boolean value) {
        synchronized (lock) {
            if (!closed) {
                moving = value;
                lock.notifyAll();
            }
        }
    }

    public void setSpeed(double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Скорость должна быть положительной");
        }
        synchronized (lock) {
            speed = value;
        }
    }

    public void setBounds(int width, int height) {
        synchronized (lock) {
            this.width = width;
            this.height = height;
        }
    }

    public void reset() {
        synchronized (lock) {
            moving = false;
            x = initialX;
            y = initialY;
            vx = initialVx;
            vy = initialVy;
            steps = 0;
        }
    }

    public void stop() {
        synchronized (lock) {
            closed = true;
            moving = false;
            lock.notifyAll();
        }
        worker.interrupt();
    }

    public void awaitTermination() throws InterruptedException {
        worker.join(2000);
        if (worker.isAlive()) {
            throw new IllegalStateException("Поток не завершился: " + worker.getName());
        }
    }

    public boolean isAlive() {
        return worker.isAlive();
    }
}
