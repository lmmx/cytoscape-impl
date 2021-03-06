package org.cytoscape.ding.impl.cyannotator.create;

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

import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.LoadImageDialog;

public class ImageAnnotationFactory implements DingAnnotationFactory<ImageAnnotation> {
	private final CustomGraphicsManager customGraphicsManager;

	public ImageAnnotationFactory(CustomGraphicsManager customGraphicsManager) {
		this.customGraphicsManager = customGraphicsManager;
	}

	public JDialog createAnnotationDialog(DGraphView view, Point2D location) {
		return new LoadImageDialog(view, location, customGraphicsManager);
	}

	@Override
	public ImageAnnotation createAnnotation(Class<? extends ImageAnnotation> clazz, CyNetworkView view, Map<String, String> argMap) {
		if (!(view instanceof DGraphView))
			return null;

		DGraphView dView = (DGraphView) view;
		if (ImageAnnotation.class.equals(clazz)) {
			ImageAnnotationImpl a = new ImageAnnotationImpl(dView.getCyAnnotator(), dView, argMap, customGraphicsManager);
			a.update();
			return (ImageAnnotation)a;
		} else 
			return null;
	}
}
