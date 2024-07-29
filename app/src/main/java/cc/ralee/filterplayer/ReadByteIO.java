package cc.ralee.filterplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import tv.danmaku.ijk.media.player.misc.IAndroidIO;

public class ReadByteIO implements IAndroidIO {

    private static ReadByteIO instance;
    private static final String TAG = ReadByteIO.class.getSimpleName();
    private  LinkedBlockingDeque<Byte> flvData = new LinkedBlockingDeque<>(); // 内存队列，用于缓存获取到的流数据，要实现追帧效果，只需要根据策略丢弃本地缓存的内容即可

    private int file_size;
    public ReadByteIO() {

    }

    private byte[] takeFirstWithLen(int len) {  // 取 byte 数据用于界面渲染
        byte[] byteList = new byte[len];
        for (int i = 0; i < len; i++) {
            try {
                byteList[i] = flvData.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "****debugdebug:takeFirstWithLen" );
        return byteList;
    }

//    public synchronized boolean addLast(byte[] bytes) {
//        Log.i(TAG, "tempList size:"+ bytes.length );
//        for (byte b : bytes) {
//            flvData.add(b);
//        }
//        return true;
//    }

    public synchronized boolean addLast(byte[] bytes) {
        ArrayList<Byte> tempList = new ArrayList<>(bytes.length);
        for (byte b : bytes) {
            tempList.add(b);
        }
        Log.i(TAG, "tempList size:"+ tempList.size() );
            flvData.addAll(tempList);
        return true;
    }

    public void setSize(int file_size) {
        this.file_size=file_size;

    }
    // 如果是播放本地文件，可在此处打开文件流，后续读取文件流即可
    @Override
    public int open(String url) {
//        if (URL_SUFFIX.equals(url)) {
//            return 1; // 打开播放流成功
//        }
//        return -1; // 打开播放流失败
        return 1;
    }
//
    @Override
    public int read(byte[] buffer, int size) throws InterruptedException {
        byte[] tmpBytes = takeFirstWithLen(size); // 阻塞式读取，没有数据不渲染画面
        System.arraycopy(tmpBytes, 0, buffer, 0, size);
        Log.i(TAG, "****read , size=" + size + " , bufferLength = " + buffer.length );
        return size;
    }


    @Override
    public long seek(long offset, int whence) {
        if (whence == 0x10000){
            return this.file_size;}
        return 0;
    }

    @Override
    public int close() {
        return 0;
    }
    public void reset() {
        flvData.clear();
    }
}
