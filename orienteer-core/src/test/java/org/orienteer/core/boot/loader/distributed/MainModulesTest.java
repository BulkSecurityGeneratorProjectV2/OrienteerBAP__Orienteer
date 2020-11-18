package org.orienteer.core.boot.loader.distributed;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.core.boot.loader.internal.InternalOModuleManager;
import org.orienteer.core.boot.loader.internal.artifact.OArtifact;
import org.orienteer.junit.OrienteerTestRunner;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(OrienteerTestRunner.class)
public class MainModulesTest {

    @Inject
    @Named("artifacts.test")
    private Set<OArtifact> artifacts;

    @Inject
    @Named("orienteer.artifacts.test")
    private Set<OArtifact> orienteerArtifacts;


    @Inject
    @Named("user.artifacts.test")
    private Set<OArtifact> userArtifacts;

    @Inject
    private InternalOModuleManager moduleManager;


    @Test
    public void testGetOrienteerArtifacts() {
        Set<OArtifact> difference = moduleManager.getOrienteerArtifacts(artifacts);
        assertFalse(difference.isEmpty());
        assertFalse(difference.containsAll(userArtifacts));
        assertTrue(difference.containsAll(orienteerArtifacts));
        assertTrue(artifacts.containsAll(difference));

        assertEquals(2, difference.size());
    }

    @Test
    public void testReadOrienteerArtifacts() {
        OArtifact devutils = orienteerArtifacts.iterator().next();
        Set<OArtifact> artifacts = moduleManager.getOrienteerModulesAsSet();
        assertTrue(artifacts.contains(devutils));
    }

    @Test
    public void testGetOrienteerModules() {
        assertTrue(moduleManager.getOrienteerModulesAsSet().size() > 0);
        assertTrue(moduleManager.getOrienteerModules().size() > 0);
    }

    @Test
    public void testHashArtifactCode() {
        OArtifact devutils = orienteerArtifacts.iterator().next();

        Optional<OArtifact> orienteerDevutilsOpt = moduleManager.getOrienteerModulesAsSet()
                .stream()
                .filter(artifact -> artifact.getArtifactReference().getArtifactId().equals("orienteer-devutils"))
                .findFirst();

        assertTrue(orienteerDevutilsOpt.isPresent());

        OArtifact orienteerDevutils = orienteerDevutilsOpt.get();

        assertEquals(devutils.hashCode(), orienteerDevutils.hashCode());
        assertEquals(devutils.getArtifactReference().hashCode(), orienteerDevutils.getArtifactReference().hashCode());
        assertEquals(devutils, orienteerDevutils);
    }

}
