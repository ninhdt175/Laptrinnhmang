
package client.view;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
public class LobbyView extends JFrame {
    private JTable tblRoom;
    private DefaultTableModel tableModel;
    private JButton btnCreateRoom;
    private JButton btnJoinRoom;
    public LobbyView(){
        initComponents();
    }
    private void initComponents(){
        setTitle("Sanh cho");
        setSize(600,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        JLabel lblTitle = new JLabel("Danh sach phong choi",SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial",Font.BOLD,18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        add(lblTitle,BorderLayout.NORTH);
        
        tableModel = new DefaultTableModel(new String[]{"Ma phong","So nguoi","Trang thai"},0){
            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };
        tblRoom = new JTable(tableModel);
        add(new JScrollPane(tblRoom),BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        btnCreateRoom = new JButton("Tao phong moi");
        btnJoinRoom = new JButton("Vao phong");
        
        bottomPanel.add(btnCreateRoom);
        bottomPanel.add(btnJoinRoom);
        add(bottomPanel,BorderLayout.SOUTH);
        
    }
    public void addCreateRoomListener(ActionListener listener) {
        btnCreateRoom.addActionListener(listener);
    }

    public void addJoinRoomListener(ActionListener listener) {
        btnJoinRoom.addActionListener(listener);
    }
    public int getSelectedRoomRow() {
        return tblRoom.getSelectedRow();
    }

    public Object getTableValueAt(int row, int col) {
        return tableModel.getValueAt(row, col);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            LobbyView lb = new LobbyView();
           lb.setVisible(true);
        });
    }
}
