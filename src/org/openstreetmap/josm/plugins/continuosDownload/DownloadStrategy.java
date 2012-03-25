package org.openstreetmap.josm.plugins.continuosDownload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.PostDownloadHandler;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSource;

public abstract class DownloadStrategy {

    public void fetch(Bounds bbox) {
        Bounds extendedBox = extend(bbox, Main.pref.getDouble("plugin.continuos_download.extra_download", 0.1));
        Collection<Bounds> toFetch = getBoxes(extendedBox,
                Main.pref.getInteger("plugin.continuos_download.max_areas", 4));

        printDebug(extendedBox, toFetch);

        // Try to avoid downloading areas outside the view area unnecessary
        Collection<Bounds> t = toFetch;
        toFetch = new ArrayList<Bounds>(t.size());
        for (Bounds box : t) {
            if (box.intersects(bbox)) {
                toFetch.add(box);
            }
        }

        download(toFetch);
    }

    private void printDebug(Bounds bbox, Collection<Bounds> toFetch) {
        double areaToDownload = 0;
        for (Bounds box : toFetch) {
            areaToDownload += box.getArea();
        }

        double areaDownloaded = 0;
        for (Bounds box : getExisting()) {
            if (box.intersects(bbox))
                areaDownloaded += intersection(box, bbox).getArea();
        }

        double downloadP = (areaToDownload * 100) / (bbox.getArea());
        double downloadedP = (areaDownloaded * 100) / (bbox.getArea());

        System.out.printf("Getting %.1f%% of area, already have %.1f%%, overlap %.1f%%%n", downloadP, downloadedP,
                downloadP + downloadedP - 100);
    }

    private Bounds intersection(Bounds box1, Bounds box2) {
        double minX1 = box1.getMin().getX();
        double maxX1 = box1.getMax().getX();
        double minY1 = box1.getMin().getY();
        double maxY1 = box1.getMax().getY();

        double minX2 = box2.getMin().getX();
        double maxX2 = box2.getMax().getX();
        double minY2 = box2.getMin().getY();
        double maxY2 = box2.getMax().getY();

        double minX = Math.max(minX1, minX2);
        double maxX = Math.min(maxX1, maxX2);
        double minY = Math.max(minY1, minY2);
        double maxY = Math.min(maxY1, maxY2);

        return new Bounds(minY, minX, maxY, maxX);
    }

    protected Collection<Bounds> getExisting() {
        if (Main.map.mapView.getEditLayer() == null)
            return Collections.emptySet();
        ArrayList<Bounds> r = new ArrayList<Bounds>();

        for (DataSource dataSource : Main.map.mapView.getEditLayer().data.dataSources) {
            r.add(dataSource.bounds);
        }

        return r;
    }

    public abstract Collection<Bounds> getBoxes(Bounds bbox, int maxAreas);

    protected void download(Collection<Bounds> bboxes) {
        for (Bounds bbox : bboxes) {
            DownloadOsmTask2 task = new DownloadOsmTask2();
            Future<?> future = task.download(false, bbox, null);
            DownloadPlugin.worker.execute(new PostDownloadHandler(task, future));
        }
    }

    static protected Bounds extend(Bounds bbox, double amount) {
        LatLon min = bbox.getMin();
        LatLon max = bbox.getMax();

        double dLat = Math.abs(max.lat() - min.lat()) * amount;
        double dLon = Math.abs(max.lon() - min.lon()) * amount;

        return new Bounds(min.lat() - dLat, min.lon() - dLon, max.lat() + dLat, max.lon() + dLon);
    }

}
