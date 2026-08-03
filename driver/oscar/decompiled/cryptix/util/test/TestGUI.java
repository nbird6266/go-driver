/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.test;

import cryptix.util.gui.TextAreaWriter;
import cryptix.util.test.BaseTest;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;

public class TestGUI
extends Panel {
    private static final Dimension minimumSize = new Dimension(100, 100);
    private static final Dimension preferredSize = new Dimension(300, 300);
    private Frame frame;
    private BaseTest owner;
    private PrintWriter output;
    private TextArea textArea;
    private boolean allowExit;

    public TestGUI(BaseTest owner) {
        this.owner = owner;
        this.init();
    }

    protected void init() {
        this.textArea = new TextArea();
        this.textArea.setEditable(false);
        Font font = Font.getFont("monospaced");
        if (font != null) {
            this.textArea.setFont(font);
        }
        this.output = new PrintWriter(new TextAreaWriter(this.textArea));
        this.setLayout(new GridLayout(1, 1));
        this.add(this.textArea);
        this.textArea.addMouseListener(new MouseAdapter(){

            public void mouseClicked(MouseEvent evt) {
                TestGUI.this.allowExit = true;
                TestGUI.this.notifyAll();
            }
        });
        this.textArea.addKeyListener(new KeyAdapter(){

            public void keyTyped(KeyEvent evt) {
                TestGUI.this.allowExit = true;
                TestGUI.this.notifyAll();
            }
        });
    }

    public void update(Graphics g) {
        this.paint(g);
    }

    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public PrintWriter getOutput() {
        return this.output;
    }

    public synchronized void useAppFrame(boolean flag) {
        if (flag && this.frame == null) {
            this.frame = new Frame();
            this.frame.setSize(preferredSize);
            this.frame.setTitle(this.getName());
            this.frame.add(this);
            this.frame.setVisible(true);
            this.frame.addWindowListener(new WindowAdapter(){

                public void windowClosing(WindowEvent evt) {
                    System.exit(5);
                }
            });
        } else if (!flag && this.frame != null) {
            this.frame.dispose();
            this.frame = null;
        }
    }

    public synchronized void waitForExit() {
        while (!this.allowExit) {
            try {
                this.wait();
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }
}

