package net.ftb.gui.dialogs;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import net.ftb.gui.LaunchFrame;
import net.ftb.locale.I18N;

public class YNDialog extends JDialog {
    private JLabel messageLbl;
    private JLabel overwriteLbl;
    public JButton overwrite;
    public JButton abort;
    private String message;
    private String confirmMsg;
    public boolean ready = false;
    public boolean ret = false;
    public YNDialog(String unlocMessage, String unlocConfirmMessage, String unlocTitle) {
        super(LaunchFrame.getInstance(), true);
        message = I18N.getLocaleString(unlocMessage);
        confirmMsg = I18N.getLocaleString(unlocConfirmMessage);
        this.setTitle(I18N.getLocaleString(unlocTitle));
        ret = false;
        ready = false;
        setupGui();
        overwrite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                ready = true;
                ret = true;
                setVisible(false);
            }
        });

        abort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent event) {
                ret = false;
                ready = true;
                setVisible(false);
            }
        });

    }

    private void setupGui () {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
        setResizable(false);

        Container panel = getContentPane();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        messageLbl = new JLabel(message);
        overwriteLbl = new JLabel(confirmMsg);
        overwrite = new JButton(I18N.getLocaleString("MAIN_YES"));
        abort = new JButton(I18N.getLocaleString("MAIN_NO"));

        messageLbl.setHorizontalAlignment(SwingConstants.CENTER);
        overwriteLbl.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(messageLbl);
        panel.add(overwriteLbl);
        panel.add(overwrite);
        panel.add(abort);

        Spring hSpring;
        Spring columnWidth;

        hSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.WEST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.WEST, overwriteLbl, hSpring, SpringLayout.WEST, panel);

        columnWidth = Spring.width(messageLbl);
        columnWidth = Spring.max(columnWidth, Spring.width(overwriteLbl));

        hSpring = Spring.sum(hSpring, columnWidth);

        layout.putConstraint(SpringLayout.EAST, messageLbl, hSpring, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, overwriteLbl, hSpring, SpringLayout.WEST, panel);

        hSpring = Spring.sum(hSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.EAST, panel, hSpring, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.EAST, overwrite, -5, SpringLayout.HORIZONTAL_CENTER, panel);
        layout.putConstraint(SpringLayout.WEST, abort, 5, SpringLayout.HORIZONTAL_CENTER, panel);

        Spring vSpring;
        Spring rowHeight;

        vSpring = Spring.constant(10);

        layout.putConstraint(SpringLayout.NORTH, messageLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(messageLbl));
        vSpring = Spring.sum(vSpring, Spring.constant(5));

        layout.putConstraint(SpringLayout.NORTH, overwriteLbl, vSpring, SpringLayout.NORTH, panel);

        vSpring = Spring.sum(vSpring, Spring.height(overwriteLbl));
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.NORTH, overwrite, vSpring, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, abort, vSpring, SpringLayout.NORTH, panel);

        rowHeight = Spring.height(overwrite);
        rowHeight = Spring.max(rowHeight, Spring.height(abort));

        vSpring = Spring.sum(vSpring, rowHeight);
        vSpring = Spring.sum(vSpring, Spring.constant(10));

        layout.putConstraint(SpringLayout.SOUTH, panel, vSpring, SpringLayout.NORTH, panel);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
