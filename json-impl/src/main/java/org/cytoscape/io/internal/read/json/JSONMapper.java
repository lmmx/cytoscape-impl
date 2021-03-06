package org.cytoscape.io.internal.read.json;

import org.cytoscape.model.CyNetwork;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Create network for given map object generated by Jackson
 *
 */
public interface JSONMapper {
	
	CyNetwork createNetwork(final JsonNode rootNode);

}
