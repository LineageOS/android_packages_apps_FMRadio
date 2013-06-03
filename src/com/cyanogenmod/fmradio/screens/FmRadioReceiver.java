/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.fmradio.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.cyanogenmod.fmradio.R;
import com.cyanogenmod.fmradio.adapters.StationsAdapter;
import com.cyanogenmod.fmradio.utils.Constants;
import com.cyanogenmod.fmradio.utils.Prefs;
import com.cyanogenmod.fmradio.utils.Utils;
import com.stericsson.hardware.fm.FmBand;
import com.stericsson.hardware.fm.FmReceiver;

import java.io.IOException;
import java.util.ArrayList;


public class FmRadioReceiver extends Activity implements OnClickListener, AdapterView.OnItemClickListener {

    private static final int BASE_OPTION_MENU = 0; // The base menu identifier

    private static final int BAND_SELECTION_MENU = 1; // The band menu identifier

    private MediaPlayer mMediaPlayer; // Handle to the Media Player that plays the audio from the selected station

    private FmReceiver.OnScanListener mReceiverScanListener; // The scan listener that receives the return values from the scans

    private FmReceiver.OnRDSDataFoundListener mReceiverRdsDataFoundListener; // The listener that receives the RDS data from the current channel

    private FmReceiver.OnStartedListener mReceiverStartedListener; // The started listener is activated when the radio has started
    private FmReceiver.OnPlayingInStereoListener mOnPlayingInStereoListener;

    private TextView mTvFrequency; // Displays the currently tuned frequency
    private TextView mTvRDS_PSN, mTvRDS_RT; // Displays the current station name if there is adequate RDS data

    private ImageView mIvStereo;

    // Handle to the FM radio Band object
    private FmBand mFmBand;

    private FmReceiver mFmReceiver; // Handle to the FM radio receiver object

    private boolean mIsInitializing = true; // Indicates if we are in the initialization sequence;

    // Protects the MediaPlayer and FmReceiver against rapid muting causing
    // errors
    private boolean mPauseMutex = false;


    // The menu items
    public static final int FM_BAND = Menu.FIRST;
    public static final int BAND_US = Menu.FIRST + 1;
    public static final int BAND_EU = Menu.FIRST + 2;
    public static final int BAND_JAPAN = Menu.FIRST + 3;
    public static final int BAND_CHINA = Menu.FIRST + 4;


    // The currently selected FM Radio band
    private int mSelectedBand;

    //visual components - things we are going to use often
    private ImageButton mBtnSeekUp, mBtnSeekDown, mBtnFullScan, mBtnMute;
    private ProgressBar mProgressScan;
    private Gallery mGalStationsList;

    private ArrayList<String> mStationList;

    private boolean isWiredHeadsetOn = false;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            isWiredHeadsetOn = true;
            setContentView(R.layout.main);
            mFmReceiver = (FmReceiver) getSystemService(Constants.FM_RECEIVER_SERVICE); //MOCK: new FakeFmReceiver();
            // USE Mock class if you don't have access to device with an FM Chip
            // (get mock framework from: https://github.com/pedronveloso/fm_mock_framework

