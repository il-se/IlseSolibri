package ilse.solibri.rules;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.*;

class ParametricSeverityClashDetectionRuleUIDefinition {
	private final ParametricSeverityClashDetectionRule ruleInstance;
	private final UIContainer uiDefinition;

	public ParametricSeverityClashDetectionRuleUIDefinition(ParametricSeverityClashDetectionRule ruleInstance) {
		this.ruleInstance = ruleInstance;
		this.uiDefinition = createUIDefinition();
	}

	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}

	private UIContainer createUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create(
				ruleInstance.resources.getString("ILSE.rule.CD1.TITLE"),
				BorderType.LINE);

		uiContainer.addComponent(UILabel.create(
				ruleInstance.resources.getString("ILSE.rule.CD1.DESCRIPTION")));

		UIContainer filterContainer = UIContainerHorizontal.create(ruleInstance.resources.getString("ILSE.filterContainer.TITLE"), BorderType.LINE );
		filterContainer.addComponent(createFirstComponentFilterUIDefinition());
		filterContainer.addComponent(createSecondComponentFilterUIDefinition());
		uiContainer.addComponent(filterContainer);

		for (ParametricSeverityInterval interval : ruleInstance.paramSeverityIntervals)
			uiContainer.addComponent(createParameterUIDefinition(ruleInstance.resources, interval));

		UIContainer resultContainer = UIContainerVertical.create();
		resultContainer.addComponent(UIRuleParameter.create(ruleInstance.paramResultsFileName));
		uiContainer.addComponent(resultContainer);

		return uiContainer;
	}

	private UIComponent createFirstComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(ruleInstance.paramComponentFilter1));
		return uiContainer;
	}

	private UIComponent createSecondComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(ruleInstance.paramComponentFilter2));
		return uiContainer;
	}

	private UIComponent createParameterUIDefinition(RuleResources resources, ParametricSeverityInterval interval) {
		UIContainer uiContainer = UIContainerVertical.create(
				resources.getString(String.format("ILSE.severityContainer.%s.TITLE", interval.severity.name())),
				BorderType.LINE);
		uiContainer.addComponent(UILabel.create(
				resources.getString(String.format("ILSE.severityContainer.%s.DESCRIPTION", interval.severity.name()))));

		uiContainer.addComponent(UIRuleParameter.create(interval.paramLengthRatioThreshold));
		uiContainer.addComponent(UIRuleParameter.create(interval.paramVolumeRatioThreshold));
		return uiContainer;
	}
}
