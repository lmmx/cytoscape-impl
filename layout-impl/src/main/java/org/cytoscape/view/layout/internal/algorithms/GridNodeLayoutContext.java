package org.cytoscape.view.layout.internal.algorithms;

import java.io.IOException;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class GridNodeLayoutContext implements TunableValidator {
	@Tunable(description="Vertical spacing between nodes")
	public double nodeVerticalSpacing = 40.0;

	@Tunable(description="Horizontal spacing between nodes")
	public double nodeHorizontalSpacing = 80.0;

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		if (nodeVerticalSpacing != 30.0 )
			return ValidationState.OK;
		else {
			try {
				errMsg.append("This is a test : I don't want 30.0 for nodeVerticalSpacing value\nProvide something else!!!!");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
	}
}
