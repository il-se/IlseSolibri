package ilse.solibri.rules;

import com.solibri.geometry.mesh.TriangleMesh;
import com.solibri.geometry.primitive3d.AABB3d;
import com.solibri.smc.api.intersection.Intersection;
import com.solibri.smc.api.model.Component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ClashCandidateTest {

    Component component1 = mock(Component.class);
    Component component2 = mock(Component.class);

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void mockUpComponents() {
        AABB3d mockedAabb3d1 = mock(AABB3d.class);
        AABB3d mockedAabb3d2 = mock(AABB3d.class);
        TriangleMesh mockedTriangleMesh1 = mock(TriangleMesh.class);
        TriangleMesh mockedTriangleMesh2 = mock(TriangleMesh.class);
        Intersection mockedIntersection1 = mock(Intersection.class);

        when(mockedAabb3d1.getSizeX()).thenReturn(1.0);
        when(mockedAabb3d1.getSizeY()).thenReturn(2.0);
        when(mockedAabb3d1.getSizeZ()).thenReturn(1.0);
        when(mockedAabb3d2.getSizeX()).thenReturn(2.0);
        when(mockedAabb3d2.getSizeY()).thenReturn(2.0);
        when(mockedAabb3d2.getSizeZ()).thenReturn(2.0);

        when(mockedTriangleMesh1.getVolume()).thenReturn(2.0);
        when(mockedTriangleMesh2.getVolume()).thenReturn(8.0);

        when(mockedIntersection1.getSizeX()).thenReturn(1.0);
        when(mockedIntersection1.getSizeY()).thenReturn(2.0);
        when(mockedIntersection1.getSizeZ()).thenReturn(1.0);
        when(mockedIntersection1.getVolume()).thenReturn(2.0);

        when(component1.getBoundingBox()).thenReturn(mockedAabb3d1);
        when(component2.getBoundingBox()).thenReturn(mockedAabb3d2);
        when(component1.getTriangleMesh()).thenReturn(mockedTriangleMesh1);
        when(component2.getTriangleMesh()).thenReturn(mockedTriangleMesh2);

        when(component1.getIntersections(component2)).thenReturn(Stream.of(mockedIntersection1).collect(Collectors.toSet()));
        when(component2.getIntersections(component1)).thenReturn(Stream.of(mockedIntersection1).collect(Collectors.toSet()));
    }

    @Test
    public void testingPairedClashCandidate() {
        Set<ComponentClashPair> pairs = new HashSet<>();
        List<ClashCandidate> candidates = Stream.of(component1, component2)
                .flatMap(c -> ComponentClashPair.fromComponents(c, Stream.of(component1, component2), pairs))
                .collect(Collectors.toList());

        // Should drop self intersections and variation of the same pair
        assertEquals(1, pairs.size());
        assertEquals(1, candidates.size());

        ClashCandidate candidate = candidates.get(0);
        assertEquals(1.0, candidate.minLength, 1e-5);
        assertEquals(2.0, candidate.maxLength, 1e-5);
        assertEquals(1.0, candidate.getClashPair().minLength, 1e-5);
        assertEquals(2.0, candidate.getClashPair().maxLength, 1e-5);
        assertEquals(2.0, candidate.getClashPair().minVolume, 1e-5);
        assertEquals(8.0, candidate.getClashPair().maxVolume, 1e-5);
        assertEquals(1.0, candidate.minVolumeRatio, 1e-5);
        assertEquals(1.0, candidate.maxLengthRatio, 1e-5);
    }
}
