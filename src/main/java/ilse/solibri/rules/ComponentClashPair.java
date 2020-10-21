package ilse.solibri.rules;

import com.solibri.smc.api.model.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentClashPair {

    public final Component component1, component2;
    public final double minVolume, maxVolume, maxLength, minLength;

    private ComponentClashPair(Component c1, Component c2) {
        this.component1 = c1;
        this.component2 = c2;

        double v1 = c1.getTriangleMesh().getVolume();
        double v2 = c2.getTriangleMesh().getVolume();
        this.minVolume = Math.min(v1, v2);
        this.maxVolume = Math.max(v1, v2);

        List<Double> extents = Stream.of(c1, c2)
                .map(Component::getBoundingBox)
                .flatMap(o -> Stream.of(o.getSizeX(), o.getSizeY(), o.getSizeZ()))
                .collect(Collectors.toList());

        this.minLength = extents.stream().min(Double::compareTo).get();
        this.maxLength = extents.stream().max(Double::compareTo).get();
    }

    private Stream<ClashCandidate> getClashCandidatesFromIntersection() {
        return component1.getIntersections(component2).stream().map(i -> new ClashCandidate(this, i));
    }

    /**
     * Returns a stream of clash candidates which hit the given threshold ratios.
     * @param component The source component.
     * @param candidates The candidate components.
     * @return A stream of clash candidates
     */
    public static Stream<ClashCandidate> fromComponents(Component component, Stream<Component> candidates, Set<ComponentClashPair> set) {
        return candidates
                .filter(other -> !component.equals(other))
                .map(other -> new ComponentClashPair(component, other))
                .filter(set::add)
                .flatMap(ComponentClashPair::getClashCandidatesFromIntersection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentClashPair that = (ComponentClashPair) o;
        return (component1.equals(that.component2) && component2.equals(that.component1)) // either twisted
                || (component1.equals(that.component1) && component2.equals(that.component2)); // or paired the same way
    }

    @Override
    public int hashCode() {
        // Greater hashcode first to have a sequence rule
        if (component1.hashCode() > component2.hashCode()) {
            return Objects.hash(component1, component2);
        } else {
            return Objects.hash(component2, component1);
        }
    }
}
