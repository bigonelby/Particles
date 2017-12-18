package com.particles.android.wallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.particles.android.ParticlesRenderer;

/**
 * Created by liuboyuan on 2017/12/7.
 */

public class GLWallPaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    private class GLEngine extends Engine {
        private WallpaperGLSurfaceView glSurfaceView;
        private boolean rendererSet;
        private ParticlesRenderer particlesRenderer;

        @Override
        public void onOffsetsChanged(final float xOffset, final float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    particlesRenderer.handleOffsetsChanged(xOffset, yOffset);
                }
            });
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            glSurfaceView = new WallpaperGLSurfaceView(GLWallPaperService.this);

            ActivityManager activityManager =
                    (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

            final boolean supportsEs2 =
                    configurationInfo.reqGlEsVersion >= 0x20000
                    || (Build.FINGERPRINT.startsWith("generic")
                    || (Build.FINGERPRINT.startsWith("unknown"))
                    || (Build.MODEL.contains("google_sdk"))
                    || (Build.MODEL.contains("Emulator"))
                    || (Build.MODEL.contains("Android SDK built for x86")));

            particlesRenderer =
                    new ParticlesRenderer(GLWallPaperService.this);

            if (supportsEs2) {
                glSurfaceView.setEGLContextClientVersion(2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    glSurfaceView.setPreserveEGLContextOnPause(true);
                }
                glSurfaceView.setRenderer(particlesRenderer);
                rendererSet = true;
            } else {
                Toast.makeText(GLWallPaperService.this,
                        "This device does not support OpenGL ES 2.0",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (rendererSet) {
                if (visible) {
                    glSurfaceView.onResume();
                } else {
                    glSurfaceView.onPause();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            glSurfaceView.onWallpaperDestory();
        }

        private class WallpaperGLSurfaceView extends GLSurfaceView {
            public WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onWallpaperDestory() {
                super.onDetachedFromWindow();
            }
        }
    }
}
