package org.openstreetmap.josm.plugins.continuosDownload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

public class DownloadPlugin extends Plugin implements ZoomChangeListener {

    public static ExecutorService worker; // The worker that runs all our
                                          // downloads, it have more threads
                                          // than Main.worker
    private HashMap<String, DownloadStrategy> strats;
    private Timer timer;
    private TimerTask task;
    private Bounds lastBbox = null;
    private boolean active;

    public DownloadPlugin(PluginInformation info) {
        super(info);

        // Create a new executor to run our downloads in
        int max_threads = Main.pref.getInteger("plugin.continuos_download.max_threads", 2);
        worker = new ThreadPoolExecutor(1, max_threads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        
        active = Main.pref.getBoolean("plugin.continuos_download.active_default", true);

        strats = new HashMap<String, DownloadStrategy>();
        registerStrat(new SimpleStrategy());
        registerStrat(new BoxStrategy());
        timer = new Timer();
        NavigatableComponent.addZoomChangeListener(this);

        MainMenu.add(Main.main.menu.fileMenu, new ToggleAction());
    }

    @Override
    public void zoomChanged() {
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

    public DownloadStrategy getStrat() {
        DownloadStrategy r = strats.get(Main.pref.get("plugin.continuos_download.strategy", "BoxStrategy"));

        if (r == null) {
            r = strats.get("SimpleStrategy");
        }

        return r;
    }

    public void registerStrat(DownloadStrategy strat) {
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

            LatLon min = bbox.getMin();
            LatLon max = bbox.getMax();

            // Extend the area to download beond the view
            double t = Main.pref.getDouble("plugin.continuos_download.extra_download", 0.1);
            double dLat = Math.abs(max.lat() - min.lat()) * t;
            double dLon = Math.abs(max.lon() - min.lon()) * t;

            Bounds newBbox = new Bounds(min.lat() - dLat, min.lon() - dLon,
                    max.lat() + dLat, max.lon() + dLon);
            
            // Do not try to download an area if the user have zoomed far out
            if (newBbox.getArea() < Main.pref.getDouble("plugin.continuos_download.max_area", 0.25))
                getStrat().fetch(newBbox);
        }
    }

    private class ToggleAction extends JosmAction {

        public ToggleAction() {
            super(tr("Download OSM data continuosly"), "images/continous-download",
                    tr("Download map data continuosly when paning and zooming."), Shortcut.registerShortcut(
                            "continuosdownload:activate", tr("Toggle the continuos download on/off"), KeyEvent.VK_D,
                            Shortcut.ALT_SHIFT), true, "continuosdownload/activate", true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            active = !active;
            zoomChanged(); // Trigger a new download
        }

    }

}
