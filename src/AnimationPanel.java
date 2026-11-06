import java.awt.*;
import java.util.List;
import javax.swing.*;

public final class AnimationPanel extends JPanel implements AutoCloseable {
    private final List<Ball> balls = List.of(
        new Ball(1, new Color(72, 148, 255), 90, 100, 3.2, 2.1, 800, 360),
        new Ball(2, new Color(255, 176, 65), 380, 240, -2.4, 3.0, 800, 360),
        new Ball(3, new Color(168, 117, 255), 670, 150, 2.8, -2.5, 800, 360)
    );
    private final JButton start = new JButton("Старт");
    private final JButton pause = new JButton("Пауза");
    private final JButton reset = new JButton("Сброс");
    private final JSlider speed = new JSlider(25, 250, 100);
    private final JLabel status = new JLabel("Готово к запуску · 3 отдельных потока");
    private final JLabel speedLabel = new JLabel("Скорость: 1,00×");
    private final JLabel[] counters = new JLabel[3];
    private final Timer timer;
    private final JPanel arena;

    public AnimationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 26, 22, 26));
        setBackground(new Color(242, 245, 251));
        JPanel header = new JPanel(new GridLayout(0, 1, 0, 7));
        header.setOpaque(false);
        JLabel title = new JLabel("Три мяча — три потока");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        header.add(title);
        header.add(new JLabel("Лабораторная работа № 6 · Основы работы с потоками"));
        add(header, BorderLayout.NORTH);
        arena = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(39, 49, 70));
                for (int x = 0; x < getWidth(); x += 40) {
                    for (int y = 0; y < getHeight(); y += 40) {
                        g.fillOval(x, y, 2, 2);
                    }
                }
                for (Ball ball : balls) {
                    Ball.State state = ball.snapshot();
                    int x = (int) Math.round(state.x());
                    int y = (int) Math.round(state.y());
                    int r = Ball.RADIUS;
                    g.setColor(new Color(0, 0, 0, 70));
                    g.fillOval(x - r + 3, y - r + 6, r * 2, r * 2);
                    g.setColor(state.color());
                    g.fillOval(x - r, y - r, r * 2, r * 2);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("SansSerif", Font.BOLD, 15));
                    g.drawString(state.threadName().substring(5), x - 4, y + 5);
                    g.setColor(new Color(213, 224, 244));
                    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    g.drawString(state.threadName(), x - 17, y + 40);
                }
                g.dispose();
            }
        };
        arena.setPreferredSize(new Dimension(800, 360));
        arena.setBackground(new Color(20, 28, 45));
        add(arena, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 0));
        metrics.setOpaque(false);
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new JLabel();
            counters[i].setForeground(balls.get(i).snapshot().color().darker());
            metrics.add(counters[i]);
        }
        footer.add(metrics, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(start);
        controls.add(pause);
        controls.add(reset);
        controls.add(Box.createHorizontalStrut(12));
        controls.add(speedLabel);
        speed.setOpaque(false);
        speed.setPreferredSize(new Dimension(150, 32));
        controls.add(speed);
        footer.add(controls, BorderLayout.CENTER);
        footer.add(status, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        pause.setEnabled(false);
        start.addActionListener(e -> startMotion());
        pause.addActionListener(e -> pauseMotion());
        reset.addActionListener(e -> resetMotion());
        speed.addChangeListener(e -> {
            balls.forEach(ball -> ball.setSpeed(speed.getValue() / 100.0));
            speedLabel.setText(String.format("Скорость: %.2f×", speed.getValue() / 100.0));
        });
        timer = new Timer(16, e -> refresh());
        timer.start();
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            if (arena.getWidth() > 0 && arena.getHeight() > 0) {
                ball.setBounds(arena.getWidth(), arena.getHeight());
            }
            Ball.State state = ball.snapshot();
            counters[i].setText(state.threadName() + " · шагов: " + state.steps());
        }
        arena.repaint();
    }

    public void startMotion() {
        balls.forEach(ball -> ball.setMoving(true));
        start.setEnabled(false);
        pause.setEnabled(true);
        status.setText("Движение · каждый мяч обновляется своим потоком");
    }

    public void pauseMotion() {
        balls.forEach(ball -> ball.setMoving(false));
        start.setEnabled(true);
        start.setText("Продолжить");
        pause.setEnabled(false);
        status.setText("Пауза · потоки ожидают продолжения");
    }

    public void resetMotion() {
        balls.forEach(Ball::reset);
        start.setEnabled(true);
        start.setText("Старт");
        pause.setEnabled(false);
        status.setText("Исходные позиции · нажмите «Старт»");
        refresh();
    }

    public void setSpeedPercent(int value) {
        speed.setValue(value);
    }

    @Override
    public void close() {
        timer.stop();
        balls.forEach(Ball::stop);
        for (Ball ball : balls) {
            try {
                ball.awaitTermination();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
