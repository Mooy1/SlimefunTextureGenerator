package io.github.mooy1.resourcepackgenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public final class Console {
    
    private final JTextArea output;
    
    public Console() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.exit(1);
        }
        
        // create frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        frame.setTitle("Slimefun Texture Generator");
        int width = 1200;
        int height = 800;
        frame.setSize(width, height);
        frame.setLocation(960 - width / 2, 540 - height / 2);
        frame.setResizable(true);

        // add icon
        // ImageIcon icon = new ImageIcon("");
        // frame.setIconImage(icon.getImage());
        
        // add output area
        JTextArea output = this.output = new JTextArea();
        output.setEditable(false);
        output.setOpaque(false);
        output.setFont(output.getFont().deriveFont(12f));
        frame.add(output, BorderLayout.NORTH);
        
        // show
        frame.setVisible(true);
    }
    
    public synchronized void status(String msg) throws InterruptedException {
        wait(1);
        this.output.append(msg + "\n\n");
        wait(1);
    }
    
    public void print(String msg) {
        this.output.append(msg + "\n");
    }
    
}
