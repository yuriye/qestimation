package com.ys.mfc;

import javax.swing.*;
import java.awt.event.*;

public class AskForOredrCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonClose;
    private JButton buttonNext;
    private JTabbedPane tabbedPane;
    private JPanel askForOrderCode;
    private JPanel log;
    private JTextField textField2;
    private JButton startButton;
    private JTextArea logText;

    public AskForOredrCodeDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonClose);

        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AskForOredrCodeDialog dialog = new AskForOredrCodeDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
