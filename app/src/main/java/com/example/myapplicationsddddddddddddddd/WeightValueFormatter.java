package com.example.myapplicationsddddddddddddddd;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class WeightValueFormatter extends ValueFormatter {
    // purpose is to display the y-axis-values as kg values in the LineData
    @Override
    public String getFormattedValue(float value) {
        return String.format("%.1fkg", value);
    }
}

