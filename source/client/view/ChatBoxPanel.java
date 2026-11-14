package client.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

public class ChatBoxPanel extends JPanel {
    private JTextPane txtChat;
    private JTextField txtInput;
    private JButton btnSend;
    private StyledDocument document;

    public ChatBoxPanel() {
        initComponents();
    }

    public void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("CHAT"));
        txtChat = new JTextPane();
        txtChat.setEditable(false);
        txtChat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        document = txtChat.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(txtChat);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnSend = new JButton("Gửi");
        btnSend.setBackground(new Color(41, 128, 185));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);

        inputPanel.add(txtInput, BorderLayout.WEST);
        inputPanel.add(btnSend, BorderLayout.EAST);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void addSendListener(ActionListener listener) {
        btnSend.addActionListener(listener);
    }

    public String getInputText() {
        return txtInput.getText().trim();
    }

    public void clearInput() {
        txtInput.setText("");
    }

    public void appendNormalMessage(String sender, String message) {
        appendMessage(sender, message, Color.BLACK);
    }

    public void appendCorrectMessage(String sender, String message) {
        appendMessage(sender, message, new Color(39, 174, 96));
    }

    public void appendMessage(String sender, String message, Color color) {
        try {
            Style style = txtChat.addStyle("ChatStyle", null);

            StyleConstants.setForeground(style, color);
            StyleConstants.setFontFamily(style, "Arial");
            StyleConstants.setFontSize(style, 13);

            String content = sender + ": " + message + "\n";
            document.insertString(document.getLength(),content,style
            );
            txtChat.setCaretPosition(document.getLength());

        } catch (BadLocationException ex) {
        }
    }
}
