package com.example.myapplicationsddddddddddddddd;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.List;

// the format is important for the statistics menu
public class DateValueFormatter extends ValueFormatter {
    private final List<String> dates;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public DateValueFormatter(List<String> dates) {
        this.dates = dates;
    }

    @Override
    public String getFormattedValue(float value) {
        int index = (int) value;
        if (index >= 0 && index < dates.size()) {
            return dates.get(index);
        }
        return "";
    }
}

