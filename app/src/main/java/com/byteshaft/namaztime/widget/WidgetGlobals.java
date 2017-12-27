/*
 *
 *  * (C) Copyright 2015 byteShaft Inc.
 *  *
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser General Public License
 *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl-2.1.html
 *  
 */

package com.byteshaft.namaztime.widget;

public class WidgetGlobals {

    public static final String SILENT_INTENT = "com.byteshaft.silent";

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
