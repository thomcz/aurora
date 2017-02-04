package hrv.band.app.control;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Objects;

import hrv.band.app.view.adapter.MeasurementCategoryAdapter;
import hrv.calc.AllHRVIndiceCalculator;

/**
 * Class that stores: Calculated HRV-Parameter values, Date of the measurement,
 * the actual rr-data, user note about the measurement, measurement category.
 *
 * TODO: this class has too many responsibilities in terms of the multiple
 * TODO: data that it stores. In addition, the name does not fit entirely to the stored data
 *
 * Created by Julian on 11.06.2016.
 */
public class HRVParameters implements Parcelable {

    private Date time;
    private double sdsd;
    private double sd1;
    private double sd2;
    private double lf;
    private double hf;
    private double rmssd;
    private double sdnn;
    private double baevsky;
    private double[] rrIntervals;
    private double rating;
    private MeasurementCategoryAdapter.MeasureCategory category;
    private String note;

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<HRVParameters> CREATOR = new Parcelable.Creator<HRVParameters>() {
        @Override
        public HRVParameters createFromParcel(Parcel in) {
            return new HRVParameters(in);
        }
        @Override
        public HRVParameters[] newArray(int size) {
            return new HRVParameters[size];
        }
    };

    public HRVParameters() {
        //the standard constructor
    }

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private HRVParameters(Parcel in) {
        time = (Date) in.readValue(getClass().getClassLoader());
        sdsd = in.readDouble();
        sd1 = in.readDouble();
        sd2 = in.readDouble();
        lf = in.readDouble();
        hf = in.readDouble();
        rmssd = in.readDouble();
        sdnn = in.readDouble();
        baevsky = in.readDouble();
        int rrIntervalLength= in.readInt();
        rrIntervals = new double[rrIntervalLength];
        in.readDoubleArray(rrIntervals);
        rating = in.readDouble();
        category = (MeasurementCategoryAdapter.MeasureCategory) in.readSerializable();
        note = in.readString();
    }

    public HRVParameters(Date time, double sdsd, double sd1, double sd2, double lf, double hf, double rmssd,
                         double sdnn, double baevsky, double[] rrIntervals) {
        this.time = time;
        this.sdsd = sdsd;
        this.sd1 = sd1;
        this.sd2 = sd2;
        this.lf = lf;
        this.hf = hf;
        this.rmssd = rmssd;
        this.sdnn = sdnn;
        this.baevsky = baevsky;
        this.rrIntervals = rrIntervals;
        this.category = MeasurementCategoryAdapter.MeasureCategory.GENERAL;
        this.note = "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeValue(time);
        out.writeDouble(sdsd);
        out.writeDouble(sd1);
        out.writeDouble(sd2);
        out.writeDouble(lf);
        out.writeDouble(hf);
        out.writeDouble(rmssd);
        out.writeDouble(sdnn);
        out.writeDouble(baevsky);
        out.writeInt(rrIntervals.length);
        out.writeDoubleArray(rrIntervals);
        out.writeDouble(rating);
        out.writeSerializable(category);
        out.writeString(note);
    }

    public Date getTime() {
        return time;
    }


    public void setTime(Date time) {
        this.time = time;
    }

    public double getBaevsky() {
        return baevsky;
    }

    public void setBaevsky(double baevsky) {
        this.baevsky = baevsky;
    }

    public double getSdSd() {
        return sdsd;
    }

    public void setSdSd(double sdsd) {
        this.sdsd = sdsd;
    }

    public double getSd1() {
        return sd1;
    }

    public void setSd1(double sd1) {
        this.sd1 = sd1;
    }

    public double getSd2() {
        return sd2;
    }

    public void setSd2(double sd2) {
        this.sd2 = sd2;
    }

    public double getSd1sd2Ratio() {
        return sd1 / sd2;
    }

    public double getLf() {
        return lf;
    }

    public void setLf(double lf) {
        this.lf = lf;
    }

    public double getHf() {
        return hf;
    }

    public void setHf(double hf) {
        this.hf = hf;
    }

    public double getLfhfRatio() {
        return lf / hf;
    }

    public double getRmssd() {
        return rmssd;
    }

    public void setRmssd(double rmssd) {
        this.rmssd = rmssd;
    }

    public double getSdnn() {
        return sdnn;
    }

    public void setSdnn(double sdnn) {
        this.sdnn = sdnn;
    }
    public double[] getRRIntervals() {
        return rrIntervals;
    }

    public void setRRIntervals(double[] rrIntervals) {
        this.rrIntervals = rrIntervals;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public MeasurementCategoryAdapter.MeasureCategory getCategory() {
        if (category == null) {
            return MeasurementCategoryAdapter.MeasureCategory.GENERAL;
        }
        return category;
    }

    public void setCategory(MeasurementCategoryAdapter.MeasureCategory category) {
        this.category = category;
    }

    public String getNote() {
        if (note == null) {
            return "";
        }
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == null) {
            return false;
        }
        if(other == this) {
            return true;
        }
        if(!(other instanceof HRVParameters)) {
            return false;
        }

        HRVParameters param = (HRVParameters)other;

        return param.getTime().equals(this.getTime());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTime());
    }

    /**
     * Creates a new HRVParameter-Object from a ALLHRVIndiceCalculator object
     * At the time the data unit can not be changed according to the incoming data
     * thats why the data has to be converted to the units given in the HRVValue class
     * @param calc AllHRVIndiceCalculator object
     * @param time Time when the measurement began
     * @param rr Original RR-Data
     * @return New HRVParameters object
     */
    public static HRVParameters from(AllHRVIndiceCalculator calc, Date time, double[] rr) {
        return new HRVParameters(time,
                calc.getSdsd().getValue(),
                calc.getSd1().getValue() * 1000, //Convert to ms
                calc.getSd2().getValue() * 1000, //Convert to ms
                calc.getLf().getValue() * 1000,
                calc.getHf().getValue() * 1000,
                calc.getRmssd().getValue() * 1000, //Convert to ms
                calc.getSdnn().getValue() * 1000 , //Convert to ms
                calc.getBaevsky().getValue() * 100, //Convert to %
                rr);
    }
}