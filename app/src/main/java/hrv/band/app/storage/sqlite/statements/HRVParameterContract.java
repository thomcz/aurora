package hrv.band.app.storage.sqlite.statements;

import android.provider.BaseColumns;

/**
 * Copyright (c) 2017
 * Created by Julian Martin on 23.06.2016.
 */
public class HRVParameterContract {

    /**
     * Private constructor to prevent object creation
     */
    private HRVParameterContract() { }

    public abstract static class HRVParameterEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "HRVEntry";
        public static final String COLUMN_NAME_ENTRY_ID = "id";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_SD1 = "sd1";
        public static final String COLUMN_NAME_SD2 = "sd2";
        public static final String COLUMN_NAME_LF = "lf";
        public static final String COLUMN_NAME_HF = "hf";
        public static final String COLUMN_NAME_RMSSD = "rmssd";
        public static final String COLUMN_NAME_SDNN = "sdnn";
        public static final String COLUMN_NAME_BAEVSKY = "baevsky";
        public static final String COLUMN_NAME_RRDATAID = "rrid";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_NOTE = "note";

        /**
         * "private constructor hides the implicit public one"
         */
        private HRVParameterEntry() { }
    }
}
