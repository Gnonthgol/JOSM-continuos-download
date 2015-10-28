package org.openstreetmap.josm.plugins.continuosDownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;

public class DownloadPreference extends DefaultTabPreferenceSetting {

    public DownloadPreference() {
        super("continous-download", tr("Download Settings"), tr("Settings for the continuos download."), true);
    }

    private JCheckBox activeDefault = new JCheckBox(tr("Activate continuos downloads at startup."));
    private JTextField maxThreads = new JTextField(4);
    private JTextField maxAreas = new JTextField(4);
    private JTextField waitTime = new JTextField(6);
    private JTextField extraDownload = new JTextField(4);
    private JTextField maxArea = new JTextField(4);
    private JComboBox<String> strategy = new JComboBox<>();
    private JCheckBox quietDownload = new JCheckBox(tr("Supress the default modal progress monitor when downloading."));

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel panel = new JPanel(new GridBagLayout());

        // activeDefault
        activeDefault.setSelected(Main.pref.getBoolean("plugin.continuos_download.active_default", true));
        activeDefault.setToolTipText(tr("If this plugin is active at startup. This default state will not change when"
                + " you are toggeling the plugin with the menu option."));
        panel.add(activeDefault, GBC.eol().insets(0, 0, 0, 0));
        
        // maxThreads
        maxThreads.setText(Main.pref.get("plugin.continuos_download.max_threads", "2"));
        maxThreads.setToolTipText(tr("Maximum number of threads used for downloading, increasing this will cause the"
                + " client to send more concurrint queries to the server. (Requires restart)"));
        panel.add(new JLabel(tr("Max threads")), GBC.std());
        panel.add(maxThreads, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // maxAreas
        maxAreas.setText(Main.pref.get("plugin.continuos_download.max_areas", "4"));
        maxAreas.setToolTipText(tr("Maximum number of boxes to download for each pan/zoom."));
        panel.add(new JLabel(tr("Max download boxes")), GBC.std());
        panel.add(maxAreas, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // waitTime
        waitTime.setText(Main.pref.get("plugin.continuos_download.wait_time", "500"));
        waitTime.setToolTipText(tr("Time in milliseconds after a pan/zoom before it starts downloading. Additional"
                + " changes in the viewport in the waiting time will reset the timer."));
        panel.add(new JLabel(tr("Wait time (milliseconds)")), GBC.std());
        panel.add(waitTime, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // extraDownload
        extraDownload.setText(Main.pref.get("plugin.continuos_download.extra_download", "0.1"));
        extraDownload.setToolTipText(tr("How much extra area around the viewport is it going to download. Setting this"
                + " to 0 will not download any extra data."));
        panel.add(new JLabel(tr("Extra download area")), GBC.std());
        panel.add(extraDownload, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // maxArea
        maxArea.setText(Main.pref.get("plugin.continuos_download.max_area", "0.25"));
        maxArea.setToolTipText(tr("Max area to download in degrees^2. Increasing this number will cause the"
                + " plugin to download areas when you are zoomed far out."));
        panel.add(new JLabel(tr("Max download area")), GBC.std());
        panel.add(maxArea, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // strategy
        for (String strat : DownloadPlugin.getStrategies()) {
            strategy.addItem(strat);
        }
        strategy.setSelectedItem(Main.pref.get("plugin.continuos_download.strategy", "BoxStrategy"));
        strategy.setToolTipText(tr("The strategy for finding what areas to request from the server."));
        panel.add(new JLabel(tr("Download strategy")), GBC.std());
        panel.add(strategy, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // quietDownload
        quietDownload.setSelected(Main.pref.getBoolean("plugin.continuos_download.quiet_download", false));
        quietDownload.setToolTipText(tr("If we should supress the progress monitor that is shown when downloading. If"
                + " select this option there is no indication that something is being done, and no way to"
                + " cancel the download."));
        panel.add(quietDownload, GBC.eol().insets(0, 0, 0, 0));

        panel.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
        createPreferenceTabWithScrollPane(gui, panel);
    }

    @Override
    public boolean ok() {
        boolean r = !maxThreads.getText().equals(Main.pref.get("plugin.continuos_download.max_threads", "2"));

        Main.pref.put("plugin.continuos_download.active_default", activeDefault.isSelected());
        Main.pref.put("plugin.continuos_download.max_threads", maxThreads.getText());
        Main.pref.put("plugin.continuos_download.max_areas", maxAreas.getText());
        Main.pref.put("plugin.continuos_download.wait_time", waitTime.getText());
        Main.pref.put("plugin.continuos_download.extra_download", extraDownload.getText());
        Main.pref.put("plugin.continuos_download.max_area", maxArea.getText());
        Main.pref.put("plugin.continuos_download.strategy", (String) strategy.getSelectedItem());
        Main.pref.put("plugin.continuos_download.quiet_download", quietDownload.isSelected());
        return r;
    }
}
