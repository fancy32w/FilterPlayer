package cc.ralee.filterplayer;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * MediaPlayer plays video to a surface ,then use OpenGL renderer the surface
 */


public class MediaPlayerWithSurface implements  IjkMediaPlayer.OnPreparedListener {
    public static final String TAG = "MediaPlayerWithSurface";
    private IjkMediaPlayer mediaPlayer;
    private String videoPath;
    private Surface mSurface;
    private SeekBar seekBar;
    private boolean isPlaying = false ;
    public MediaPlayerWithSurface(String videoPath, Surface mSurface, SeekBar seekBar) {
        this.mSurface = mSurface;
        this.videoPath = videoPath;
        mediaPlayer = new IjkMediaPlayer();
        this.seekBar = seekBar;
    }
    public void playVideoToSurface() {
        preparePlayer();
    }
    public void changeVideoPath(String newVideoPath) {
        this.videoPath = newVideoPath;


    }
    private byte[] readFrameData(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "File does not exist: " + filePath);
            return null;
        }

        long fileLength = file.length();
        if (fileLength == 0) {
            Log.e(TAG, "File is empty: " + filePath);
            return null;
        }

        byte[] frameData = new byte[(int) fileLength];
//        Log.d(TAG, "File length: " + fileLength);

        try (InputStream inputStream = new FileInputStream(file)) {
            int bytesRead = inputStream.read(frameData);
            if (bytesRead != frameData.length) {
                throw new IOException("Could not read the entire file.");
            }
        }
        return frameData;
    }

    private void preparePlayer() {
        mediaPlayer.reset();
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setLooping(true);

        if(!videoPath.equals("")) {
            File file = new File(videoPath);
            boolean rtpFlag = false;
            if (videoPath.startsWith("rtp")) {
                rtpFlag = true;
            }
            try {
                if(file.exists()) {
                    mediaPlayer.setDataSource(file.getPath());
                }else{
                    mediaPlayer.setDataSource(videoPath);
                }

            if (rtpFlag) {
                // Param for rtp-living
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
                //设置丢帧阈值
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                //设置无 packet 缓存
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
                // 预加载
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);//实时性
                // 设置探测包数量
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024);// 该参数调小可解决卡顿问题以及起播时长问题 但是过小会导致起播失败需多次起播
                // 设置分析流时长
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 10000); //调小可减短起播时长
                //启用快速播放模式 (减少启动时间，降低播放延迟)
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);


            } else {
                // Param for playback
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
            }

            //开启硬编码
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            // 启用HTTP服务器范围请求检测
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 1);
            // 启用数据包刷新
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
            // 设置视频帧率
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 30);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);
            //指定视频输出的像素格式
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12);
//            // 设置环路滤波
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);//48快但质量差，0慢但质量好
//            // 设置缓冲区大小
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "buffer_size", 1024);
//            // 设置最大缓存数量
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024*1024);
//            // 设置最小解码帧数
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 3);

            //设置音视频解码器格式格式
            //audio
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "ac", 2);                  //channels
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "ar", 48000);              //sample_rate
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "channel_layout", 3);      //channel_layout
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "frame_size", 1024);       //frame_size
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "request_sample_fmt", 8);  //sample_fmt
            //video
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "pixel_format", 0);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "video_size", 1920); //{"video_size", "set video size", OFFSET(width), AV_OPT_TYPE_IMAGE_SIZE, {.str=NULL}, 0, INT_MAX, 0 }

            //设置可循环播放
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek_at_start", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,"safe",0);

            //静音设置
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "an", 1);
            //设置tcp传输
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");

            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "ijkio,crypto,file,http,https,tcp,tls,udp");

            // 清空DNS,有时因为在APP里面要播放多种类型的视频(如:MP4,直播,直播平台保存的视频,和其他http视频), 有时会造成因为DNS的问题而报10000问题
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);


        } catch (IOException e) {
            e.printStackTrace();
        }


        }else{

//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

            final ReadByteIO rio = new ReadByteIO();
            rio.reset();
            // 启动一个新的线程来插入数据
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= 0; i++) {
                        String targetFilePath = String.format("/storage/self/primary/out3.flv");
                        byte[] frameData = new byte[0];
                        try {
                            frameData = readFrameData(targetFilePath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (frameData != null) {
                            rio.addLast(frameData);

                        }
                    }

                    beginPushPakect();

                }
            }).start();

            mediaPlayer.setAndroidIOCallback(rio);
            Uri uri = Uri.parse("ijkio:androidio:" + "/storage/self/primary/rcv_data"); // 设定我们自定义的 url
            try {
                mediaPlayer.setDataSource(uri.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mediaPlayer.prepareAsync();




    }
    public void beginPushPakect(){
        //开启插入音频包线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                    for (int i = 0; i <= 5; i++) {
                        String frameFilePath = String.format("/storage/self/primary/frames_aac/frame_%04d.aac", i);
                        try {
                            byte[] pushframeData = readFrameData(frameFilePath);
                            if (pushframeData != null) {
                                mediaPlayer.pushAudioPacket(pushframeData);
                            } else {
                                Log.d(TAG, "push fail!! Frame: " + frameFilePath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }



            }
        }).start();
        //开启插入视频包线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                    for (int i = 1; i <= 125; i++) {
                        String frameFilePath = String.format("/storage/self/primary/frames_h264/frame_%04d.264", i);
                        try {
                            byte[] pushframeData = readFrameData(frameFilePath);
                            if (pushframeData != null) {
                                mediaPlayer.pushVideoPacket(pushframeData);
                            } else {
                                Log.d(TAG, "push fail!! Frame: " + frameFilePath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


            }
        }).start();
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {

        mediaPlayer.setSurface(mSurface);
        mediaPlayer.start();
        isPlaying = true;


        seekBar.setProgress(0);
        seekBar.setMax((int)mediaPlayer.getDuration());
        Log.d("Player", "onprepared!! ");
//
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 0) {
                    if (fromUser) {
                        Log.d("Player", "seeekTo: "+progress);
                        mediaPlayer.seekTo(progress);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
/*
        final Boolean seekBarAutoFlag = true;
        // deal with the seekbar's move
        Thread moveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(seekBarAutoFlag) {
                        if( mediaPlayer != null && isPlaying) {
                            seekBar.setProgress((int)mediaPlayer.getCurrentPosition());
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
        //moveThread.start();

    }

    public void pausePlay() {
        Log.d("Player", "pausePlay: ");
        mediaPlayer.pause();
        isPlaying = false;
    }

    public void startPlay() {

        mediaPlayer.start();
        Log.d("Player", "startPlay: ");
        isPlaying = true;

    }

    public void restart() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
            mediaPlayer.setLooping(true);

        }catch (IOException e ) {
            e.printStackTrace();
        }
    }

    public void release() {
        if(mediaPlayer != null ) {
            mediaPlayer.release();
            mediaPlayer = null ;
        }
    }

}
