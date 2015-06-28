package com.jcloisterzone.bugreport;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jcloisterzone.ui.I18nUtils._;

public class BugReportPanel extends JPanel {
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private ReportingTool reportingTool;
    private JDialog parent;

    public BugReportPanel() {
        setLayout(new MigLayout("insets dialog, gapy unrel", "[grow]", "[][][grow,fill][][]"));

        JLabel headerLabel = new JLabel(_("<html>Bug report tool pack saved game, internal game log and system information to simplify debugging process.</html>"));
        add(headerLabel, "cell 0 0");

        JLabel describeLabel = new JLabel(_("Please describe bug..."));
        add(describeLabel, "cell 0 1,grow");

        final JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(textArea, "cell 0 2,grow");

        JButton downloadButton = new JButton("Download report");
        downloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectFile(textArea.getText());
            }
        });

        JLabel downloadLabel = new JLabel(_("...then download report archive and send via email to farin@farin.cz"));
        add(downloadLabel, "cell 0 3");
        add(downloadButton, "cell 0 4");
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

    public static class ReportFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".zip") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "*.zip";
        }
    }

}
