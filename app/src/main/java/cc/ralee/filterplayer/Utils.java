package cc.ralee.filterplayer;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class Utils {
    //  private static final String p2pInt = "p2p-p2p0";
    private static final String p2pInt = "p2p-wlan0";
    public static final String TAG = "AndroidNetworkAddressFactory";

        public Utils() throws IOException {
        }

        @SuppressLint("LongLogTag")
        public static String getIPFromMac(String Mac) {
            Delay();//延时

            readArp();

            BufferedReader br = null;

            try {
                br = new BufferedReader(new FileReader("/proc/net/arp"));//1.	读ARP表
                List list = new ArrayList();
                int a;
                String line;
                a=getFileLineNum();
                System.out.println("arp lines = " + a);

                //System.out.println();

                Log.e(TAG, "读取表题");
                line =br.readLine();
                System.out.println(line );


                while(a-1>=1) {
                Log.e(TAG, "读取次数："+(a-1) );
                line =br.readLine();
                System.out.println(line );
                String[] splitted = line.split(" +");//2.	分割ARP表中每行内容；
                String mac = splitted[3];
                Log.e(TAG, "mac=" + mac);      //3. 获取表中设备的mac地址；
                String str1=Mac.substring(2, 12);   //由于Galaxy读出来Mac地址出现一位误差，故取前12位进行比较
                Log.e(TAG, "str1："+str1 );
                String str2=mac.substring(2, 12);
                Log.e(TAG, "str2："+str2 );
                if (str1.equals(str2))             //4.  Mac地址进行比较；
                {
                    Log.e(TAG, "进来了4");
                    String GCip = splitted[0];     //5.	从分割内容中找到mac地址对应设备的IP地址。
                    return GCip;
                }
                a=a-1;
                }

            } catch (Exception var16) {
                var16.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException var15) {
                    var15.printStackTrace();
                }

            }

            return null;
        }


        public static void Delay()

        {try
        {
            Thread.currentThread().sleep(1000);//毫秒
        }
        catch(Exception e){}
        }

    public static int getFileLineNum() {
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader("/proc/net/arp"))){
            lineNumberReader.skip(Long.MAX_VALUE);
            int lineNumber = lineNumberReader.getLineNumber();
            return lineNumber + 1;//实际上是读取换行符数量 , 所以需要+1
        } catch (IOException e) {
            return -1;
        }
    }


        @SuppressLint("LongLogTag")
        public static String getLocalIPAddress() {
            try {
                Enumeration en = NetworkInterface.getNetworkInterfaces();

                while(en.hasMoreElements()) {
                    Log.e(TAG, "进来了1" );
                    NetworkInterface intf = (NetworkInterface)en.nextElement();
                    Enumeration enumIpAddr = intf.getInetAddresses();

                    while(enumIpAddr.hasMoreElements()) {
                        Log.e(TAG, "进来了2" );
                        InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
                        String iface = intf.getName();
                      //  if (iface.matches(".*p2p-p2p0.*") && inetAddress instanceof Inet4Address) {
                            Log.e(TAG, "进来了3" );
                            return getDottedDecimalIP(inetAddress.getAddress());

                    }
                }
            } catch (SocketException var5) {
                Log.e(TAG, "getLocalIPAddress()", var5);
            } catch (NullPointerException var6) {
                Log.e(TAG, "getLocalIPAddress()", var6);
            }

            return null;
        }

        private static String getDottedDecimalIP(byte[] ipAddr) {
            String ipAddrStr = "";

            for(int i = 0; i < ipAddr.length; ++i) {
                if (i > 0) {
                    ipAddrStr = ipAddrStr + ".";
                }

                ipAddrStr = ipAddrStr + (ipAddr[i] & 255);
            }

            return ipAddrStr;
        }


        static private void readArp() {
            try {
                BufferedReader br = new BufferedReader(
                        new FileReader("/proc/net/arp"));
                String line = "";
                String ip = "";
                String flag = "";
                String mac = "";

                while ((line = br.readLine()) != null) {
                    try {
                        line = line.trim();
                        if (line.length() < 63) continue;
                        if (line.toUpperCase(Locale.US).contains("IP")) continue;
                        ip = line.substring(0, 17).trim();
                        flag = line.substring(29, 32).trim();
                        mac = line.substring(41, 63).trim();
                        if (mac.contains("00:00:00:00:00:00")) continue;
                        Log.e("scanner", "readArp: mac= "+mac+" ; ip= "+ip+" ;flag= "+flag);


                    } catch (Exception e) {
                    }
                }
                br.close();

            } catch(Exception e) {
            }
        }


    }

