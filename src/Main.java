import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnimationPanel panel = new AnimationPanel();
            JFrame frame = new JFrame("Лабораторная № 6 — потоки");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(panel);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {
                    panel.close();
                }
            });
            frame.pack();
            frame.setMinimumSize(frame.getSize());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
