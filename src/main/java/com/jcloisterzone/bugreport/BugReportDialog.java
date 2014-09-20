package com.jcloisterzone.bugreport;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jcloisterzone.ui.panel.HelpPanel;


public class BugReportDialog extends JDialog {

    private static final long serialVersionUID = 4697784648983290492L;

    private final JPanel contentPanel = new JPanel();


    /**
     * Create the dialog.
     */
    public BugReportDialog(ReportingTool reportingTool) {
        setTitle(_("Report bug"));
        centerDialog(480, 300);
        contentPanel.setBounds(0, 0, 480, 30);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.setLayout(new BorderLayout());
        BugReportPanel panel = new BugReportPanel();
        panel.setReportingTool(reportingTool);
        panel.setParent(this);
        getContentPane().add(panel);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private void centerDialog(int width, int height) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        setBounds(screenWidth / 2 - width / 2, screenHeight / 3 - height / 2, width, height);

    }
}
