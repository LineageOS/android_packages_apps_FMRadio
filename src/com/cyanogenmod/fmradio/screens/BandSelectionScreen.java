package com.cyanogenmod.fmradio.screens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import com.cyanogenmod.fmradio.R;
import com.cyanogenmod.fmradio.utils.Prefs;
import com.stericsson.hardware.fm.FmBand;

/**
 * At first start, user must select the FM Band in order for proper functionality
 */
public class BandSelectionScreen extends Activity implements View.OnClickListener{


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Prefs.getIsFirstTime(this)){
            setContentView(R.layout.band_selection);
            Button btnProceed = (Button) findViewById(R.id.btn_next);
            btnProceed.setOnClickListener(this);
        } else {
            startActivity(new Intent(this,FmRadioReceiver.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                RadioGroup bandChoice = (RadioGroup) findViewById(R.id.rg_band);
                switch (bandChoice.getCheckedRadioButtonId()){
                    case R.id.rb_usa:
                        Prefs.setPreferredBand(this, FmBand.BAND_US);
                        break;
                    case R.id.rb_eu:
                                            Prefs.setPreferredBand(this, FmBand.BAND_EU);
                                            break;
                    case R.id.rb_ch:
                                            Prefs.setPreferredBand(this, FmBand.BAND_CHINA);
                                            break;
                    case R.id.rb_ja:
                                            Prefs.setPreferredBand(this, FmBand.BAND_JAPAN);
                                            break;
                }
                //next time won't be first time anymore
                Prefs.setIsFirstTime(this,false);
                //start next screen
                startActivity(new Intent(this,FmRadioReceiver.class));
                break;
        }
    }
}