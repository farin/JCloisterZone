package com.jcloisterzone.ui;

import com.jcloisterzone.bugreport.ReportingTool;



/**
 * Active panel (in future it can be "tab")  with game / channel
 */
public interface Activity {

    void undo();

    void toggleRecentHistory(boolean show);
    void setShowFarmHints(boolean showFarmHints);
    void zoom(double steps);

    ReportingTool getReportingTool();

}