            //get saved FM Band
            mSelectedBand = Prefs.getPreferredBand(this);
            mFmBand = new FmBand(mSelectedBand);
            setupUI();
        } else {
            //earphones not connected
            new AlertDialog.Builder(this).setTitle(R.string.app_name).setIcon(
                    android.R.drawable.ic_dialog_alert).setMessage(getString(R.string.earplugs_not_connected))
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show()
                    .setCancelable(false);
        }
    }

    /**
     * Starts up the listeners and the FM radio if it isn't already active
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (isWiredHeadsetOn) {
            Utils.debugFunc("onStart", Log.DEBUG);

            /**
             * ScanListener
             */
            mReceiverScanListener = new FmReceiver.OnScanListener() {

                // FullScan results
                public void onFullScan(int[] frequency, int[] signalStrength, boolean aborted) {
                    Utils.debugFunc("onFullScan(). aborted: " + aborted, Log.INFO);
                    //stop progress animation
                    stopScanAnimation();

                    mStationList = new ArrayList<String>(frequency.length);
                    for (int i = 0; i < frequency.length; i++) {
                        Utils.debugFunc("[item] freq: " + frequency[i] + ", signal: " + signalStrength[i], Log.INFO);
                        String a = Double.toString((double) frequency[i] / 1000);
                        if (mFmBand.getChannelOffset() == Constants.CHANNEL_OFFSET_50KHZ) {
                            a = String.format(a, "%.2f");
                        } else {
                            a = String.format(a, "%.1f");
                        }
                        mStationList.add(a);
                    }

                    //fill stations list
                    mGalStationsList.setAdapter(new StationsAdapter(FmRadioReceiver.this, mStationList));
                    mGalStationsList.setOnItemClickListener(FmRadioReceiver.this);

                    //initialize playback of first station
                    if (mIsInitializing) {
                        mIsInitializing = false;
                        try {
                            mFmReceiver.setFrequency(frequency[0]);
                            mTvFrequency.setText(mStationList.get(0));
                        } catch (Exception e) {
                            Utils.debugFunc("onFullScan(). E.: " + e.getMessage(), Log.ERROR);
                            showToast(R.string.unable_to_set_frequency, Toast.LENGTH_LONG);
                        }
                    }
                }

                // Returns the new frequency.
                public void onScan(int tunedFrequency, int signalStrength, int scanDirection, boolean aborted) {
                    Utils.debugFunc("onScan(). freq: " + tunedFrequency + ", signal: " + signalStrength + ", dir: " + scanDirection + ", aborted? " + aborted, Log.INFO);

                    stopScanAnimation();

                    String a = Double.toString((double) tunedFrequency / 1000);
                    if (mFmBand.getChannelOffset() == Constants.CHANNEL_OFFSET_50KHZ) {
                        mTvFrequency.setText(String.format(a, "%.2f"));
                    } else {
                        mTvFrequency.setText(String.format(a, "%.1f"));
                    }
                    mBtnSeekUp.setEnabled(true);
                    mBtnSeekDown.setEnabled(true);
                }
            };

            /**
             * RDS Listener
             */
            mReceiverRdsDataFoundListener = new FmReceiver.OnRDSDataFoundListener() {

                // Receives the current frequency's RDS Data
                public void onRDSDataFound(Bundle rdsData, int frequency) {
                    String newPSN = "", newRT = "";
                    if (rdsData.containsKey("PSN")) {
                        newPSN = rdsData.getString("PSN");
                    }
                    if (rdsData.containsKey("RT")) {
                        newRT = rdsData.getString("RT");
                    }
                    if (newPSN != null && !newPSN.trim().isEmpty()) {
                        if (newRT != null && !newRT.trim().isEmpty()) {
                            newPSN = newPSN + " - ";
                            mTvRDS_RT.setText(newRT.trim());
                        }
                        mTvRDS_PSN.setText(newPSN.trim());
                    }
                }
            };

            /**
             * OnStart Listener
             */
            mReceiverStartedListener = new FmReceiver.OnStartedListener() {

                public void onStarted() {
                    Utils.debugFunc("onStarted()", Log.INFO);
                    // Activate all the buttons
                    mBtnSeekUp.setEnabled(true);
                    mBtnSeekDown.setEnabled(true);
                    mBtnMute.setEnabled(true);
                    mBtnFullScan.setEnabled(true);
                    initialBandScan();
                    startAudio();
                }
            };

            /**
             * PlayingInStereo Listener
             */
            mOnPlayingInStereoListener = new FmReceiver.OnPlayingInStereoListener() {
                @Override
                public void onPlayingInStereo(boolean inStereo) {
                    //Utils.debugFunc("onPlayingInStereo(): "+inStereo, Log.INFO);
                    if (inStereo) {
                        mIvStereo.setImageResource(R.drawable.fm_stereo);
                    } else {
                        mIvStereo.setImageResource(R.drawable.fm_mono);
                    }
                }
            };

            mFmReceiver.addOnScanListener(mReceiverScanListener);
            mFmReceiver.addOnRDSDataFoundListener(mReceiverRdsDataFoundListener);
            mFmReceiver.addOnStartedListener(mReceiverStartedListener);
            mFmReceiver.addOnPlayingInStereoListener(mOnPlayingInStereoListener);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Stops the FM Radio listeners
        if (mFmReceiver != null) {
            mFmReceiver.removeOnScanListener(mReceiverScanListener);
            mFmReceiver.removeOnRDSDataFoundListener(mReceiverRdsDataFoundListener);
            mFmReceiver.removeOnStartedListener(mReceiverStartedListener);
            mFmReceiver.removeOnPlayingInStereoListener(mOnPlayingInStereoListener);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //only if playback was actually initiated
        if (!mIsInitializing){
            //Saves the FmBand for next time the program is used and closes the radio
            // and media player.
            Prefs.setPreferredBand(this, mSelectedBand);
            turnOffRadio();
        }
    }


    private void turnOffRadio() {
        try {
            mFmReceiver.reset();
        } catch (IOException e) {
            Utils.debugFunc("Unable to reset correctly E.: " + e, Log.ERROR);
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * Starts the initial bandscan in it's own thread
     */
    private void initialBandScan() {
        Utils.debugFunc("initialBandScan()", Log.INFO);
        Thread bandscanThread = new Thread() {
            public void run() {
                try {
                    mFmReceiver.startFullScan();
                } catch (IllegalStateException e) {
                    showToast(R.string.unable_to_scan, Toast.LENGTH_LONG);
                    Utils.debugFunc("initialBandScan(). E.: " + e.getMessage(), Log.ERROR);
                    return;
                }
            }
        };
        bandscanThread.start();
    }


    /**
     * Helper method to display toast
     */
    private void showToast(final int resourceID, final int duration) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), getString(resourceID), duration).show();
            }
        });
    }

    /**
     * Starts the FM receiver and makes the buttons appear inactive
     */
    private void turnRadioOn() {
        Utils.debugFunc("turnRadioOn()", Log.INFO);

        try {
            mFmReceiver.startAsync(mFmBand);
            // Darken the the buttons
            mBtnSeekUp.setEnabled(false);
            mBtnSeekDown.setEnabled(false);
            mBtnMute.setEnabled(false);
            mBtnFullScan.setEnabled(false);
            showToast(R.string.scanning_for_stations, Toast.LENGTH_LONG);
        } catch (Exception e) {
            Utils.debugFunc("turnRadioOn(). E.: " + e.getMessage(), Log.ERROR);
            showToast(R.string.unable_to_start_radio, Toast.LENGTH_LONG);
        }
    }

    /**
     * Starts the FM receiver and makes the buttons appear inactive
     */
    private void startAudio() {
        Utils.debugFunc("startAudio()", Log.INFO);

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(Constants.MEDIA_SOURCE);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            Utils.debugFunc("startAudio. E.: " + e.getMessage(), Log.ERROR);
            showToast(R.string.unable_to_mediaplayer, Toast.LENGTH_LONG);
        }
    }

    /**
     * Sets up the buttons and their listeners
     */
    private void setupUI() {
        Utils.debugFunc("setupUI()", Log.INFO);

        mTvFrequency = (TextView) findViewById(R.id.FrequencyTextView);
        mTvRDS_PSN = (TextView) findViewById(R.id.tv_ps_text);
        mTvRDS_RT = (TextView) findViewById(R.id.tv_rds_text);
        mProgressScan = (ProgressBar) findViewById(R.id.scan_progressbar);

        mGalStationsList = (Gallery) findViewById(R.id.gal_stations_list);

        mBtnSeekUp = (ImageButton) findViewById(R.id.btn_seek_up);
        mBtnSeekUp.setOnClickListener(this);

        mBtnSeekDown = (ImageButton) findViewById(R.id.btn_seek_down);
        mBtnSeekDown.setOnClickListener(this);

        mBtnMute = (ImageButton) findViewById(R.id.btn_mute);
        mBtnMute.setOnClickListener(this);

        mBtnFullScan = (ImageButton) findViewById(R.id.btn_fullscan);
        mBtnFullScan.setOnClickListener(this);

        mIvStereo = (ImageView) findViewById(R.id.iv_playback_mode);
    }

    /**
     * Stops scanning animation
     */
    private void stopScanAnimation() {
        mProgressScan.setVisibility(View.GONE);
        mBtnFullScan.setVisibility(View.VISIBLE);
        mBtnFullScan.setEnabled(true);
    }

    /**
     * Start scanning animation
     */
    private void startScanAnimation() {
        mProgressScan.setVisibility(View.VISIBLE);
        mBtnFullScan.setVisibility(View.GONE);
        mBtnFullScan.setEnabled(false);
    }

    /**
     * If application is currently scanning, that scanning operation is
     * canceled.
     */
    private void stopCurrentScan() {
        if (mFmReceiver.getState() == FmReceiver.STATE_SCANNING) {
            Utils.debugFunc("There is a scan in progress. Stopping it.", Log.INFO);
            mFmReceiver.stopScan();
        }
    }

    /**
     * Sets up the options menu when the menu button is pushed, dynamic
     * population of the station select menu
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
        Utils.debugFunc("onPrepareOptionsMenu()", Log.INFO);
        menu.clear();
        boolean result = super.onCreateOptionsMenu(menu);
        SubMenu subMenu = menu.addSubMenu(BASE_OPTION_MENU, FM_BAND, Menu.NONE,
                R.string.band_select);
        subMenu.setIcon(android.R.drawable.ic_menu_mapmode);
        // Populate the band selection menu
        subMenu.add(BAND_SELECTION_MENU, BAND_US, Menu.NONE, R.string.band_us);
        subMenu.add(BAND_SELECTION_MENU, BAND_EU, Menu.NONE, R.string.band_eu);
        subMenu.add(BAND_SELECTION_MENU, BAND_JAPAN, Menu.NONE, R.string.band_ja);
        subMenu.add(BAND_SELECTION_MENU, BAND_CHINA, Menu.NONE, R.string.band_ch);
        subMenu.setGroupCheckable(BAND_SELECTION_MENU, true, true);
        subMenu.getItem(mSelectedBand).setChecked(true);
        return result;
    }


    /**
     * React to a selection in the option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getGroupId()) {

            case BAND_SELECTION_MENU:
                switch (item.getItemId()) {
                    case BAND_US:
                        mSelectedBand = FmBand.BAND_US;
                        item.setChecked(true);
                        break;
                    case BAND_EU:
                        mSelectedBand = FmBand.BAND_EU;
                        item.setChecked(true);
                        break;
                    case BAND_JAPAN:
                        mSelectedBand = FmBand.BAND_JAPAN;
                        item.setChecked(true);
                        break;
                    case BAND_CHINA:
                        mSelectedBand = FmBand.BAND_CHINA;
                        item.setChecked(true);
                        break;
                    default:
                        break;
                }
                mFmBand = new FmBand(mSelectedBand);
                //restart radio
                turnOffRadio();
                turnRadioOn();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_seek_down:
                Utils.debugFunc("SeekDown pressed", Log.INFO);
                if (mFmReceiver.getState() == FmReceiver.STATE_IDLE) {
                    doFullScan();
                } else {
                    //select next station in stations list
                    int pos = mGalStationsList.getSelectedItemPosition();
                    if (pos == 0) {
                        mGalStationsList.setSelection(mStationList.size() - 1, true);
                        setFrequency(mStationList.get(mStationList.size() - 1));
                    } else {
                        mGalStationsList.setSelection(pos - 1, true);
                        setFrequency(mStationList.get(pos - 1));
                    }
                }
                break;

            case R.id.btn_seek_up:
                Utils.debugFunc("SeekUp pressed", Log.INFO);
                if (mFmReceiver.getState() == FmReceiver.STATE_IDLE) {
                    doFullScan();
                } else {
                    //select next station in stations list
                    int pos = mGalStationsList.getSelectedItemPosition();
                    if (pos >= (mStationList.size() - 1)) {
                        mGalStationsList.setSelection(0, true);
                        setFrequency(mStationList.get(0));
                    } else {
                        mGalStationsList.setSelection(pos + 1, true);
                        setFrequency(mStationList.get(pos + 1));
                    }
                }
                break;

            case R.id.btn_fullscan:
                doFullScan();
                break;

            case R.id.btn_mute:
                Utils.debugFunc("Mute pressed", Log.INFO);
                if (mFmReceiver.getState() == FmReceiver.STATE_PAUSED && !mPauseMutex) {
                    try {
                        mPauseMutex = true;
                        mFmReceiver.resume();
                        mMediaPlayer.start();
                        mBtnMute.setImageResource(R.drawable.fm_volume_mute);
                    } catch (Exception e) {
                        Utils.debugFunc("Unable to resume. E.: " + e.getMessage(), Log.ERROR);
                        showToast(R.string.unable_to_resume, Toast.LENGTH_LONG);
                    }
                    mPauseMutex = false;
                } else if (mFmReceiver.getState() == FmReceiver.STATE_STARTED
                        && !mPauseMutex) {
                    try {
                        mPauseMutex = true;
                        mMediaPlayer.pause();
                        mFmReceiver.pause();
                        mBtnMute.setImageResource(R.drawable.fm_volume);
                    } catch (Exception e) {
                        Utils.debugFunc("Unable to pause. E.: " + e.getMessage(), Log.ERROR);
                        showToast(R.string.unable_to_pause, Toast.LENGTH_LONG);
                    }
                    mPauseMutex = false;
                } else if (mPauseMutex) {
                    showToast(R.string.mediaplayer_busy, Toast.LENGTH_LONG);
                } else {
                    Utils.debugFunc("No action: incorrect state - " + mFmReceiver.getState(), Log.WARN);
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //right now just handling gallery item clicks
        setFrequency(mStationList.get(position));
    }


    /**
     * Do FM FullScan. Takes care of UI update and error handling
     */
    private void doFullScan() {
        Utils.debugFunc("doFullScan()", Log.INFO);
        // if Radio is OFF, turn it ON now
        if (mFmReceiver.getState() == FmReceiver.STATE_IDLE) {
            turnRadioOn();
        }
        try {
            mFmReceiver.startFullScan();
            startScanAnimation();
        } catch (IllegalStateException e) {
            Utils.debugFunc("Scan error: " + e.getMessage(), Log.ERROR);
            showToast(R.string.unable_to_scan, Toast.LENGTH_LONG);
            mBtnFullScan.setEnabled(true);
        }
    }

    /**
     * Sets the frequency for the radio and update UI accordingly
     *
     * @param freq frequency to tune to
     */
    private void setFrequency(String freq) {
        //clear RSD text
        mTvRDS_PSN.setText("");
        mTvRDS_RT.setText("");
        try {
            mFmReceiver.setFrequency((int) (Double.valueOf(freq) * 1000));
            mTvFrequency.setText(freq);
        } catch (IOException e) {
            Utils.debugFunc("Set frequency failed! E.: " + e.getMessage(), Log.ERROR);
            showToast(R.string.unable_to_set_frequency, Toast.LENGTH_LONG);
        }

    }
}
