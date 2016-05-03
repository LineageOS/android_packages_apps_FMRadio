# Copyright (C) 2014 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := FMRadio_static
LOCAL_SRC_FILES := $(filter-out src/com/android/fmradio/%Activity.java src/com/android/fmradio/dialogs/% src/com/android/fmradio/views/%, $(call all-java-files-under, src))
LOCAL_RESOURCE_DIR = $(LOCAL_PATH)/res
include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := FMRadio
LOCAL_JNI_SHARED_LIBRARIES := libfmjni

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PRIVILEGED_MODULE := true

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-cardview
LOCAL_RESOURCE_DIR = $(LOCAL_PATH)/res frameworks/support/v7/cardview/res

LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages android.support.v7.cardview

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
