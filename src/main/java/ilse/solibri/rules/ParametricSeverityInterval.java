package ilse.solibri.rules;

import com.solibri.smc.api.checking.DoubleParameter;
import com.solibri.smc.api.checking.RuleParameters;
import com.solibri.smc.api.checking.Severity;
import com.solibri.smc.api.model.PropertyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParametricSeverityInterval {
    final public Severity severity;
    final public DoubleParameter paramLengthRatioThreshold;
    final public DoubleParameter paramVolumeRatioThreshold;

    public ParametricSeverityInterval(Severity severity, DoubleParameter paramLengthRatioThreshold, DoubleParameter paramVolumeRatioThreshold) {
        this.severity = severity;
        this.paramLengthRatioThreshold = paramLengthRatioThreshold;
        this.paramVolumeRatioThreshold = paramVolumeRatioThreshold;
    }

    public static List<ParametricSeverityInterval> fromResources(RuleParameters params, Severity ... severity) {
        List<ParametricSeverityInterval> severityIntervals = new ArrayList<>();
        for (Severity s : severity) {
            severityIntervals.add(
                    new ParametricSeverityInterval(
                            s, params.createDouble("ILSE.lengthRatio." + s.name(), PropertyType.PERCENTAGE),
                            params.createDouble("ILSE.volumeRatio." + s.name(), PropertyType.PERCENTAGE))
            );
        }
        return Collections.unmodifiableList(severityIntervals);
    }

    public boolean isPassing(ClashCandidate candidate) {
        return candidate.minLengthRatio < paramLengthRatioThreshold.getValue()
                && candidate.minVolumeRatio < paramVolumeRatioThreshold.getValue();
    }
}
