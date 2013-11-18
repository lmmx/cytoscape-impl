package org.cytoscape.search.internal.ui;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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


import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.EnhancedSearch;
import org.cytoscape.search.internal.SearchTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedSearchPanel extends JPanel {

	private static final long serialVersionUID = 3748296514173533886L;
	
	private static final Logger logger = LoggerFactory.getLogger(EnhancedSearchPanel.class);
	
	private static final Font MESSAGE_FONT = new Font("SansSerif", Font.ITALIC, 10);
	private static final Font REGULAR_FONT = new Font("SansSerif", Font.BOLD, 12);
	
	private boolean firstSearch = true;
	
	private final CyApplicationManager appManager;
	private final EnhancedSearch searchMgr;
	private final DialogTaskManager taskMgr;
	
	private final CyNetworkViewManager viewManager;
	
	private JTextField tfSearchText;

	/** Creates new form NewJPanel */
	public EnhancedSearchPanel(final CyApplicationManager appManager, final CyNetworkViewManager viewManager,
			final EnhancedSearch searchMgr, final DialogTaskManager taskMgr) {
		
		this.appManager = appManager;
		this.searchMgr = searchMgr;
		this.taskMgr = taskMgr;
		this.viewManager = viewManager;
		
		initComponents();
	}


	private void tfSearchTextActionPerformed(java.awt.event.ActionEvent evt) {
		doSearching();
	}

	// Do searching based on the query string from user on text-field
	private void doSearching() {
		final String queryStr = this.tfSearchText.getText().trim();
		
		// Ignore if the search term is empty
		if(queryStr == null || queryStr.length() == 0)
			return;
		
		logger.info("Search Start.  Query text = " + queryStr);

		final CyNetwork currentNetwork = appManager.getCurrentNetwork();
		if (currentNetwork != null) {
			logger.debug("Target Network ID = " + currentNetwork.getSUID());

			final SearchTaskFactory factory = new SearchTaskFactory(searchMgr,
					queryStr, viewManager, appManager);
			this.taskMgr.execute(factory.createTaskIterator(currentNetwork));
		} else
			logger.error("Could not find network for search");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// @SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		tfSearchText = new JTextField();

		tfSearchText.setToolTipText("<html>Example Search Queries:<br><br>YL* -- Search all columns<br>name:YL* -- Search 'name' column<br>GO\\:1232 -- Escape special characters with backslash</html>");
		tfSearchText.setName("tfSearchText"); // NOI18N
		tfSearchText.setFont(MESSAGE_FONT);
		tfSearchText.setText("Enter search term...");
		tfSearchText.setPreferredSize(new java.awt.Dimension(150, 32));
		tfSearchText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				tfSearchTextActionPerformed(evt);
			}
		});
		tfSearchText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				if(firstSearch) {
					tfSearchText.setText("");
					tfSearchText.setFont(REGULAR_FONT);
					firstSearch = false;
				}
			}
		});
		add(tfSearchText);
	}// </editor-fold>
}
