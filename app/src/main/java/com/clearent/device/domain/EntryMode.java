package com.clearent.device.domain;

public enum EntryMode {

    FALLBACK_SWIPE(80),
    NONTECH_FALLBACK_SWIPE(95),
    EMV(05),
    CONTACTLESS_EMV(07),
    CONTACTLESS_MAGNETIC_SWIPE(91),
    SWIPE(90),
    INVALID(999);

    private int entryMode;

    EntryMode(int entryMode) {
        this.entryMode = entryMode;
    }

    public static EntryMode valueOfByInt(int entryMode) {
        for (EntryMode entryModeEnum : EntryMode.values()) {
            if (entryModeEnum.entryMode == entryMode) {
                return entryModeEnum;
            }
        }
        return INVALID;
    }

    public int getEntryMode() {
        return entryMode;
    }

}
