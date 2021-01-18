package edmt.dev.camrec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private Button btnRecord;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private MediaRecorder mr;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRecord = findViewById(R.id.btnRecord);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
            }
        }
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording)
                {
                    mr.stop();
                    releaseMediaRecorder();
                    camera.lock();
                    btnRecord.setText("REC");
                    isRecording = false;
                    Toast.makeText(getApplicationContext(),"Done!",Toast.LENGTH_LONG).show();
                }else
                {
                    if (prepareForVideoRecording())
                    {
                        mr.start();
                        btnRecord.setText("STOP");
                        isRecording = true;
                    }
                }
            }
        });
    }

    private boolean prepareForVideoRecording()
    {
        camera.unlock();
        mr = new MediaRecorder();
        mr.setCamera(camera);
        mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mr.setOutputFile(getOutputFile(MEDIA_TYPE_VIDEO).toString());
        mr.setPreviewDisplay(surfaceHolder.getSurface());
        mr.setVideoSize(1920,1080);

        try {
            mr.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mr != null)
        {
            mr.reset();
            mr.release();
            mr = null;
            camera.lock();
        }
    }


    private String currentTimeStamp()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String currentTime = simpleDateFormat.format(new Date());
        return currentTime;
    }

    private File getOutputFile(int mediaTypeVideo) {
        File dir = Environment.getExternalStorageDirectory();
        String timeStamp = currentTimeStamp();

        if (mediaTypeVideo == MEDIA_TYPE_VIDEO)
        {
            return new File(dir.getPath() + File.separator + "VID_" +timeStamp+".3gp");
        }else
        {
            return null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            camera = Camera.open();
        }catch (Exception e) {
        }
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        parameters.setPreviewSize(352,288);
        parameters.setPreviewFrameRate(20);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}