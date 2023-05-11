/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.fmradio.recordings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;

import java.io.File;

public class PlayRecording extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent oldIntent = getIntent();

        if (oldIntent != null && oldIntent.hasExtra("type") && oldIntent.hasExtra("path")) {
            Intent nextIntent = new Intent(Intent.ACTION_VIEW);
            final Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), new File(Uri.parse(oldIntent.getStringExtra("path")).getPath()));
            nextIntent.setDataAndType(uri, oldIntent.getStringExtra("type"));
            nextIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(nextIntent);
        }

        finish();
    }
}
