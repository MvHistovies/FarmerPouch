package com.mrhistories.farmerpouch.util;

public enum RegionMode {
    NONE,
    WORLDGUARD_REGION,
    WORLD_AND_AREA;

    public static RegionMode fromConfig(String raw) {
        if (raw == null) return NONE;
        try {
            return RegionMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return NONE;
        }
    }
}
