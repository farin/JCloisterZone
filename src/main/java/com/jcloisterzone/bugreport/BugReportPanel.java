package com.jcloisterzone.bugreport;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.ui.SavegameFileFilter;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.TransformerException;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BugReportPanel extends JPanel {
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private ReportingTool reportingTool;
    private JDialog parent;

    public BugReportPanel() {
        setLayout(new MigLayout("", "[grow]", "[][grow,fill][][]"));


        JLabel lblNewLabel = new JLabel("Please describe bug...");
        add(lblNewLabel, "cell 0 0,grow");

        final JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(textArea, "cell 0 1,grow");

        JButton download = new JButton("Download report");
        download.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectFile(textArea.getText());
            }
        });

        JLabel lblThenDownload = new JLabel("... then download report file and send via email to farin@farin.cz");
        add(lblThenDownload, "cell 0 2");
        add(download, "cell 0 3");
    }

    public void selectFile(String description) {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle(_("Report bug"));
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileFilter(new ReportFileFilter());
        fc.setLocale(getLocale());
        fc.setSelectedFile(new File("report.zip"));
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
                if (!file.getName().endsWith(".zip")) {
                    file = new File(file.getAbsolutePath() + ".zip");
                }
                try {
                    reportingTool.createReport(new FileOutputStream(file), description);
                    parent.dispose();
                } catch (Exception ex) {
                    logger.error("Bug report failed", ex);
                    JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), _("Bug report failed"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public ReportingTool getReportingTool() {
        return reportingTool;
    }

    public void setReportingTool(ReportingTool reportingTool) {
        this.reportingTool = reportingTool;
    }

    public void setParent(JDialog parent) {
        this.parent = parent;
    }

    public class ReportFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".zip") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return _("Bug reports");
        }


    }

}
