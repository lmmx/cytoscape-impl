package org.cytoscape.ding.impl.cyannotator.dialogs;

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


import java.awt.Point;
import java.awt.Robot;
import java.awt.geom.Point2D;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cytoscape.view.presentation.annotations.ArrowAnnotation.AnchorType;
import org.cytoscape.view.presentation.annotations.ArrowAnnotation.ArrowEnd;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;

public class ArrowAnnotationDialog extends JDialog {
	private javax.swing.JButton applyButton;
	private javax.swing.JButton cancelButton;

	private final CyAnnotator cyAnnotator;    
	private final DGraphView view;    
	private final Point2D startingLocation;
	private final ArrowAnnotationImpl mAnnotation;
	private ArrowAnnotationImpl preview;
	private DingAnnotation source = null;
	private final boolean create;
		
	public ArrowAnnotationDialog(DGraphView view, Point2D start) {
		this.view = view;
		this.cyAnnotator = view.getCyAnnotator();
		this.startingLocation = start;
		this.mAnnotation = new ArrowAnnotationImpl(cyAnnotator, view);
		this.source = cyAnnotator.getAnnotationAt(start);
		this.create = true;

		initComponents();		        
	}

	public ArrowAnnotationDialog(ArrowAnnotationImpl mAnnotation) {
		this.mAnnotation=mAnnotation;
		this.cyAnnotator = mAnnotation.getCyAnnotator();
		this.view = cyAnnotator.getView();
		this.create = false;
		this.startingLocation = null;

		initComponents();	
	}
    
	private void initComponents() {
		int ARROW_HEIGHT = 500;
		int ARROW_WIDTH = 500;
		int PREVIEW_WIDTH = 500;
		int PREVIEW_HEIGHT = 120;

		// Create the preview panel
		preview = new ArrowAnnotationImpl(cyAnnotator, view);
		preview.setUsedForPreviews(true);
		preview.getComponent().setLocation(10,10);
		((ArrowAnnotationImpl)preview).setSize(400.0,100.0);
		PreviewPanel previewPanel = new PreviewPanel(preview, PREVIEW_WIDTH, PREVIEW_HEIGHT);

		JPanel arrowAnnotation1 = new ArrowAnnotationPanel(mAnnotation, previewPanel, ARROW_WIDTH, ARROW_HEIGHT);

		applyButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		if (create)
			setTitle("Create Arrow Annotation");
		else
			setTitle("Modify Arrow Annotation");

		setResizable(false);
		getContentPane().setLayout(null);

		getContentPane().add(arrowAnnotation1);
		arrowAnnotation1.setBounds(5, 0, arrowAnnotation1.getWidth(), arrowAnnotation1.getHeight());

		getContentPane().add(previewPanel);
		previewPanel.setBounds(5, arrowAnnotation1.getHeight()+5, PREVIEW_WIDTH, PREVIEW_HEIGHT);

		int y = PREVIEW_HEIGHT+ARROW_HEIGHT+10;

		applyButton.setText("OK");
		applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				applyButtonActionPerformed(evt);
			}
		});
		getContentPane().add(applyButton);
		applyButton.setBounds(350, y+20, applyButton.getPreferredSize().width, 23);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		getContentPane().add(cancelButton);
		cancelButton.setBounds(430, y+20, cancelButton.getPreferredSize().width, 23);

		pack();
		setSize(520, PREVIEW_HEIGHT+ARROW_HEIGHT+80);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalityType(DEFAULT_MODALITY_TYPE);
	}

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();           
		//Apply

		mAnnotation.setLineColor(preview.getLineColor());
		mAnnotation.setLineWidth(preview.getLineWidth());
		mAnnotation.setArrowType(ArrowEnd.SOURCE, preview.getArrowType(ArrowEnd.SOURCE));
		mAnnotation.setArrowColor(ArrowEnd.SOURCE, preview.getArrowColor(ArrowEnd.SOURCE));
		mAnnotation.setArrowSize(ArrowEnd.SOURCE, preview.getArrowSize(ArrowEnd.SOURCE));
		mAnnotation.setAnchorType(ArrowEnd.SOURCE, preview.getAnchorType(ArrowEnd.SOURCE));
		mAnnotation.setArrowType(ArrowEnd.TARGET, preview.getArrowType(ArrowEnd.TARGET));
		mAnnotation.setArrowColor(ArrowEnd.TARGET, preview.getArrowColor(ArrowEnd.TARGET));
		mAnnotation.setArrowSize(ArrowEnd.TARGET, preview.getArrowSize(ArrowEnd.TARGET));
		mAnnotation.setAnchorType(ArrowEnd.TARGET, preview.getAnchorType(ArrowEnd.TARGET));

		if (!create) {
			mAnnotation.update(); 
			return;
		}

		mAnnotation.addComponent(null);
		mAnnotation.setSource(this.source);
		mAnnotation.update();

		// Update the canvas
		view.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS).repaint();

		// Set this shape to be resized
		cyAnnotator.positionArrow(mAnnotation);

		try {
			// Warp the mouse to the starting location (if supported)
			Point start = mAnnotation.getComponent().getLocationOnScreen();
			Robot robot = new Robot();
			robot.mouseMove((int)start.getX()+100, (int)start.getY()+100);
		} catch (Exception e) {}
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//Cancel
		dispose();
	}
}

