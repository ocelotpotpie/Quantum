package dev.kscott.quantum.location.locator;

import org.bukkit.ChunkSnapshot;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The YLocator is used to locate Y values for a Quantum ruleset
 */
public interface YLocator {

    /**
     * Locates a valid Y value.
     * @param snapshot The snapshot of the chunk to look for
     * @param x the X coordinate (relative to chunk, 0-15)
     * @param z the Z coordinate (relative to chunk, 0-15)
     * @return a valid Y value
     */
    int getValidY(final @NonNull ChunkSnapshot snapshot, final int x, final int z);

}
