package dev.kscott.quantum.rule.rules.async;

import dev.kscott.quantum.rule.QuantumRule;
import dev.kscott.quantum.rule.option.QuantumRuleOption;
import org.bukkit.ChunkSnapshot;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class AsyncQuantumRule extends QuantumRule {

    public AsyncQuantumRule(final @NonNull QuantumRuleOption<?>... options) {
        super(options);
    }

        /**
         * Checks the location against this rule and returns it's validity as a boolean.
         *
         * @param snapshot snapshot of the chunk
         * @param x        x coordinate, relative to chunk (0-15)
         * @param y        y coordinate (0-255)
         * @param z        z coordinate, relative to chunk (0-15)
         * @return true if valid, false if not
         */
    public abstract boolean validate(final @NonNull ChunkSnapshot snapshot, final int x, final int y, final int z);

}
