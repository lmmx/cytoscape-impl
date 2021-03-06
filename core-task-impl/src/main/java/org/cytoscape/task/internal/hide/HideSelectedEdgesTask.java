package org.cytoscape.task.internal.hide;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class HideSelectedEdgesTask extends AbstractNetworkViewTask {
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmMgr;

	public HideSelectedEdgesTask(final UndoSupport undoSupport,
	                             final CyEventHelper eventHelper,
	                             final VisualMappingManager vmMgr,
	                             final CyNetworkView v) {
		super(v);
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		this.vmMgr = vmMgr;
	}

	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		
		final CyNetwork network = view.getModel(); 
		final List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		undoSupport.postEdit(new HideEdit("Hide Selected Edges", view, selectedEdges, false, eventHelper, vmMgr));
		e.setProgress(0.3);
		
		HideUtils.setVisibleEdges(selectedEdges, false, view);
		e.setProgress(0.7);
		
		// vmMgr.getVisualStyle(view).apply(view);
		view.updateView();
		e.setProgress(1.0);
	} 
}
