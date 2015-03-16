package com.byteshaft.namaztime;

public class WidgetGlobals {

    final static String SILENT_INTENT = "com.byteshaft.silent";

    private static final int DUMMY_RINGTONE_MODE = -78;
    private static boolean isPhoneSilent = false;
    private static int ringtoneModeBackup = 0;

    public static boolean isPhoneSilent() {
        return isPhoneSilent;
    }

    public static void setIsPhoneSilent(boolean SILENT) {
        isPhoneSilent = SILENT;
    }

    public static int getRingtoneModeBackup() {
        return ringtoneModeBackup;
    }

    public static void setRingtoneModeBackup(int MODE) {
        ringtoneModeBackup = MODE;
    }

    public static void resetRingtoneBackup() {
        ringtoneModeBackup = DUMMY_RINGTONE_MODE;
    }
}
