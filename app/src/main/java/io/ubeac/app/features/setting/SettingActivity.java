package io.ubeac.app.features.setting;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import com.blankj.utilcode.util.SPUtils;
import io.ubeac.app.R;
import io.ubeac.app.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    private TextInputEditText transmissionPeriod;
    private CheckBox enabled;
    private Spinner protocol;
    private EditText token ;
    private TextInputEditText samplingPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        token = findViewById(R.id.token);
        findViewById(R.id.remove_token).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                token.setText(null);
            }
        });
        samplingPeriod= findViewById(R.id.sampling_period);
        transmissionPeriod= findViewById(R.id.transmission_period);
        token.setText(SPUtils.getInstance().getString(Constants.SETTING_TOKEN));
        enabled = findViewById(R.id.enabled);
        enabled.setChecked(SPUtils.getInstance().getBoolean(Constants.SETTING_ENABLED));
        samplingPeriod.setText(String.valueOf(SPUtils.getInstance().getInt(Constants.SETTING_SAMPLING_PERIOD,1000)));
        transmissionPeriod.setText(String.valueOf(SPUtils.getInstance().getInt(Constants.SETTING_TRANSMISSION_PERIOD,1000)));
        protocol = findViewById(R.id.protocol);
        List<String> categories = new ArrayList<>();
        categories.add(Constants.HTTPS);
        categories.add(Constants.HTTP);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocol.setAdapter(dataAdapter);
        protocol.setSelection(SPUtils.getInstance().getString(Constants.SETTING_PROTOCOL, Constants.HTTPS).equals(Constants.HTTPS) ? 0 : 1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onBack();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBack();
        return true;
    }

    private void onBack() {
        save();
        finish();
        overridePendingTransition(0, 0);
    }

    private void save() {
        SPUtils.getInstance().put(Constants.SETTING_SAMPLING_PERIOD, Integer.parseInt(String.valueOf(samplingPeriod.getText())));
        SPUtils.getInstance().put(Constants.SETTING_TRANSMISSION_PERIOD, Integer.parseInt(String.valueOf(transmissionPeriod.getText())));
        SPUtils.getInstance().put(Constants.SETTING_TOKEN, String.valueOf(token.getText()));
        SPUtils.getInstance().put(Constants.SETTING_ENABLED, enabled.isChecked());
        SPUtils.getInstance().put(Constants.SETTING_PROTOCOL, protocol.getSelectedItem().toString());
    }
}
