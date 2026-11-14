
package client.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;

public class ScoreboardPanel extends JPanel {
    private static final Color BORDER = new Color(203, 213, 225);

    private final DefaultTableModel scoreModel =new DefaultTableModel(
                    new String[]{"Người chơi", "Điểm"}, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable tblScores = new JTable(scoreModel);
    public ScoreboardPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(260, 100));
        setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER),"Bảng điểm",
                TitledBorder.LEFT,TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(23, 37, 84)
        ));
        tblScores.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblScores.setRowHeight(32);
        tblScores.setBackground(Color.WHITE);
        tblScores.setForeground(new Color(51, 65, 85));
        tblScores.setGridColor(BORDER);
        tblScores.setSelectionBackground(new Color(219, 234, 254));
        tblScores.setSelectionForeground(new Color(23, 37, 84));
        tblScores.setFillsViewportHeight(true);

        tblScores.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        tblScores.getTableHeader().setBackground(
                new Color(226, 232, 240)
        );
        tblScores.getTableHeader().setForeground(new Color(23, 37, 84));
        tblScores.getTableHeader().setReorderingAllowed(false);

        tblScores.getColumnModel().getColumn(0)
                .setPreferredWidth(165);
        tblScores.getColumnModel().getColumn(1)
                .setPreferredWidth(65);

        add(new JScrollPane(tblScores), BorderLayout.CENTER);
    }

    public void updateScoreboard(String scoreboard) {
        scoreModel.setRowCount(0);
        if (scoreboard == null || scoreboard.isBlank()) {
            return;
        }
        String[] entries = scoreboard.split(",");
        Arrays.sort(entries, (a, b) -> {
            try {
                int scoreA = Integer.parseInt(
                        a.substring(a.lastIndexOf(':') + 1).trim()
                );
                int scoreB = Integer.parseInt(
                        b.substring(b.lastIndexOf(':') + 1).trim()
                );
                return Integer.compare(scoreB, scoreA);
            } catch (RuntimeException ex) {
                return 0;
            }
        });
        for (String entry : entries) {
            int colon = entry.lastIndexOf(':');
            if (colon > 0) {
                String name = entry.substring(0, colon).trim();
                String score = entry.substring(colon + 1).trim();
                scoreModel.addRow(new Object[]{name, score});
            }
        }
    }
    public void clearLeaderboard() {
        scoreModel.setRowCount(0);
    }
}
