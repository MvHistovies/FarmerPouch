package com.mrhistories.farmerpouch.pouch;

public enum PouchRarity {
    POUCH_1,
    POUCH_2,
    POUCH_3;

    public static PouchRarity fromSimple(String s) {
        if (s == null) return null;
        return switch (s.trim()) {
            case "1" -> POUCH_1;
            case "2" -> POUCH_2;
            case "3" -> POUCH_3;
            default -> null;
        };
    }
}
