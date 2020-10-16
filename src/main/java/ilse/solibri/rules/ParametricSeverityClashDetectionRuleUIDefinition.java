package ilse.solibri.rules;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.ui.BorderType;
import com.solibri.smc.api.ui.UIComponent;
import com.solibri.smc.api.ui.UIContainer;
import com.solibri.smc.api.ui.UIContainerVertical;
import com.solibri.smc.api.ui.UILabel;
import com.solibri.smc.api.ui.UIRuleParameter;

class ParametricSeverityClashDetectionRuleUIDefinition {
	private final ParametricSeverityClashDetectionRule parametricSeverityClashDetectionRule;
	private final UIContainer uiDefinition;

	public ParametricSeverityClashDetectionRuleUIDefinition(ParametricSeverityClashDetectionRule parametricSeverityClashDetectionRule) {
		this.parametricSeverityClashDetectionRule = parametricSeverityClashDetectionRule;
		this.uiDefinition = createUIDefinition();
	}

	public UIContainer getDefinitionContainer() {
		return uiDefinition;
	}

	private UIContainer createUIDefinition() {
		RuleResources resources = RuleResources.of(parametricSeverityClashDetectionRule);
		UIContainer uiContainer = UIContainerVertical.create(
				resources.getString("ILSE.rule.CD1.TITLE"),
				BorderType.LINE);

		uiContainer.addComponent(UILabel.create(
				resources.getString("ILSE.rule.CD1.DESCRIPTION")));

		uiContainer.addComponent(createFirstComponentFilterUIDefinition());
		uiContainer.addComponent(createSecondComponentFilterUIDefinition());

		for (ParametricSeverityInterval interval : parametricSeverityClashDetectionRule.paramSeverityIntervals)
			uiContainer.addComponent(createParameterUIDefinition(resources, interval));

		UIContainer resultContainer = UIContainerVertical.create();
		resultContainer.addComponent(UIRuleParameter.create(parametricSeverityClashDetectionRule.paramResultsFileName));
		uiContainer.addComponent(resultContainer);

		return uiContainer;
	}

	private UIComponent createFirstComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(parametricSeverityClashDetectionRule.paramComponentFilter1));
		return uiContainer;
	}

	private UIComponent createSecondComponentFilterUIDefinition() {
		UIContainer uiContainer = UIContainerVertical.create();
		uiContainer.addComponent(UIRuleParameter.create(parametricSeverityClashDetectionRule.paramComponentFilter2));
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
