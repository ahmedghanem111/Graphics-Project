
package  brawhalla;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InstructionsPanel extends JPanel {
    ArenaEscape frame;
    JButton back;

    public InstructionsPanel(ArenaEscape f) {
        this.frame = f;
        setLayout(null);
        setBackground(Color.BLACK);

        JTextArea txt = new JTextArea(
                "Instructions:\n\n- Move with arrow keys\n- Avoid enemies\n- Collect points\n- Survive!"
        );
        txt.setEditable(false);
        txt.setFont(new Font("Arial", Font.BOLD, 22));
        txt.setOpaque(false);
        txt.setForeground(Color.WHITE);
        txt.setBounds(40, 40, 880, 460);
        add(txt);

        back = new JButton("Back");
        styleBackButton(back);
        back.addActionListener(e -> frame.showScreen("menu"));
        add(back);

        updateBackPosition();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBackPosition();
            }
        });
    }

    private void styleBackButton(JButton b) {
        b.setFont(new Font("Arial", Font.BOLD, 24));
        b.setBackground(new Color(20,20,30));
        b.setForeground(Color.CYAN);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.CYAN,2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void updateBackPosition() {
        back.setBounds(20, getHeight() - 80, 140, 44);
    }
}
