//Generated by GuiGenie - Copyright (c) 2004 Mario Awad.
//Home Page http://guigenie.cjb.net - Check often for new versions!

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class NapovedaMenu extends JPanel {
    private JButton jcomp1;
    private JButton jcomp2;

    public NapovedaMenu() {
        //construct components
        jcomp1 = new JButton ("Napoveda");
        jcomp2 = new JButton ("Vyresit");

        //adjust size and set layout
        setPreferredSize (new Dimension (182, 104));
        setLayout (null);

        //add components
        add (jcomp1);
        jcomp1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.napoveda();
            }
        });
        add (jcomp2);
        jcomp2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.vyresit();
            }
        });
        //set component bounds (only needed by Absolute Positioning)
        jcomp1.setBounds (45, 20, 100, 20);
        jcomp2.setBounds (45, 50, 100, 20);

    }


    static Panel panel;
    public static void run (Panel p) {
        panel = p;
        JFrame frame = new JFrame ("Napoveda");
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add (new NapovedaMenu());
        frame.pack();
        frame.setVisible (true);
    }
}
