package cytoscape.genomespace;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class SaveSessionToGenomeSpace extends AbstractCyAction {
	private static final long serialVersionUID = 9988760123456789L;
	private static final Logger logger = LoggerFactory.getLogger(UploadFileToGenomeSpace.class);
	private final DialogTaskManager dialogTaskManager;
	private final SaveSessionAsTaskFactory saveSessionAsTaskFactory;
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	
	public SaveSessionToGenomeSpace(DialogTaskManager dialogTaskManager, SaveSessionAsTaskFactory saveSessionAsTaskFactory, GSUtils gsUtils, JFrame frame) {
		// Give your action a name here
		super("Save Session As");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.Export.GenomeSpace");
		this.dialogTaskManager = dialogTaskManager;
		this.saveSessionAsTaskFactory = saveSessionAsTaskFactory;
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			final GsSession client = gsUtils.getSession();
			final DataManagerClient dataManagerClient = client.getDataManagerClient();

			final List<String> acceptableExtensions = new ArrayList<String>();
			acceptableExtensions.add("cys");
			System.out.println("Before dialog");
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							acceptableExtensions,
							GSFileBrowserDialog.DialogType.SAVE_AS_DIALOG);
			System.out.println("After dialog");
			String saveFileName = dialog.getSaveFileName();
			if (saveFileName == null)
				return;

			// Make sure the file name ends with ".cys":
			if (!saveFileName.toLowerCase().endsWith(".cys"))
				saveFileName += ".cys";

			// Create Task
			final File tempFile = File.createTempFile("temp", "cysession");
			dialogTaskManager.execute(saveSessionAsTaskFactory.createTaskIterator(tempFile));
			System.out.println("before save");
			GSFileMetadata uploadedFileMetadata = dataManagerClient.uploadFile(tempFile, 
                   gsUtils.dirName(saveFileName),
                    gsUtils.baseName(saveFileName));
			System.out.println("After save");
			
			tempFile.delete();
		} catch (final Exception ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}
}
