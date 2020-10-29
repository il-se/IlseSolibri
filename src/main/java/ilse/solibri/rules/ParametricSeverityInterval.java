package ilse.solibri.rules;

import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.Severity;
import com.solibri.smc.api.model.PropertyType;

public class ParametricSeverityInterval extends ParametricThresholds {
    final public Severity severity;

    public ParametricSeverityInterval(Severity severity, DoubleParameter paramLengthRatioThreshold, DoubleParameter paramVolumeRatioThreshold) {
        super(paramLengthRatioThreshold, paramVolumeRatioThreshold);
        this.severity = severity;
    }

    public static ParametricSeverityInterval fromResources(RuleParameters params, Severity severity) {
        return new ParametricSeverityInterval(
                    severity, params.createDouble("ILSE.lengthRatio." + severity.name(), PropertyType.PERCENTAGE),
                    params.createDouble("ILSE.volumeRatio." + severity.name(), PropertyType.PERCENTAGE));
    }

    public boolean isPassing(ClashCandidate candidate) {
        return candidate.minLengthRatio < paramLengthRatioThreshold.getValue()
                && candidate.minVolumeRatio < paramVolumeRatioThreshold.getValue();
    }
}
