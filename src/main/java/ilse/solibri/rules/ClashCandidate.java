package ilse.solibri.rules;

import com.solibri.smc.api.intersection.Intersection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClashCandidate {
    private final ComponentClashPair componentClashPair;
    public final double minLength, maxLength, minLengthRatio, maxLengthRatio, minVolumeRatio, volume;

    ClashCandidate(ComponentClashPair componentClashPair, Intersection intersection) {
        this.componentClashPair = componentClashPair;
        List<Double> extents = Stream.of(intersection.getSizeX(), intersection.getSizeY(), intersection.getSizeZ())
                .collect(Collectors.toList());
        this.minLength = extents.stream().min(Double::compareTo).get();
        this.maxLength = extents.stream().max(Double::compareTo).get();

        this.minLengthRatio = minLength / componentClashPair.minLength;
        this.maxLengthRatio = maxLength / componentClashPair.maxLength;

        this.volume = intersection.getVolume();
        this.minVolumeRatio = volume / componentClashPair.minVolume;
    }

    public ComponentClashPair getClashPair() {
        return componentClashPair;
    }
}
