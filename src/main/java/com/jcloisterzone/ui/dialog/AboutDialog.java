package com.jcloisterzone.ui.dialog;

import static com.jcloisterzone.ui.I18nUtils._tr;

import java.awt.Font;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.jcloisterzone.Application;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.component.MultiLineLabel;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;


public class AboutDialog extends JDialog {

    private static final long serialVersionUID = 4697784648983290492L;

    private final JPanel contentPanel = new ThemedJPanel();

    /**
     * Create the dialog.
     */
    public AboutDialog(Client client, File configLocation) {
        setTitle(_tr("About application"));
        UiUtils.centerDialog(this, 460, 230);
        contentPanel.setBounds(0, 0, 444, 214);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel);
        contentPanel.setLayout(null);

        JLabel header = new ThemedJLabel("JCloisterZone");
        header.setBounds(167, 11, 235, 43);
        header.setFont(new Font(null, Font.BOLD, 27));
        contentPanel.add(header);

        JLabel icon = new ThemedJLabel("");
        icon.setIcon(new ImageIcon(AboutDialog.class.getResource("/sysimages/ico.png")));
        icon.setBounds(10, 11, 119, 120);
        contentPanel.add(icon);

        JLabel url = new ThemedJLabel("http://www.jcloisterzone.com/");
        url.setBounds(167, 46, 235, 26);
        contentPanel.add(url);

        JLabel version = new ThemedJLabel(_tr("Version") + ": " + Application.VERSION + " (" + Application.BUILD_DATE + ")");
        version.setBounds(167, 77, 235, 21);
        contentPanel.add(version);

        MultiLineLabel license = new MultiLineLabel(_tr("Distributed under the terms of GNU Affero General Public License version 3"));
        license.setBounds(167, 123, 235, 37);
        license.setRows(3);
        contentPanel.add(license);

        JLabel lblAuthor = new ThemedJLabel("Roman Krejčík <farin@farin.cz>");
        lblAuthor.setVerticalAlignment(SwingConstants.TOP);
        lblAuthor.setBounds(167, 98, 267, 26);
        contentPanel.add(lblAuthor);

        if (configLocation != null) {
            JLabel lblConfigLoc = new ThemedJLabel("config: " + configLocation.getAbsolutePath());
            lblConfigLoc.setForeground(client.getTheme().getHintColor());
            lblConfigLoc.setVerticalAlignment(SwingConstants.TOP);
            lblConfigLoc.setBounds(5, 170, 400, 26);
            contentPanel.add(lblConfigLoc);
        }

        setResizable(false);
        setVisible(true);
    }
}
