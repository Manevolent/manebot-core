package com.github.manevolent.jbot.artifact;

import java.net.URI;
import java.util.Collection;

public interface Artifact {

    /**
     * Gets this artifact's identifier.
     * @return artifact identifier.
     */
    ArtifactIdentifier getIdentifier();

    /**
     * Gets this artifact's manifest.
     * @return Artifact manifest.
     */
    ArtifactManifest getManifest();

    /**
     * Finds if this artifact is out-of-date (i.e. a newer version exists)
     * @return true if this artifact is out-of-date, false otherwise.
     */
    default boolean isOutOfDate() {
        return getManifest().doesUpdateExist(getVersion());
    }

    /**
     * Gets this plugin's version.
     * @return Version.
     */
    default String getVersion() {
        return getIdentifier().getVersion();
    }

    /**
     * Gets this artifact's root resource URI.
     * @return Artifact root resource URI.
     */
    URI getUri();

    /**
     * Finds if the Artifact instance has been obtained.
     * @return true if the artifact is obtained, false otherwise.
     */
    boolean hasObtained();

    /**
     * Obtains this artifact locally.
     * @return Local artifact.
     */
    LocalArtifact obtain() throws ArtifactRepositoryException, ArtifactNotFoundException;

    /**
     * Finds the dependencies for this artifact.
     * @return immutable list of this artifact's dependencies.
     */
    Collection<ArtifactDependency> getDependencies();

}
