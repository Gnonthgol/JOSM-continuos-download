// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.continuosDownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

public class DownloadPlugin extends Plugin implements ZoomChangeListener {

    /**
     * The worker that runs all our downloads, it have more threads than {@link Main#worker}.
     */
    public static ExecutorService worker; 
    private static HashMap<String, AbstractDownloadStrategy> strats;
    private Timer timer;
    private TimerTask task;
    private Bounds lastBbox;
    private boolean active;

    /**
     * Constructs a new {@code DownloadPlugin}.
     * @param info plugin info
     */
    public DownloadPlugin(PluginInformation info) {
        super(info);

        // Create a new executor to run our downloads in
        int maxThreads = Main.pref.getInteger("plugin.continuos_download.max_threads", 2);
        worker = new ThreadPoolExecutor(1, maxThreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        
        active = Main.pref.getBoolean("plugin.continuos_download.active_default", true);

        strats = new HashMap<>();
        registerStrat(new SimpleStrategy());
        registerStrat(new BoxStrategy());
        timer = new Timer();
        NavigatableComponent.addZoomChangeListener(this);

        ToggleAction toggle = new ToggleAction();
        JCheckBoxMenuItem menuItem = MainMenu.addWithCheckbox(Main.main.menu.fileMenu, toggle,
                MainMenu.WINDOW_MENU_GROUP.ALWAYS);
        menuItem.setState(active);
        toggle.addButtonModel(menuItem.getModel());
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new DownloadPreference();
    }

    @Override
    public void zoomChanged() {
        if (Main.map == null)
            return;
        MapView mv = Main.map.mapView;
        Bounds bbox = mv.getLatLonBounds(mv.getBounds());

        // Have the user changed view since last time
        if (active && (lastBbox == null || !lastBbox.equals(bbox))) {
            if (task != null) {
                task.cancel();
            }

            // wait 500ms before downloading in case the user is in the middle
            // of a pan/zoom
            task = new Task(bbox);
            timer.schedule(task, Main.pref.getInteger("plugin.continuos_download.wait_time", 500));
            lastBbox = bbox;
        }
    }

    public AbstractDownloadStrategy getStrat() {
        AbstractDownloadStrategy r = strats.get(Main.pref.get("plugin.continuos_download.strategy", "BoxStrategy"));

        if (r == null) {
            r = strats.get("SimpleStrategy");
        }

        return r;
    }

    public void registerStrat(AbstractDownloadStrategy strat) {
        strats.put(strat.getClass().getSimpleName(), strat);
    }

    private class Task extends TimerTask {
        private Bounds bbox;

        public Task(Bounds bbox) {
            this.bbox = bbox;
        }

        @Override
        public void run() {
            if (!active)
                return;
            
            // Do not try to download an area if the user have zoomed far out
            if (bbox.getArea() < Main.pref.getDouble("plugin.continuos_download.max_area", 0.25))
                getStrat().fetch(bbox);
        }
    }

    private class ToggleAction extends JosmAction {

        private transient Collection<ButtonModel> buttonModels;

        public ToggleAction() {
            super(tr("Download OSM data continuously"), "continuous-download",
                    tr("Download map data continuously when paning and zooming."), Shortcut.registerShortcut(
                            "continuosdownload:activate", tr("Toggle the continuous download on/off"), KeyEvent.VK_D,
                            Shortcut.ALT_SHIFT), true, "continuosdownload/activate", true);
            buttonModels = new ArrayList<>();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            active = !active;
            notifySelectedState();
            zoomChanged(); // Trigger a new download
        }

        public void addButtonModel(ButtonModel model) {
            if (model != null && !buttonModels.contains(model)) {
                buttonModels.add(model);
            }
        }

        protected void notifySelectedState() {
            for (ButtonModel model : buttonModels) {
                if (model.isSelected() != active) {
                    model.setSelected(active);
                }
            }
        }
    }

    public static List<String> getStrategies() {
        return new ArrayList<>(strats.keySet());
    }
}
