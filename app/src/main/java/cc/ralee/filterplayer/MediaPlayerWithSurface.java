package cc.ralee.filterplayer;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;

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
    private void preparePlayer() {
        mediaPlayer.reset();
        File file = new File(videoPath);
        Log.d("Moive File", "videoPath:" + videoPath);
        Log.d("Moive File", "Exists:" + file.exists());
        Log.d("Moive File", "filePath:" + file.getPath());


        try {
            if(file.exists()) {
                mediaPlayer.setDataSource(file.getPath());
            }else{
                mediaPlayer.setDataSource(videoPath);
            }
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setLooping(true);




            if (true) {
                // Param for living
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            } else {
                // Param for playback
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 0);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);
            }

            //开启硬编码
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);

            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 1);

            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fast", 1);//不额外优化

            // 设置丢帧阈值
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            // 设置视频帧率
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 15);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12);

            // 设置环路滤波
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);//48快但质量差，0慢但质量好

            // 设置无 packet 缓存
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "buffer_size", 1024);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");
            // 不限制拉流缓存大小
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", "1");
            // 设置最大缓存数量
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024*1024);
            // 设置最小解码帧数
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 3);
            // 预加载
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);//实时性
            // 设置探测包数量
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024);// 该参数调小可解决卡顿问题以及起播时长问题 但是过小会导致起播失败需多次起播
            // 设置分析流时长
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 10000);//起播问题相关
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 10000);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);


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



            //静音设置
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "an", 1);
            //设置tcp传输
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");

//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,rtp,rtsp");
//            // 清空DNS,有时因为在APP里面要播放多种类型的视频(如:MP4,直播,直播平台保存的视频,和其他http视频), 有时会造成因为DNS的问题而报10000问题
//            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
//
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "seek_at_start", 1);
            mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPrepared(IMediaPlayer mp) {

        mediaPlayer.setSurface(mSurface);
        mediaPlayer.start();
        isPlaying = true;
        seekBar.setProgress(0);
        seekBar.setMax((int)mediaPlayer.getDuration());
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
