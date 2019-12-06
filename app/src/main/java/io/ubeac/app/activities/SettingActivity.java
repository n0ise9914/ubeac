package io.ubeac.app.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import com.blankj.utilcode.util.SPUtils;
import io.ubeac.app.R;
import io.ubeac.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SettingActivity extends AppCompatActivity {
    private TextInputEditText transmissionPeriod;
    private CheckBox enabled;
    private CheckBox bgServiceEnabled;
    private TextInputEditText samplingPeriod;
    private TextInputEditText url;
    private TextInputEditText device;
    private TextInputLayout samplingPeriodLayout;
    private TextInputLayout transmissionPeriodLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        url = findViewById(R.id.url);
        device = findViewById(R.id.device_id);
        samplingPeriod = findViewById(R.id.sampling_period);
        CustomTextWatcher customTextWatcher = new CustomTextWatcher();
        samplingPeriod.addTextChangedListener(customTextWatcher);
        samplingPeriodLayout = findViewById(R.id.sampling_period_layout);
        transmissionPeriodLayout = findViewById(R.id.transmission_period_layout);
        transmissionPeriod = findViewById(R.id.transmission_period);
        transmissionPeriod.addTextChangedListener(customTextWatcher);
        url.setText(SPUtils.getInstance().getString(Constants.SETTING_URL));
        device.setText(SPUtils.getInstance().getString(Constants.SETTING_DEVICE, UUID.randomUUID().toString()));
        enabled = findViewById(R.id.enabled);
        enabled.setChecked(SPUtils.getInstance().getBoolean(Constants.SETTING_ENABLED));
        samplingPeriod.setText(String.valueOf(SPUtils.getInstance().getInt(Constants.SETTING_SAMPLING_PERIOD, 1000)));
        transmissionPeriod.setText(String.valueOf(SPUtils.getInstance().getInt(Constants.SETTING_TRANSMISSION_PERIOD, 1000)));
        List<String> categories = new ArrayList<>();
        categories.add(Constants.HTTPS);
        categories.add(Constants.HTTP);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bgServiceEnabled = findViewById(R.id.background_service_enabled);
        bgServiceEnabled.setChecked(SPUtils.getInstance().getBoolean(Constants.SETTING_BG_SERVICE_ENABLED));
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
        if (save()) {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private boolean save() {
        if (!validate())
            return false;
        String samplingPeriod = String.valueOf(this.samplingPeriod.getText());
        String transmissionPeriod = String.valueOf(this.transmissionPeriod.getText());
        SPUtils.getInstance().put(Constants.SETTING_SAMPLING_PERIOD, Integer.parseInt(samplingPeriod));
        SPUtils.getInstance().put(Constants.SETTING_TRANSMISSION_PERIOD, Integer.parseInt(transmissionPeriod));
        SPUtils.getInstance().put(Constants.SETTING_URL, String.valueOf(url.getText()));
        SPUtils.getInstance().put(Constants.SETTING_ENABLED, enabled.isChecked());
        SPUtils.getInstance().put(Constants.SETTING_BG_SERVICE_ENABLED, bgServiceEnabled.isChecked());
        SPUtils.getInstance().put(Constants.SETTING_DEVICE, String.valueOf(device.getText()));
        return true;
    }

    private class CustomTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            validate();
        }
    }

    private boolean validate() {
        String samplingPeriod = String.valueOf(this.samplingPeriod.getText());
        String transmissionPeriod = String.valueOf(this.transmissionPeriod.getText());
        boolean hasError = false;
        if (samplingPeriod.equals("") || samplingPeriod.equals("0")) {
            samplingPeriodLayout.setError("Should be bigger than zero");
            samplingPeriodLayout.setErrorEnabled(true);
            hasError = true;
        }
        if (transmissionPeriod.equals("") || transmissionPeriod.equals("0")) {
            transmissionPeriodLayout.setError("Should be bigger than zero");
            transmissionPeriodLayout.setErrorEnabled(true);
            hasError = true;
        }
        if (!transmissionPeriod.equals("") && !samplingPeriod.equals(""))
            if (Integer.parseInt(samplingPeriod) > Integer.parseInt(transmissionPeriod)) {
                if (this.samplingPeriod.isFocused()) {
                    samplingPeriodLayout.setError("Should be smaller than interval");
                    samplingPeriodLayout.setErrorEnabled(true);
                    hasError = true;
                } else if (this.transmissionPeriod.isFocused()) {
                    transmissionPeriodLayout.setError("Should be bigger than sampling period");
                    transmissionPeriodLayout.setErrorEnabled(true);
                    hasError = true;
                }
            }
        if (!hasError) {
            samplingPeriodLayout.setErrorEnabled(false);
            transmissionPeriodLayout.setErrorEnabled(false);
        }
        return !hasError;
    }
}
