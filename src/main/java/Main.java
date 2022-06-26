import javax.swing.*;

public class Main {
    public static JFrame window;
    public static void main(String[] args){
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Domino");

        Panel panel = new Panel();
        window.add(panel);
        window.addMouseListener(new MouseHandler(panel));
        window.setUndecorated(true); // <-- the title bar is removed here

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
        panel.start();

        NapovedaMenu.run(panel);
    }
}
