package org.cytoscape.io.internal.read.json;

import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.DATA;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.EDGES;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.ELEMENTS;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.ID;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.NODES;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.POSITION;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.POSITION_X;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.POSITION_Y;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.SOURCE;
import static org.cytoscape.io.internal.write.json.serializer.CytoscapeJsToken.TARGET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Parse Cytoscape.js style map.
 * 
 */
public class CytoscapejsMapper implements JSONMapper {

	private final CyNetworkFactory factory;
	private Map<CyNode, Double[]> positionMap;

	public CytoscapejsMapper(final CyNetworkFactory factory) {
		this.factory = factory;
	}

	@Override
	public CyNetwork createNetwork(final JsonNode rootNode) {

		final JsonNode elements = rootNode.get(ELEMENTS.getTag());
		final JsonNode nodes = elements.get(NODES.getTag());
		final JsonNode edges = elements.get(EDGES.getTag());

		final CyNetwork network = factory.createNetwork();
		// Read network 
		final JsonNode data = rootNode.get(DATA.getTag());
		addTableData(data, network, network);
		
		this.positionMap = new HashMap<CyNode, Double[]>();
		final Map<String, CyNode> nodeMap = this.addNodes(network, nodes);
		this.addEdges(network, edges, nodeMap);

		return network;
	}

	private final Map<String, CyNode> addNodes(final CyNetwork network, final JsonNode nodes) {

		final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

		for (final JsonNode node : nodes) {
			// Extract node properties.
			final JsonNode data = node.get(DATA.getTag());
			final JsonNode nodeId = data.get(ID.getTag());
			CyNode cyNode = nodeMap.get(nodeId.textValue());
			if (cyNode == null) {
				cyNode = network.addNode();

				// Use ID as unique name.
				network.getRow(cyNode).set(CyNetwork.NAME, nodeId.textValue());
				nodeMap.put(nodeId.textValue(), cyNode);
				addTableData(data, cyNode, network);
			}

			// Get (x,y) location if available.
			final JsonNode position = node.get(POSITION.getTag());
			if (position != null) {
				final JsonNode x = position.get(POSITION_X.getTag());
				final JsonNode y = position.get(POSITION_Y.getTag());
				final Double[] positionArray = new Double[2];
				positionArray[0] = x.doubleValue();
				positionArray[1] = y.doubleValue();
				positionMap.put(cyNode, positionArray);
			}
		}
		return nodeMap;
	}


	private final void addEdges(final CyNetwork network, final JsonNode edges, final Map<String, CyNode> nodeMap) {

		for (final JsonNode edge : edges) {
			final JsonNode data = edge.get(DATA.getTag());
			final JsonNode source = data.get(SOURCE.getTag());
			final JsonNode target = data.get(TARGET.getTag());

			final CyNode sourceNode = nodeMap.get(source.textValue());
			final CyNode targetNode = nodeMap.get(target.textValue());
			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			
			// Add edge table data
			addTableData(data, newEdge, network);
		}
	}


	private final void addTableData(final JsonNode data, final CyIdentifiable graphObject, final CyNetwork network) {
		final Iterator<String> fieldNames = data.fieldNames();

		while (fieldNames.hasNext()) {
			final String fieldName = fieldNames.next();
			
			// Ignore unnecessary fields (ID, SUID, SELECTED)
			if (fieldName.equals(CyIdentifiable.SUID) == false
				&& fieldName.equals(CyNetwork.SELECTED) == false) {

				final CyTable table = network.getRow(graphObject).getTable();
				if (table.getColumn(fieldName) == null) {
					// Create new column if necessary
					final Class<?> dataType = getDataType(data.get(fieldName));
					if (dataType == List.class) {
						final Class<?> listDataType = getListDataType(data.get(fieldName));
						table.createListColumn(fieldName, listDataType, false);
					} else {
						table.createColumn(fieldName, dataType, false);
					}
				}

				network.getRow(graphObject).set(fieldName, getValue(data.get(fieldName)));
			}
		}
	}


	private final Class<?> getListDataType(final JsonNode arrayNode) {
		// For empty lists, use as String.
		if(arrayNode.size() == 0) {
			return String.class;
		}
		
		final JsonNode entry = arrayNode.get(0);

		if (entry.isLong()) {
			return Long.class;
		} else if (entry.isBoolean()) {
			return Boolean.class;
		} else if (entry.isInt()) {
			return Integer.class;
		} else if (entry.isFloat()) {
			return Float.class;
		} else if (entry.isDouble()) {
			return Double.class;
		} else {
			return String.class;
		}
	}


	private final Class<?> getDataType(final JsonNode entry) {
		if (entry.isArray()) {
			return List.class;
		} else if (entry.isLong()) {
			return Long.class;
		} else if (entry.isBoolean()) {
			return Boolean.class;
		} else if (entry.isInt()) {
			return Integer.class;
		} else if (entry.isFloat()) {
			return Float.class;
		} else if (entry.isDouble()) {
			return Double.class;
		} else {
			return String.class;
		}
	}

	private final Object getValue(final JsonNode entry) {
		// Check the data is list or not.
		if (entry.isArray()) {
			final Iterator<JsonNode> values = entry.elements();
			final List<Object> list = new ArrayList<Object>();
			while (values.hasNext()) {
				list.add(parseValue(values.next()));
			}
			return list;
		} else {
			return parseValue(entry);
		}
	}


	private final Object parseValue(final JsonNode entry) {
		if (entry.isLong()) {
			return entry.longValue();
		} else if (entry.isInt()) {
			return entry.intValue();
		} else if (entry.isFloat()) {
			return entry.floatValue();
		} else if (entry.isDouble()) {
			return entry.doubleValue();
		} else if (entry.isBoolean()) {
			return entry.booleanValue();
		} else {
			return entry.asText();
		}
	}


	protected Map<CyNode, Double[]> getNodePosition() {
		return this.positionMap;
	}
}
