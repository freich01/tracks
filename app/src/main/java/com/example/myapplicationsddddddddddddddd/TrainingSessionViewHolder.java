package com.example.myapplicationsddddddddddddddd;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class TrainingSessionViewHolder extends RecyclerView.ViewHolder {
    private TextView exerciseNameView;
    private final LineChart lineChart;
    private LineDataSet dataSet;
    private List<Entry> entries;
    private float maxYValue;


    private TrainingSessionViewHolder(@NonNull View itemView) {
        super(itemView);
        exerciseNameView = itemView.findViewById(R.id.exercise_name);
        lineChart = itemView.findViewById(R.id.line_chart);
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "Weight");
    }

    public static TrainingSessionViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.training_session_item, parent, false);
        return new TrainingSessionViewHolder(view);
    }

    public void bind(List<TrainingSession> trainingSessions, String type) {
        Log.d("TSVH", "Received new data, refreshing with: " + trainingSessions);
        entries.clear();
        List<String> dates = new ArrayList<>();
        maxYValue = 0;
        String exerciseName = trainingSessions.get(0).getExerciseName();
        exerciseNameView.setText(exerciseName);

        // iterate over each training session, process data as defined by the type
        for (TrainingSession session : trainingSessions) {
            List<String> sessionDates = session.getTrainingDate();
            List<Double> values;

            switch (type) {
                case "Total weight":
                    values = session.getTotalWeight();
                    break;
                case "Max weight":
                    values = session.getMaxWeight();
                    break;
                case "Avg weight per rep":
                    values = session.getAverageWeightPerRep();
                    break;
                default:
                    values = new ArrayList<>();
            }

            // add entries for each date, value
            for (int i = 0; i < sessionDates.size(); i++) {
                String date = sessionDates.get(i);
                if (!dates.contains(date)) {
                    dates.add(date);
                }
                int dateIndex = dates.indexOf(date);
                Double value = values.get(i);
                entries.add(new Entry(dateIndex, Float.valueOf(String.valueOf(value))));
                maxYValue = (float) Math.max(maxYValue, value);
                Log.d("TSVH", "Added entry for exercise: " + exerciseName + " (" + dateIndex + ", " + value + ")");
            }
        }

        // update dataset with the new entries
        dataSet = new LineDataSet(entries, exerciseName); // label -> exercisename
        dataSet.setValueTextSize(12);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);


        // name that gets displayed on the top of each chart
        Description description = new Description();
        description.setText(exerciseName);
        lineChart.setDescription(description);

        // refresh chart
        lineChart.invalidate();

        // graph design
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new DateValueFormatter(dates));
        xAxis.setLabelRotationAngle(-45); // easier to read
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(-Float.valueOf(dates.size())/20); // improved readability (like padding) so no value will be on the very edge of the graph
        xAxis.setAxisMaximum(Float.valueOf(dates.size()) + Float.valueOf(dates.size())/20); // -||-


        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // hide right y axis

        leftAxis.setAxisMaximum(maxYValue+maxYValue/10); // max Y value is dynamically set based on the values in the graph
        leftAxis.setAxisMinimum(0f); // no negative values are expected, so start graph at 0
        leftAxis.setValueFormatter(new WeightValueFormatter());


        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisLeft().setDrawLabels(true);
        lineChart.getAxisLeft().setTextColor(R.color.black);
        lineChart.getAxisLeft().setLabelCount(5, true);


        xAxis.setDrawLabels(true);
        xAxis.setTextColor(R.color.black);
        if (entries.size() > 8) {
            List<Entry> sampledEntries = sampleEntries(entries, 8);
            dataSet = new LineDataSet(sampledEntries, exerciseName);
            lineChart.setData(new LineData(dataSet));
            dataSet.setValueTextSize(12);
            lineChart.invalidate();
        }

    }

    private List<Entry> sampleEntries(List<Entry> entries, int maxEntries) {
        // sampling is necessary for keeping good readability in large datasets (e.g. multiple years of training data)
        List<Entry> sampledEntries = new ArrayList<>();
        int step = entries.size() / maxEntries;
        for (int i = 0; i < entries.size(); i += step) {
            sampledEntries.add(entries.get(i));
        }
        return sampledEntries;
    }
}




