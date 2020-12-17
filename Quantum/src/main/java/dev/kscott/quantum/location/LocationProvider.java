package dev.kscott.quantum.location;

import cloud.commandframework.paper.PaperCommandManager;
import dev.kscott.quantum.rule.AsyncQuantumRule;
import dev.kscott.quantum.rule.SyncQuantumRule;
import dev.kscott.quantum.rule.ruleset.QuantumRuleset;
import dev.kscott.quantum.rule.ruleset.search.SearchArea;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * LocationProvider, the core of Quantum. Generates spawn points, handles spawn rules, etc
 */
public class LocationProvider {

    /**
     * Random reference
     */
    private final @NonNull Random random;

    private final @NonNull PaperCommandManager<CommandSender> commandManager;

    /**
     * Constructs the LocationProvider
     */
    public LocationProvider(final @NonNull PaperCommandManager<CommandSender> commandManager) {
        this.commandManager = commandManager;
        this.random = new Random();
    }

    /**
     * Returns a random spawn location for {@code world}
     *
     * @param quantumRuleset The ruleset to use for this search
     * @return A CompletableFuture<Location>. Will complete when a valid location is found.
     */
    // TODO review this when i wake up
    public @NonNull CompletableFuture<Location> getSpawnLocation(final @NonNull QuantumRuleset quantumRuleset) {

        final @NonNull CompletableFuture<Location> cf = new CompletableFuture<>();

        this.commandManager.taskRecipe()
                .begin(quantumRuleset)
                .synchronous(ruleset -> {
                    // Get the world and constuct the QuantumState
                    final @Nullable World world = Bukkit.getWorld(ruleset.getWorldUuid());

                    if (world == null) {
                        throw new RuntimeException("World cannot be null!");
                    }

                    final @NonNull QuantumLocationState state = new QuantumLocationState();

                    state.setWorld(world);
                    state.setQuantumRuleset(quantumRuleset);

                    return state;
                })
                .asynchronous(state -> {
                    // Do some random generation
                    final @NonNull SearchArea searchArea = state.getQuantumRuleset().getSearchArea();

                    final int x = random.nextInt((searchArea.getMaxX() - searchArea.getMinX()) + 1) + searchArea.getMinX();
                    final int z = random.nextInt((searchArea.getMaxZ() - searchArea.getMinZ()) + 1) + searchArea.getMinZ();

                    state.setX(x);
                    state.setZ(z);

                    return state;
                })
                .synchronous(state -> {
                    // Get the chunk future and add it to the state
                    state.setChunkFuture(state.getWorld().getChunkAtAsync(state.getChunkX(), state.getChunkZ()));

                    return state;
                })
                .asynchronous(state -> {
                    // Get the chunk from the future
                    try {
                        state.setChunk(state.getChunkFuture().get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException("Error getting cf");
                    }

                    return state;
                })
                .synchronous(state -> {
                    // get & set the snapshot
                    state.setSnapshot(state.getChunk().getChunkSnapshot());

                    return state;
                })
                .asynchronous(state -> {
                    // Get the y value and check async rules

                    final int y = state.getQuantumRuleset().getYLocator().getValidY(state.getSnapshot(), state.getRelativeX(), state.getRelativeZ());


                    if (y == -1) {
                        state.setValid(false);
                        return state;
                    }

                    state.setY(y);

                    for (final AsyncQuantumRule rule : state.getQuantumRuleset().getAsyncRules()) {
                        boolean valid = rule.validate(state.getSnapshot(), state.getRelativeX(), y, state.getRelativeZ());

                        state.setValid(valid);

                        if (!valid) {
                            break;
                        }
                    }

                    return state;
                })
                .synchronous(state -> {
                    for (final SyncQuantumRule rule : state.getQuantumRuleset().getSyncRules()) {
                        boolean valid = rule.validate(state.getChunk(), state.getRelativeX(), state.getY(), state.getRelativeZ());

                        state.setValid(valid);

                        if (!valid) {
                            break;
                        }
                    }

                    return state;
                })
                .asynchronous(state -> {
                    if (state.isValid()) {
                        cf.complete(new Location(state.getWorld(), state.getX(), state.getY(), state.getZ()));
                    } else {
                        cf.complete(getSpawnLocation(state.getQuantumRuleset()).join());
                    }
                })
                .execute();

        return cf;
    }
}
