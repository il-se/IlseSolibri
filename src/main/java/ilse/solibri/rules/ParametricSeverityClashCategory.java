package ilse.solibri.rules;

import com.solibri.smc.api.checking.RuleResources;
import com.solibri.smc.api.checking.Severity;

public enum ParametricSeverityClashCategory {

    general,
    fullExtentIntersection,
    fullComponentIntersection;

    public String getLabel(RuleResources ruleResources, Severity severity) {
        return ruleResources.getString(String.format("ILSE.category.%s.%s", severity.name(), this.name()));
    }

    public static ParametricSeverityClashCategory getCategoryOf(ClashSeverityInstance csi, ParametricSeverityInterval threshold) {
        if (threshold.paramVolumeRatioThreshold.getValue() > (1.0 - csi.candidate.minVolumeRatio))
            // If exceeds upper threshold of volume cut
            return ParametricSeverityClashCategory.fullComponentIntersection;
        else if (threshold.paramLengthRatioThreshold.getValue() > (1.0 - csi.candidate.maxLengthRatio))
            // If exceeds upper threshold of max length ratio
            return ParametricSeverityClashCategory.fullExtentIntersection;
        return ParametricSeverityClashCategory.general;
    }
}
