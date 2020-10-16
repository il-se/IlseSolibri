package ilse.solibri.rules;

import com.solibri.smc.api.checking.Severity;

import java.util.List;
import java.util.Optional;

public class ClashSeverityInstance {
    final public Severity severity;
    final public ClashCandidate candidate;

    ClashSeverityInstance(Severity severity, ClashCandidate candidate) {
        this.severity = severity;
        this.candidate = candidate;
    }

    public static Optional<ClashSeverityInstance> findMostCritical(List<ParametricSeverityInterval> intervals, ClashCandidate candidate) {
        return intervals.stream()
                .filter(i -> !i.isPassing(candidate))
                // CRITICAL first
                .min((i1,i2) -> Integer.compare(i1.severity.ordinal(), i2.severity.ordinal()))
                .map(i -> new ClashSeverityInstance(i.severity, candidate));
    }
}
