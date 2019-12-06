package io.ubeac.app.models;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import io.ubeac.app.R;

public class SensorViewHolder {
    public TextView name;
    public TextView value;
    public CheckBox enabled;

    public SensorViewHolder(View view) {
        name = view.findViewById(R.id.name);
        value = view.findViewById(R.id.value);
        enabled = view.findViewById(R.id.enabled);
    }
}
