package cytoscape.genomespace.action;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.genomespace.sws.GSLoadEvent;
import org.genomespace.sws.GSLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;

public class LoadNetworkFromURLAction implements GSLoadEventListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromURLAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	private final GSUtils gsUtils;
	
	public LoadNetworkFromURLAction(DialogTaskManager dialogTaskManager, LoadNetworkFileTaskFactory loadNetworkFileTaskFactory, GSUtils gsUtils) {
		this.dialogTaskManager = dialogTaskManager;
		this.loadNetworkFileTaskFactory = loadNetworkFileTaskFactory;
		this.gsUtils = gsUtils;
	}
	
	public void onLoadEvent(GSLoadEvent event) {
		Map<String,String> params = event.getParameters();
		String netURL = params.get("network");
		loadNetwork(netURL);
	}

	public void loadNetwork(String netURL) {
		if ( netURL == null )
			return;

		try {
			DataManagerClient dmc = gsUtils.getSession().getDataManagerClient();
			final String extension = gsUtils.getExtension(netURL);
            GSDataFormat dataFormat = null; 
			if ( extension != null && extension.equalsIgnoreCase("adj") )
				dataFormat = gsUtils.findConversionFormat(gsUtils.getSession().getDataManagerClient().listDataFormats(), "xgmml");
			GSFileMetadata fileMetadata = dmc.getMetadata(new URL(netURL));
			File tempFile = File.createTempFile("tempGS","." + extension);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(gsUtils, fileMetadata, dataFormat, tempFile, true));
			ti.append(loadNetworkFileTaskFactory.createTaskIterator(tempFile));
			dialogTaskManager.execute(ti);
			dialogTaskManager.execute(new TaskIterator(new DeleteFileTask(tempFile)));
		} catch ( Exception e ) { e.printStackTrace(); }
	}
}
