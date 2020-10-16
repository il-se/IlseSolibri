package ilse.solibri.rules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.solibri.smc.api.SMC;
import com.solibri.smc.api.checking.*;
import com.solibri.smc.api.filter.AABBIntersectionFilter;
import com.solibri.smc.api.filter.ComponentFilter;
import com.solibri.smc.api.model.Component;
import com.solibri.smc.api.ui.UIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customized clash detection rule which used interval logic to check for
 * clashes.
 */
public class ParametricSeverityClashDetectionRule extends OneByOneRule {

    static final String PARAM_COMPONENT_FILTER_TARGET = "ILSE.componentFilterTarget";
    static final String PARAM_RESULT_FILENAME = "ILSE.resultFileName";

    private final RuleParameters params = RuleParameters.of(this);

    final FilterParameter paramComponentFilter1 = this.getDefaultFilterParameter();
    final FilterParameter paramComponentFilter2 = params.createFilter(PARAM_COMPONENT_FILTER_TARGET);
    final StringParameter paramResultsFileName = this.params.createString(PARAM_RESULT_FILENAME);
    final List<ParametricSeverityInterval> paramSeverityIntervals = ParametricSeverityInterval.fromResources(
            params, Severity.LOW, Severity.MODERATE, Severity.CRITICAL);

    final ParametricSeverityClashDetectionRuleUIDefinition uiDefinition = new ParametricSeverityClashDetectionRuleUIDefinition(this);

    private final double transparency = 0.5;

    private Logger log;
    private BufferedWriter csvWriter;

    public ParametricSeverityClashDetectionRule() {
        log = LoggerFactory.getILoggerFactory().getLogger(getClass().getCanonicalName());
    }

    @Override
    public PreCheckResult preCheck() {
        String fileName = paramResultsFileName.getValue();
        if (null != fileName && !fileName.trim().isEmpty()) {
            try {
                csvWriter = new BufferedWriter(new FileWriter(fileName));
                log.info("Will create result file '" + fileName + "'.");
                writeCsvHeader(csvWriter);
            } catch (Throwable e) {
                log.error("Unable to create result file '" + fileName + "'. Exception: " + e);
            }
        }
        log.info("Successfully finished precheck stage.");
        return super.preCheck();
    }

    @Override
    public void postCheck() {
        try {
            if (null != csvWriter)
                csvWriter.close();

            log.info("Successfully finished postcheck stage.");
        } catch (IOException e) {
            log.error(e.toString());
        }
        finally {
            csvWriter = null;
        }
    }

    @Override
    public Collection<Result> check(Component component, ResultFactory resultFactory) {
        // Find components within potential intersection range
        ComponentFilter secondFilter = paramComponentFilter2.getValue();
        ComponentFilter targetComponentFilter = AABBIntersectionFilter.ofComponentBounds(component).and(secondFilter);
        Collection<Component> targets = SMC.getModel().getComponents(targetComponentFilter);

        // Collect results
        List<ClashCandidate> candidates = ComponentClashPair
                .fromComponents(component, targets.stream())
                .collect(Collectors.toList());

        if (null != csvWriter) {
            try {
                for (ClashCandidate candidate : candidates)
                    writeCsvLine(csvWriter, candidate);

            } catch (IOException e) {
                log.error("Caught exception while writing to result file: " + e);
            }
        }

        return candidates.stream()
                .map(c -> ClashSeverityInstance.findMostCritical(paramSeverityIntervals, c))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(i -> getResultFromCandidate(i, resultFactory))
                .collect(Collectors.toList());
    }

    @Override
    public UIContainer getParametersUIDefinition() {
        return uiDefinition.getDefinitionContainer();
    }

    private Result getResultFromCandidate(ClashSeverityInstance instance, ResultFactory resultFactory)
    {
        ComponentClashPair cp = instance.candidate.getClashPair();
        String description = String.format("%s (%s) intersects %s (%s)",
                cp.component1.getName(), cp.component1.getDisciplineName().orElse("no discipline"),
                cp.component2.getName(), cp.component2.getDisciplineName().orElse("no discipline"));

        String name = String.format("%s intersects %s", cp.component1.getName(), cp.component2.getName());

        return resultFactory
            .create(name, description)
            .withInvolvedComponent(cp.component2)
            .withSeverity(instance.severity)
            .withVisualization(visualization -> {
                visualization.addComponent(cp.component1, transparency);
                visualization.addComponent(cp.component2, transparency);
            });
    }

    private void writeCsvHeader(BufferedWriter writer) throws IOException {
        writer.write("GUID1; GUID2; MinVolume; MaxVolume; MinLength; MaxLength; CutMinLength; CutMaxLength; CutMinVolumeRatio; CutMinLengthRatio");
        writer.newLine();
    }

    private void writeCsvLine(BufferedWriter writer, ClashCandidate candidate) throws IOException {
        ComponentClashPair cp = candidate.getClashPair();
        writer.write(Stream.of(
                cp.component1.getGUID(),
                cp.component2.getGUID(),
                Double.toString(cp.minVolume),
                Double.toString(cp.maxVolume),
                Double.toString(cp.minLength),
                Double.toString(cp.maxLength),
                Double.toString(candidate.minLength),
                Double.toString(candidate.maxLength),
                Double.toString(candidate.minVolumeRatio),
                Double.toString(candidate.minLengthRatio)).collect(Collectors.joining("; ")));
        writer.newLine();
    }
}
