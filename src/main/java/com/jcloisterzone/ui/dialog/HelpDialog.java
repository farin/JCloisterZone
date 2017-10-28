package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.panel.HelpPanel;


public class HelpDialog extends JDialog {

    private static final long serialVersionUID = 4697784648983290492L;

    private final JPanel contentPanel = new JPanel();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            HelpDialog dialog = new HelpDialog();
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public HelpDialog() {
        setTitle(_tr("Controls"));
        UiUtils.centerDialog(this, 480, 300);
        contentPanel.setBounds(0, 0, 480, 30);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BorderLayout());
        getContentPane().add(new HelpPanel());

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }
}
