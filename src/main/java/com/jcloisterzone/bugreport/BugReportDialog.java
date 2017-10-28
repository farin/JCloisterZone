package com.jcloisterzone.bugreport;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.gtk.ThemedJPanel;


public class BugReportDialog extends JDialog {

    private static final long serialVersionUID = 4697784648983290492L;

    private final JPanel contentPanel = new ThemedJPanel();


    /**
     * Create the dialog.
     */
    public BugReportDialog(ReportingTool reportingTool) {
        setTitle(_tr("Report bug"));
        UiUtils.centerDialog(this, 560, 300);
        contentPanel.setBounds(0, 0, 560, 30);
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
}
