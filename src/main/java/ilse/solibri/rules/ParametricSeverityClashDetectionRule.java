package ilse.solibri.rules;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

    final private RuleParameters params = RuleParameters.of(this);

    final RuleResources resources = RuleResources.of(this);

    final FilterParameter paramComponentFilter1 = this.getDefaultFilterParameter();
    final FilterParameter paramComponentFilter2 = params.createFilter(PARAM_COMPONENT_FILTER_TARGET);
    final StringParameter paramResultsFileName = this.params.createString(PARAM_RESULT_FILENAME);
    final ParametricSeverityInterval paramIntervalPassing = ParametricSeverityInterval.fromResources(params, Severity.PASSED);
    final ParametricSeverityInterval paramIntervalLow = ParametricSeverityInterval.fromResources(params, Severity.LOW);
    final ParametricSeverityInterval paramIntervalModerate = ParametricSeverityInterval.fromResources(params, Severity.MODERATE);
    final ParametricSeverityInterval paramIntervalCritical = ParametricSeverityInterval.fromResources(params, Severity.CRITICAL);
    final List<ParametricSeverityInterval> paramSeverityIntervals = Arrays.asList(
            paramIntervalPassing, paramIntervalLow, paramIntervalModerate, paramIntervalCritical);

    final ParametricSeverityClashDetectionRuleUIDefinition uiDefinition = new ParametricSeverityClashDetectionRuleUIDefinition(this);

    final private double transparency = 0.5;
    final private Logger log;

    // Per check session
    private BufferedWriter sessionCsvWriter;
    private Set<ComponentClashPair> sessionRuleComponentPairs;
    private Map<String, ResultCategory> sessionResultCategories;

    public ParametricSeverityClashDetectionRule() {
        log = LoggerFactory.getILoggerFactory().getLogger(getClass().getCanonicalName());
    }

    @Override
    public PreCheckResult preCheck() {
        String fileName = paramResultsFileName.getValue();
        if (null != fileName && !fileName.trim().isEmpty()) {
            try {
                sessionCsvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.ISO_8859_1));
                //csvWriter = new BufferedWriter(new FileWriter(fileName));
                log.info("Will create result file '{}'.",fileName);
                writeCsvHeader(sessionCsvWriter);
            } catch (Throwable e) {
                log.error("Unable to create result file '{}': {}", fileName, e.getMessage());
            }
        }

        sessionRuleComponentPairs = new HashSet<>();
        sessionResultCategories = new HashMap<>();

        log.info("Successfully finished pre-check stage.");
        return super.preCheck();
    }

    @Override
    public void postCheck() {
        log.info("Checked {} candidate clash component pairs.", sessionRuleComponentPairs.size());
        List<Component> components = sessionRuleComponentPairs.stream().flatMap(p -> Stream.of(p.component1, p.component2)).sorted((a, b) -> a.getGUID().compareTo(b.getGUID())).collect(Collectors.toList());
        Map<Integer, List<Component>> map = components.stream().collect(Collectors.groupingBy(Component::hashCode));

        // Close report CSV table, if existing
        try {
            if (null != sessionCsvWriter)
                sessionCsvWriter.close();

            log.info("Successfully finished post-check stage.");
        } catch (IOException e) {
            log.error(e.toString());
        }
        finally {
            sessionRuleComponentPairs = null;
            sessionResultCategories = null;
            sessionCsvWriter = null;
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
                .fromComponents(component, targets.stream(), sessionRuleComponentPairs)
                .collect(Collectors.toList());

        if (null != sessionCsvWriter) {
            try {
                for (ClashCandidate candidate : candidates)
                    writeCsvLine(sessionCsvWriter, candidate);

            } catch (IOException e) {
                log.error("Caught exception while writing to result file: {}", e.getMessage());
            }
        }

        return candidates.stream()
                .map(c -> ClashSeverityInstance.findMostCritical(paramSeverityIntervals, c))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(i -> getResultFromCandidate(i, resultFactory, sessionResultCategories))
                .collect(Collectors.toList());
    }

    @Override
    public UIContainer getParametersUIDefinition() {
        return uiDefinition.getDefinitionContainer();
    }

    private Result getResultFromCandidate(ClashSeverityInstance instance,
                                          ResultFactory resultFactory, Map<String, ResultCategory> categoryMap)
    {
        ComponentClashPair cp = instance.candidate.getClashPair();
        String description = String.format(resources.getString("ILSE.resultDescriptionPattern"),
                cp.component1.getName(), cp.component1.getDisciplineName().orElse("no discipline"),
                cp.component2.getName(), cp.component2.getDisciplineName().orElse("no discipline"));

        String name = String.format(resources.getString("ILSE.resultNamePattern"),
                cp.component1.getName(), cp.component2.getName());

        // Use generally for category passing least thresholds
        ParametricSeverityClashCategory category = ParametricSeverityClashCategory.getCategoryOf(instance, paramIntervalPassing);

        String id = String.format("%s.%s", instance.severity.name(), category.name());
        ResultCategory resultCategory = categoryMap.computeIfAbsent(id, (s) -> {
            String label = category.getLabel(resources, instance.severity);
            log.info("Adding new result category '{}' with ID '{}'.", label, id);
            return resultFactory.createCategory(id, label);
        });

        return resultFactory
            .create(name, description, resultCategory)
            .withInvolvedComponent(cp.component2)
            .withSeverity(instance.severity)
            .withVisualization(visualization -> {
                visualization.addComponent(cp.component1, transparency);
                visualization.addComponent(cp.component2, transparency);
            });
    }

    private void writeCsvHeader(BufferedWriter writer) throws IOException {
        writer.write("GUID1; GUID2; min volume; max volume; min length; max length; cut min length; cut max length; cut volume");
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
                Double.toString(candidate.volume)).collect(Collectors.joining("; ")));
        writer.newLine();
    }
}
