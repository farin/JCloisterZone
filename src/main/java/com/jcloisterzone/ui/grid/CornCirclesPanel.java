package com.jcloisterzone.ui.grid;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.FakeComponent;

public class CornCirclesPanel extends FakeComponent {

	public static final int PANEL_WIDTH = 250;

    private static Font FONT_HEADER = new Font(null, Font.BOLD, 18);


	private JButton deploymentOption, removalOption;

	public CornCirclesPanel(Client client) {
		super(client);
	}

	//TODO should be outside this
	public void destroy() {
		this.destroySwingComponents(client.getGridPanel());
		client.getGridPanel().setCornCirclesPanel(null);
	}

	@Override
    public void registerSwingComponents(JComponent parent) {
		int panelX = client.getGridPanel().getWidth()-ControlPanel.PANEL_WIDTH-CornCirclesPanel.PANEL_WIDTH-60;

        deploymentOption = new JButton();
        //deploymentOption.setFont(FONT_BUTTON);
        deploymentOption.setText(_("may deploy follower"));
        deploymentOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.getServer().cornCiclesRemoveOrDeploy(false);
				destroy();
			}
        });
        deploymentOption.setBounds(panelX, 34, ControlPanel.PANEL_WIDTH-10, 30);
        parent.add(deploymentOption);

        removalOption = new JButton();
        removalOption.setText(_("must remove follower"));
        removalOption.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				client.getServer().cornCiclesRemoveOrDeploy(true);
				destroy();
			}
		});

        removalOption.setBounds(panelX, 74, ControlPanel.PANEL_WIDTH-10, 30);
        parent.add(removalOption);
    }

	@Override
    public void destroySwingComponents(JComponent parent) {
		parent.remove(deploymentOption);
		parent.remove(removalOption);
	}

	@Override
	public void paintComponent(Graphics2D g2) {
		super.paintComponent(g2);

		GridPanel gp = client.getGridPanel();
        int h = gp.getHeight();

        g2.setColor(ControlPanel.PANEL_BG_COLOR);
        g2.fillRect(0 , 0, PANEL_WIDTH, h);

        g2.setColor(ControlPanel.HEADER_FONT_COLOR);
        g2.setFont(FONT_HEADER);
        g2.drawString(_("Corn circle"), 20, 24);
	}

}
