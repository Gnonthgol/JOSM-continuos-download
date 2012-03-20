package org.openstreetmap.josm.plugins.continuosDownload;

import java.util.concurrent.Future;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.BoundingBoxDownloader;
import org.openstreetmap.josm.io.OsmServerReader;

/*
 * This is a copy of the DownloadOsmTask that does not change the view after the area is downloaded.
 * It still displays modal windows and ugly dialog boxes :(
 */
public class DownloadOsmTask2 extends DownloadOsmTask {

    public Future<?> download(boolean newLayer, Bounds downloadArea,
            ProgressMonitor progressMonitor) {

        downloadTask = new DownloadTask2(newLayer, new BoundingBoxDownloader(
                downloadArea), progressMonitor);
        currentBounds = new Bounds(downloadArea);
        // We need submit instead of execute so we can wait for it to finish and
        // get the error
        // message if necessary. If no one calls getErrorMessage() it just
        // behaves like execute.
        return Main.worker.submit(downloadTask);
    }

    protected class DownloadTask2 extends DownloadTask {

        public DownloadTask2(boolean newLayer, OsmServerReader reader,
                ProgressMonitor progressMonitor) {
            super(newLayer, reader, progressMonitor);
        }

        protected void computeBboxAndCenterScale() {
            BoundingXYVisitor v = new BoundingXYVisitor();
            if (currentBounds != null) {
                v.visit(currentBounds);
            } else {
                v.computeBoundingBox(dataSet.getNodes());
            }

            // Do not change the view as it will trigger another call to update
            // the area and the user would not be able to work. Some say this is
            // an improvement.
            // Main.map.mapView.recalculateCenterScale(v);
        }
    }
}
