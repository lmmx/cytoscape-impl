package org.cytoscape.ding.impl.cyannotator.tasks;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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



import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.GroupAnnotationImpl;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupAnnotationsTask extends AbstractNetworkViewTask {

	private static final Logger logger = LoggerFactory.getLogger(GroupAnnotationsTask.class);
	
	
	public GroupAnnotationsTask(CyNetworkView view) {
		super(view);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( view instanceof DGraphView ) {
			DGraphView dView = (DGraphView) view;
			CyAnnotator cyAnnotator = dView.getCyAnnotator();
			GroupAnnotationImpl group = new GroupAnnotationImpl(cyAnnotator, dView);
			group.addComponent(null); // Need to add this first so we can update things appropriately
			cyAnnotator.addAnnotation(group);

			// Now, add all of the children
			for (DingAnnotation child: new ArrayList<DingAnnotation>(cyAnnotator.getSelectedAnnotations())) {
				group.addMember(child);
				child.setSelected(false);
			}

			// Finally, set ourselves to be the selected component
			group.setSelected(true);
			group.update();
		}
	}
}
