package com.fpetrola.cap;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.piccolo2d.extras.pswing.PSwing;
import org.piccolo2d.extras.pswing.PSwingCanvas;

/**
 * A simple example showing how to use RSyntaxTextArea to add Java syntax
 * highlighting to a Swing application.
 * <p>
 * 
 * This example uses RSyntaxTextArea 3.0.5.
 */
public class TextEditorDemo extends JFrame {

	public TextEditorDemo() {

		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(BorderFactory.createLineBorder(Color.green));

		JButton component = new JButton("Hola Mundo");
		PSwing swingWrapper = new PSwing(cp);

		PSwingCanvas canvas = new PSwingCanvas();
		canvas.getLayer().addChild(swingWrapper);
		canvas.setBorder(BorderFactory.createLineBorder(Color.red));

		RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		cp.add(sp);
		cp.setSize(400, 300);

		add(canvas, BorderLayout.CENTER);
		setSize(400, 300);
		setTitle("Text Editor Demo");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		pack();
		setLocationRelativeTo(null);

	}

	public static void main(String[] args) {
		// Start all Swing applications on the EDT.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new TextEditorDemo().setVisible(true);
			}
		});
	}

}