## FilterPlayer注意事项

### AndroidIO

1. 新增AndroidIO接口的实现：ReadByteIO，使用内存队列LinkedBlockingDeque缓存获取的数据。
2. 当`videoRenderer.changeVideoPathAndPlay("")`接口中传入的路径为空时就会以AndroidIO的方式读取数据。
3. 该接口不支持MP4格式播放，seek时会出现moov问题。

### pushPacket

1. 新增pushVideoPacket和pushAudioPacket两个接口，可实现直接插入音视频裸数据到packet队列中播放。

2. 若需同时分别插入音视频packet，则需通过ReadByteIO接口播放初始flv文件。利用FFmpeg转换将h264文件转为flv文件命令：

   ```
   ffmpeg -i input.h264 -vf "fps=60" -video_track_timescale 1200000 output.flv
   ```

   `-vf "fps=60"`：设置输出视频的帧率为 60 fps

   `-video_track_timescale 1200000`：设置视频轨道的时间基为 1/1200000 秒

3. 分别插入音视频packet时需配置`mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)`，否则会出现没有音频包插入时播放会暂停的问题。

4. 硬解模式下不支持播放flv文件后同时插入音视频packet，只支持播放h264/AAC文件后单独插入视频/音频packet。



