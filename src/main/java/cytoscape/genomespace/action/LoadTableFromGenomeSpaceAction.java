package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;


public class LoadTableFromGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(LoadTableFromGenomeSpaceAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final LoadTableFileTaskFactory loadTableFileTaskFactory;
	private final GenomeSpaceContext gsContext;
	private final JFrame frame;
	
	public LoadTableFromGenomeSpaceAction(DialogTaskManager dialogTaskManager, LoadTableFileTaskFactory loadTableFileTaskFactory, GenomeSpaceContext gsContext, JFrame frame) {
		super("Load Table...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Import.GenomeSpace");
		this.dialogTaskManager = dialogTaskManager;
		this.loadTableFileTaskFactory = loadTableFileTaskFactory;
		this.gsContext = gsContext;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if(!gsContext.loginIfNotAlready()) return;
			final GsSession session = gsContext.getSession(); 
			final DataManagerClient dataManagerClient = session.getDataManagerClient();

			// Select the GenomeSpace file:
			final GSFileBrowserDialog dialog =
					new GSFileBrowserDialog(frame, dataManagerClient,
								GSFileBrowserDialog.DialogType.FILE_SELECTION_DIALOG);
			final GSFileMetadata fileMetadata = dialog.getSelectedFileMetadata();
			if (fileMetadata == null)
				return;

			// Download the GenomeSpace file:
			final String origFileName = fileMetadata.getName();
			final String extension = gsContext.getExtension(origFileName);
			File tempFile = File.createTempFile("tempGS", "." + extension);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(session, fileMetadata, tempFile, true));
			ti.append(loadTableFileTaskFactory.createTaskIterator(tempFile));
			dialogTaskManager.execute(ti);
			dialogTaskManager.execute(new TaskIterator(new DeleteFileTask(tempFile)));
		} catch (Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		} 
	}

}