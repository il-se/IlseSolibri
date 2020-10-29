package ilse.solibri.rules;

import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.model.PropertyType;

public class ParametricThresholds {
    final public DoubleParameter paramLengthRatioThreshold;
    final public DoubleParameter paramVolumeRatioThreshold;

    public ParametricThresholds(DoubleParameter paramLengthRatioThreshold, DoubleParameter paramVolumeRatioThreshold)
    {
        this.paramLengthRatioThreshold = paramLengthRatioThreshold;
        this.paramVolumeRatioThreshold = paramVolumeRatioThreshold;
    }

    public static ParametricThresholds fromResources(RuleParameters params, String baseIdentifier) {
        return new ParametricThresholds(
                params.createDouble(String.format("%s.lengthRatio", baseIdentifier), PropertyType.PERCENTAGE),
                params.createDouble(String.format("%s.volumeRatio", baseIdentifier), PropertyType.PERCENTAGE));
    }

    public boolean isPassing(ClashCandidate candidate) {
        return candidate.minLengthRatio < paramLengthRatioThreshold.getValue()
                && candidate.minVolumeRatio < paramVolumeRatioThreshold.getValue();
    }
}
