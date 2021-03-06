package hrv.band.app.model.storage.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hrv.band.app.model.Measurement;
import hrv.band.app.model.storage.IStorage;
import hrv.band.app.model.storage.sqlite.statements.HRVParamSQLiteObjectAdapter;
import hrv.band.app.model.storage.sqlite.statements.HRVParameterContract;
import hrv.band.app.model.storage.sqlite.statements.RRIntervalContract;

/**
 * Copyright (c) 2017
 * Created by Julian Martin on 23.06.2016.
 * <p>
 * Responsible for saving and loading user data.
 */
public class HRVSQLController implements IStorage {

    private SQLiteStorageController storageController;

    public HRVSQLController(Context context) {
        this.storageController = new SQLiteStorageController(context);
    }
    public HRVSQLController(SQLiteStorageController storageController) {
        this.storageController = storageController;
    }

    private static Date getEndOfDay(Date date) {
        return DateUtils.addMilliseconds(DateUtils.ceiling(date, Calendar.DATE), -1);
    }

    private static Date getStartOfDay(Date date) {
        return DateUtils.truncate(date, Calendar.DATE);
    }

    @Override
    public void saveData(List<Measurement> parameters) {

        for (Measurement param : parameters) {
            saveData(param);
        }
    }

    @Override
    public void saveData(Measurement parameter) {
        ContentValues valuesParams = createSavableMeasurement(parameter);
        long firstId = saveMeasurement(valuesParams);
        saveRRData(parameter, firstId);
    }

    @NonNull
    private ContentValues createSavableMeasurement(Measurement parameter) {
        ContentValues valuesParams = new ContentValues();
        long time = parameter.getTime().getTime();

        valuesParams.put(HRVParameterContract.HRVParameterEntry.COLUMN_NAME_TIME, time);
        valuesParams.put(HRVParameterContract.HRVParameterEntry.COLUMN_NAME_RATING, parameter.getRating());
        valuesParams.put(HRVParameterContract.HRVParameterEntry.COLUMN_NAME_CATEGORY, parameter.getCategory().toString());
        valuesParams.put(HRVParameterContract.HRVParameterEntry.COLUMN_NAME_NOTE, parameter.getNote());

        return valuesParams;
    }

    private void saveRRData(Measurement measurement, long id) {
        SQLiteDatabase db = storageController.getWritableDatabase();
        db.beginTransaction();

        for (Double rrVal : measurement.getRRIntervals()) {
            ContentValues valuesRR = new ContentValues();
            valuesRR.put(RRIntervalContract.RRIntervalEntry.COLUMN_NAME_ENTRY_ID, id);
            valuesRR.put(RRIntervalContract.RRIntervalEntry.COLUMN_NAME_ENTRY_VALUE, rrVal);

            db.insert(RRIntervalContract.RRIntervalEntry.TABLE_NAME,
                    RRIntervalContract.RRIntervalEntry.COLUMN_NAME_ENTRY_VALUE,
                    valuesRR);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    private long saveMeasurement(ContentValues valuesParams) {
        SQLiteDatabase db = storageController.getWritableDatabase();
        db.beginTransaction();
        long firstId = db.insert(HRVParameterContract.HRVParameterEntry.TABLE_NAME,
                HRVParameterContract.HRVParameterEntry.COLUMN_NAME_ENTRY_ID,
                valuesParams);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return firstId;
    }

    @Override
    public List<Measurement> loadData(Date date) {
        return loadData(date, date);
    }

    @Override
    public List<Measurement> loadData(Date startDate, Date endDate) {
        SQLiteDatabase db = storageController.getReadableDatabase();
        HRVParamSQLiteObjectAdapter hrvParamSelect = new HRVParamSQLiteObjectAdapter(db);
        String whereClause = HRVParameterContract.HRVParameterEntry.COLUMN_NAME_TIME + " BETWEEN ? AND ?";
        String timeOfDayStartStr = Long.toString(getStartOfDay(startDate).getTime());
        String timeOfDayEndStr = Long.toString(getEndOfDay(endDate).getTime());
        String[] whereClauseParams = new String[]{timeOfDayStartStr, timeOfDayEndStr};
        return hrvParamSelect.select(whereClause, whereClauseParams);
    }

    private List<Measurement> loadAllHRVParams() {
        SQLiteDatabase db = storageController.getReadableDatabase();

        HRVParamSQLiteObjectAdapter hrvParamSelect = new HRVParamSQLiteObjectAdapter(db);
        return hrvParamSelect.select(null, null);
    }

    @Override
    public boolean deleteData(Measurement parameter) {
        SQLiteDatabase db = storageController.getReadableDatabase();

        String timeStr = Long.toString(parameter.getTime().getTime());

        String whereClause = HRVParameterContract.HRVParameterEntry.COLUMN_NAME_TIME + "=?";
        String[] whereArgs = new String[]{timeStr};

        return db.delete(HRVParameterContract.HRVParameterEntry.TABLE_NAME,
                whereClause, whereArgs) > 0;
    }

    @Override
    public boolean deleteData(List<Measurement> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            deleteData(parameters.get(i));
        }

        return true;
    }

    @Override
    public void closeDatabaseHelper() {
        storageController.close();
    }

    /**
     * Tries to export the database to the users documents folder.
     * Returns whether the export was successfully or not
     *
     * @return True if export was successful, false otherwise
     * @throws IOException
     */
    public boolean exportDB() throws IOException {

        //Experimental zone
        if (!isExternalStorageWritable()) {
            return false;
        }
        //Activity has to check for write external model.storage permission
        // try to write the file and return error if not able to write.

        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/hrvband");
        documentsDir.mkdirs();

        List<Measurement> allHrvParams = loadAllHRVParams();

        try {
            for (int i = 0; i < allHrvParams.size(); i++) {
                if (!exportIBIFile(documentsDir, allHrvParams.get(i))) {
                    return false;
                }
            }

            return true;

        } catch (SecurityException e) {
            Log.e(e.getClass().getName(), "SecurityException", e);
        }

        return false;
    }


    /**
     * Exports the given param to the given directory
     *
     * @param documentsDir Directory to export to
     * @param param        param to export
     * @return Whether the export was successful
     * @throws IOException
     */
    private boolean exportIBIFile(File documentsDir, Measurement param) throws IOException {
        final String s = param.getTime().toString();
        //TODO: folder structure /RR/date/file.ibi
        File ibiFile = new File(documentsDir.getAbsolutePath(), "/RR" + s + ".ibi");
        ibiFile.deleteOnExit();

        if (ibiFile.exists() && !ibiFile.delete()
                || !ibiFile.createNewFile()) {
            return false;
        }

        FileOutputStream outStr = new FileOutputStream(ibiFile);
        PrintWriter out = new PrintWriter(outStr);

        double[] rrIntervals = param.getRRIntervals();
        for (double rrInterval : rrIntervals) {
            out.println(rrInterval);
        }

        out.close();
        outStr.close();
        return true;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}

