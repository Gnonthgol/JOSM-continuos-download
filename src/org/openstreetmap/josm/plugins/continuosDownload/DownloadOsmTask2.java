package org.openstreetmap.josm.plugins.continuosDownload;

import java.util.concurrent.Future;

import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;

/**
 * This is a copy of the DownloadOsmTask that does not change the view after the area is downloaded.
 * It still displays modal windows and ugly dialog boxes :(
 */
public class DownloadOsmTask2 extends DownloadOsmTask {

    public DownloadOsmTask2() {
    	warnAboutEmptyArea = false;
	}

	@Override
    public Future<?> download(OsmServerReader reader, boolean newLayer, Bounds downloadArea,
            ProgressMonitor progressMonitor) {
        return download(new DownloadTask2(newLayer, reader, progressMonitor), downloadArea);
    }

    protected class DownloadTask2 extends DownloadTask {

        public DownloadTask2(boolean newLayer, OsmServerReader reader,
                ProgressMonitor progressMonitor) {
            super(newLayer, reader, progressMonitor, false);
        }
    }
}
