/*
 * Copyright (C) 2010 The Android Open Source Project
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
package fm.last.android;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MusicPlayerFocusHelper {
    private static final String TAG = "MusicPlayerFocusHelper";

    private AudioManager mAM;
    private MusicFocusable mMusicFocusable;
    private Object mAudioFocusChangeListener;
    
    private boolean mAudioFocusLost = false;
    
    public MusicPlayerFocusHelper(Context context, MusicFocusable musicFocusable) {
        if (sClassOnAudioFocusChangeListener != null) {
            mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mMusicFocusable = musicFocusable;
            createAudioFocusChangeListener();
        }
    }

    public void requestMusicFocus() {
        requestAudioFocusCompat(mAM, mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AUDIOFOCUS_GAIN);
    }
    
    public void abandonMusicFocus() {
        abandonAudioFocusCompat(mAM, mAudioFocusChangeListener);
    }
    
    public void createAudioFocusChangeListener() {
        if (sClassOnAudioFocusChangeListener == null)
            return;

        mAudioFocusChangeListener = Proxy.newProxyInstance(AudioManager.class.getClassLoader(),
                new Class[]{ sClassOnAudioFocusChangeListener },
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if (!method.getName().equals("onAudioFocusChange"))
                            return null;
                        
                        int focusChange = (Integer) args[0];
                        Log.i(TAG, "Audio focus change: " + Integer.toString(focusChange));

                        if (focusChange == AUDIOFOCUS_GAIN) {
                            if (mAudioFocusLost) {
                                mMusicFocusable.focusGained();
                                mAudioFocusLost = false;
                            }
                        } else if (focusChange == AUDIOFOCUS_LOSS) {
                            mAudioFocusLost = true;
                            mMusicFocusable.focusLost(false, false);
                        } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
                            mAudioFocusLost = true;
                            mMusicFocusable.focusLost(true, false);
                        } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            mAudioFocusLost = true;
                            mMusicFocusable.focusLost(true, true);
                        }
                        return null;
                    }
                });
    }


    // Backwards compatibility code (methods available as of SDK Level 8)

    static {
        initializeStaticCompat();
    }

    @SuppressWarnings("unchecked")
    static Class sClassOnAudioFocusChangeListener;
    static Method sMethodRequestAudioFocus;
    static Method sMethodAbandonAudioFocus;
    static int AUDIOFOCUS_GAIN;
    static int AUDIOFOCUS_GAIN_TRANSIENT;
    static int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
    static int AUDIOFOCUS_LOSS;
    static int AUDIOFOCUS_LOSS_TRANSIENT;
    static int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

    private static void initializeStaticCompat() {
        try {
            sClassOnAudioFocusChangeListener = Class.forName("android.media.AudioManager$OnAudioFocusChangeListener");
            sMethodRequestAudioFocus = AudioManager.class.getMethod(
                    "requestAudioFocus",
                    new Class[] { sClassOnAudioFocusChangeListener, int.class, int.class });
            sMethodAbandonAudioFocus = AudioManager.class.getMethod(
                    "abandonAudioFocus",
                    new Class[] { sClassOnAudioFocusChangeListener });
            AUDIOFOCUS_GAIN = AudioManager.class.getField("AUDIOFOCUS_GAIN").getInt(null);
            AUDIOFOCUS_GAIN_TRANSIENT = AudioManager.class.getField("AUDIOFOCUS_GAIN_TRANSIENT").getInt(null);
            AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = AudioManager.class.getField("AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK").getInt(null);
            AUDIOFOCUS_LOSS = AudioManager.class.getField("AUDIOFOCUS_LOSS").getInt(null);
            AUDIOFOCUS_LOSS_TRANSIENT = AudioManager.class.getField("AUDIOFOCUS_LOSS_TRANSIENT").getInt(null);
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = AudioManager.class.getField("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK").getInt(null);
        } catch (ClassNotFoundException e) {
            // Silently fail when running on an OS before SDK level 8.
        } catch (NoSuchMethodException e) {
            // Silently fail when running on an OS before SDK level 8.
        } catch (NoSuchFieldException e) {
            // Silently fail when running on an OS before SDK level 8.
        } catch (IllegalArgumentException e) {
            // Silently fail when running on an OS before SDK level 8.
        } catch (SecurityException e) {
            // Silently fail when running on an OS before SDK level 8.
        } catch (IllegalAccessException e) {
            // Silently fail when running on an OS before SDK level 8.
        }
    }

    private static void requestAudioFocusCompat(AudioManager audioManager,
            Object focusChangeListener, int stream, int durationHint) {
        Log.i(TAG, "reg is null?: " + Boolean.toString(sMethodRequestAudioFocus == null));
        if (sMethodRequestAudioFocus == null)
            return;

        try {
            Object[] args = new Object[3];
            args[0] = focusChangeListener;
            args[1] = stream;
            args[2] = durationHint;
            sMethodRequestAudioFocus.invoke(audioManager, args);
        } catch (InvocationTargetException e) {
            // Unpack original exception when possible
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                // Unexpected checked exception; wrap and re-throw
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException invoking requestAudioFocus.");
            e.printStackTrace();
        }
    }

    private static void abandonAudioFocusCompat(AudioManager audioManager,
            Object focusChangeListener) {
        if (sMethodAbandonAudioFocus == null)
            return;

        try {
            sMethodAbandonAudioFocus.invoke(audioManager, focusChangeListener);
        } catch (InvocationTargetException e) {
            // Unpack original exception when possible
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                // Unexpected checked exception; wrap and re-throw
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException invoking abandonAudioFocus.");
            e.printStackTrace();
        }
    }
}
