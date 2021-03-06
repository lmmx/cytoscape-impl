package org.cytoscape.tableimport.internal;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.work.TunableValidator.ValidationState.OK;

import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;
//import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask;
//import org.cytoscape.task.internal.table.JoinTablesTask;
//import org.cytoscape.task.internal.table.UpdateAddedNetworkAttributes;

public class CombineReaderAndMappingTask extends AbstractTask implements CyNetworkReader, TunableValidator {

	private TaskMonitor taskMonitor;
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Network From Table";
	}	

	@ContainsTunables
	public NetworkCollectionHelper networkCollectionHelperTask;
	
	@ContainsTunables
	public ImportNetworkTableReaderTask importTask;

	
	public CombineReaderAndMappingTask(final InputStream is, final String fileType,
		    final String inputName){
		this.importTask = new ImportNetworkTableReaderTask(is, fileType, inputName);
		this.networkCollectionHelperTask = new NetworkCollectionHelper();
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if ( importTask instanceof TunableValidator ) {
			ValidationState readVS = ((TunableValidator)importTask).getValidationState(errMsg);

			if ( readVS != OK )
				return readVS;
		}
		
		return OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.networkCollectionHelperTask.run(taskMonitor);
		this.importTask.setNodeMap(this.networkCollectionHelperTask.getNodeMap());
		this.importTask.setRootNetwork(this.networkCollectionHelperTask.getRootNetwork());
		this.importTask.run(taskMonitor);
	}

	
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = CytoscapeServices.cyNetworkViewFactory.createNetworkView(network);
		final CyLayoutAlgorithm layout = CytoscapeServices.cyLayouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,"");
		Task nextTask = itr.next();
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		taskMonitor.setProgress(1.0d);
		return view;	
	}

	@Override
	public CyNetwork[] getNetworks() {
		return this.importTask.getNetworks();
	}

}
