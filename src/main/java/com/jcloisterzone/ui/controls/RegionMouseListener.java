package com.jcloisterzone.ui.controls;

import java.awt.event.MouseEvent;

public interface RegionMouseListener {

    public void mouseClicked(MouseEvent e, MouseListeningRegion origin);
    public void mouseEntered(MouseEvent e, MouseListeningRegion origin);
    public void mouseExited(MouseEvent e, MouseListeningRegion origin);

}