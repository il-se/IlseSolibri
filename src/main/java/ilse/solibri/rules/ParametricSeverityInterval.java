package ilse.solibri.rules;

import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.Severity;
import com.solibri.smc.api.model.PropertyType;

public class ParametricSeverityInterval {
    final public Severity severity;
    final public DoubleParameter paramLengthRatioThreshold;
    final public DoubleParameter paramVolumeRatioThreshold;

    public ParametricSeverityInterval(Severity severity, DoubleParameter paramLengthRatioThreshold, DoubleParameter paramVolumeRatioThreshold) {
        this.severity = severity;
        this.paramLengthRatioThreshold = paramLengthRatioThreshold;
        this.paramVolumeRatioThreshold = paramVolumeRatioThreshold;
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
