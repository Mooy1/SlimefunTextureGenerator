package io.github.mooy1.resourcepackgenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

public final class ConsoleWindow {
    
    private JTextPane output;
    
    public ConsoleWindow() {
        // set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.exit(1);
            return;
        }
        
        // create frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.LIGHT_GRAY);
        frame.setTitle("Resource Pack Generator");
        frame.setSize(720, 480);
        frame.setLocation(600, 300);
        frame.setResizable(true);
        frame.setVisible(true);

        // add icon
        //ImageIcon icon = new ImageIcon("resources/");
        //frame.setIconImage(icon.getImage());

        // add output area
        JTextPane output = this.output = new JTextPane();
        output.setEditable(false);
        output.setOpaque(false);
        frame.add(output, BorderLayout.NORTH);
        
        // add scroll bar
        JScrollPane scroll = new JScrollPane();
        frame.add(scroll, BorderLayout.EAST);
    }
    
    public void print(String string) {
        
    }
    
}
