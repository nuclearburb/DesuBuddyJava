import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

class DraggableFrame extends JFrame {
    private long lastClickTime = 0;
    private int initialX, initialY, initialMouseX, initialMouseY;
    private String[] dolls = {"buddies/suiseiseki/", "buddies/suigintou/"};
    private int currentDollIndex = 0; // suiseiseki = 0, suigintou = 1
    private String[] imageFiles = {"0.gif", "1.gif", "2.gif", "3.gif", "4.gif"};
    private String[] soundFiles = {"click1.wav", "click2.wav", "click3.wav", "close.wav"};
    private int currentImageIndex = 0;
    private javax.sound.sampled.Clip closeClip;
    private CustomPanel customPanel;

    private void defaultLogic() throws InterruptedException {
        BufferedImage img = loadImage(dolls[currentDollIndex] + "images/blink/" + imageFiles[0]);
        if (img != null) {
            customPanel.setImage(img);
        }
        int randomSleepTime = ThreadLocalRandom.current().nextInt(2000, 7000); // Random sleep time between 2 and 10 seconds
        Thread.sleep(randomSleepTime);
        blink();
    }

    private void blink() throws InterruptedException {
        while (true) {
            for (int i = 0; i < imageFiles.length; i++) {
                BufferedImage img = loadImage(dolls[currentDollIndex] + "images/blink/" + imageFiles[i]);
                if (img != null) {
                    customPanel.setImage(img);
                }
                Thread.sleep(20);
            }
            defaultLogic();
        }
    }

    DraggableFrame() {
        try {
            File closeSoundFile = new File(dolls[currentDollIndex] + "sounds/close.wav");
            if (closeSoundFile.exists()) {
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(closeSoundFile);
                closeClip = javax.sound.sampled.AudioSystem.getClip();
                closeClip.open(audioIn);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setAlwaysOnTop(true);
        setSize(105, 140);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());

        customPanel = new CustomPanel();
        add(customPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    setOpacity(0.7f);
                    initialMouseX = e.getXOnScreen();
                    initialMouseY = e.getYOnScreen();
                    initialX = getLocation().x;
                    initialY = getLocation().y;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    showPopupMenu(e.getComponent(), e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                setOpacity(1.0f);
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    int newX = initialX + (e.getXOnScreen() - initialMouseX);
                    int newY = initialY + (e.getYOnScreen() - initialMouseY);
                    setLocation(newX, newY);
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (closeClip != null) {
                    closeClip.start();
                    try {
                        Thread.sleep(closeClip.getMicrosecondLength() / 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });

        setVisible(true);

        try {
            defaultLogic();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showPopupMenu(Component component, int x, int y) {
        try {
            String soundFileName = dolls[currentDollIndex] + "sounds/" + soundFiles[ThreadLocalRandom.current().nextInt(0, 3)];
            while (soundFileName.equals(dolls[currentDollIndex] + "sounds/close.wav")) {
                soundFileName = "sounds/" + soundFiles[ThreadLocalRandom.current().nextInt(0, 3)];
            }
            File soundFile = new File(soundFileName);
            if (soundFile.exists()) {
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundFile);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                System.err.println("Sound file not found!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem1 = new JMenuItem("Option 1");
        JMenuItem menuItem2 = new JMenuItem("Option 2");
        JMenuItem menuItem3 = new JMenuItem("Option 3");

        menuItem1.addActionListener(e -> {
            // perform action for option 1
            System.out.println("Option 1 selected");
        });

        menuItem2.addActionListener(e -> {
            // perform action for option 2
            System.out.println("Option 2 selected");
        });

        menuItem3.addActionListener(e -> {
            // perform action for option 3
            System.out.println("Option 3 selected");
        });

        popupMenu.add(menuItem1);
        popupMenu.add(menuItem2);
        popupMenu.add(menuItem3);

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> {
            // perform action for closing the program
            if (closeClip != null) {
                closeClip.start();
                try {
                    Thread.sleep(closeClip.getMicrosecondLength() / 1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            System.exit(0);
        });
        popupMenu.addSeparator();
        popupMenu.add(closeItem);

        popupMenu.show(component, x, y);
    }

    private BufferedImage loadImage(String fileName) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(fileName));
            BufferedImage transparentImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = transparentImage.createGraphics();
            graphics.setComposite(AlphaComposite.Src);

            // Apply green screen filter
            for (int x = 0; x < originalImage.getWidth(); x++) {
                for (int y = 0; y < originalImage.getHeight(); y++) {
                    Color pixelColor = new Color(originalImage.getRGB(x, y), true);
                    if (pixelColor.getRed() < 50 && pixelColor.getGreen() > 200 && pixelColor.getBlue() < 50) {
                        pixelColor = new Color(0, 0, 0, 0); // Make green transparent
                    }
                    transparentImage.setRGB(x, y, pixelColor.getRGB());
                }
            }

            graphics.drawImage(transparentImage, 0, 0, null);
            graphics.dispose();
            return transparentImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class CustomPanel extends JPanel {
        private BufferedImage image;

        public CustomPanel() {
            setOpaque(false);
        }

        public void setImage(BufferedImage img) {
            this.image = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(image, 0, 0, this);
            }
        }
    }

}

public class Suiseiseki {
    public static void main(String[] args) {
        new DraggableFrame();
    }
}