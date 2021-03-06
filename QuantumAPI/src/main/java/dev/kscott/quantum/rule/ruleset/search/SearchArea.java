package dev.kscott.quantum.rule.ruleset.search;

public class SearchArea {

    final int minX;

    final int maxX;

    final int minZ;

    final int maxZ;

    public SearchArea(final int minX, final int maxX, final int minZ, final int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

}
