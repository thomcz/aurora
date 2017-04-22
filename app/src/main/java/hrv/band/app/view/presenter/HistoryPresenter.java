package hrv.band.app.view.presenter;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hrv.RRData;
import hrv.band.app.control.Measurement;
import hrv.band.app.view.adapter.HRVValue;
import hrv.band.app.view.control.chart.AbstractChartDrawStrategy;
import hrv.band.app.view.control.parameter.AbstractParameterLoadStrategy;

/**
 * Copyright (c) 2017
 * Created by Thomas on 22.04.2017.
 */

public class HistoryPresenter implements IHistoryPresenter {
    private AbstractChartDrawStrategy chartStrategy;
    private AbstractParameterLoadStrategy parameterStrategy;
    private Activity activity;

    public HistoryPresenter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public List<Measurement> getMeasurements(Date date) {
        List<Measurement> measurements = parameterStrategy.loadParameter(activity, date);
        return createMeasurements(measurements);
    }

    @Override
    public void setDrawChartStrategy(AbstractChartDrawStrategy chartStrategy) {
        this.chartStrategy = chartStrategy;
    }

    @Override
    public void setParameterLoadStrategy(AbstractParameterLoadStrategy parameterStrategy) {
        this.parameterStrategy = parameterStrategy;
    }

    @NonNull
    private List<Measurement> createMeasurements(List<Measurement> params) {
        List<Measurement> result = new ArrayList<>();
        for (Measurement parameter : params) {
            RRData.createFromRRInterval(parameter.getRRIntervals(), units.TimeUnit.SECOND);
            Measurement.MeasurementBuilder measurementBuilder = Measurement.from(parameter.getTime(), parameter.getRRIntervals())
                    .category(parameter.getCategory())
                    .rating(parameter.getRating())
                    .note(parameter.getNote());
            result.add(measurementBuilder.build());
        }
        return result;
    }

    @Override
    public String[] getPageTitles() {
        HRVValue[] values = HRVValue.values();
        String[] titles = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            titles[i] = values[i].toString();
        }
        return titles;
    }

    @Override
    public AbstractChartDrawStrategy getChartDrawStrategy() {
        return chartStrategy;
    }
}
