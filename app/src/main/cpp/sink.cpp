#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <net/if.h>
#include <arpa/inet.h>
#include <unistd.h>

#include <android/log.h>
#define LOG_TAG  "******C_TAG"
#define PRINT(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#include "sink.h"




#define SESSIONIDBUFSIZE  64
#define BUFSIZE 1024
static const char m7_req_3[] = "\r\n\r\n";
static const char m1_options_rsp[] = "RTSP/1.0 200 OK\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 1\r\nPublic: org.wfa.wfd1.0, GET_PARAMETER, SET_PARAMETER\r\n\r\n";
static const char m2_req[] = "OPTIONS * RTSP/1.0\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 2\r\nRequire: org.wfa.wfd1.0\r\n\r\n";
static const char m3_body[] = "wfd_video_formats: 00 00 02 02 000000FF 00000000 00000000 00 0000 0000 00 none none\r\nwfd_audio_codecs: LPCM 00000002 00, AAC 00000001 00\r\nwfd_client_rtp_ports: RTP/AVP/UDP;unicast 50000 0 mode=play\r\nwfd_connector_type: 07\r\n";
static const char m3_rsp_1[] = "RTSP/1.0 200 OK\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 2\r\nContent-Type: text/parameters\r\nContent-Length: ";
static const char m4_rsp[] = "RTSP/1.0 200 OK\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 3\r\n\r\n";
static const char m5_rsp[] = "RTSP/1.0 200 OK\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 4\r\n\r\n";
static const char m6_req_1[] = "SETUP rtsp://";
static const char m6_req_2[] = "/wfd1.0/streamid=0 RTSP/1.0\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 4\r\nTransport: RTP/AVP/UDP;unicast;client_port=50000\r\n\r\n";
static const char m7_req_1[] = "PLAY rtsp://";
static const char m7_req_2[] = "/wfd1.0/streamid=0 RTSP/1.0\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: 5\r\nSession: ";

static const char m16_rsp_1[] = "RTSP/1.0 200 OK\r\nDate: Sun, 11 Aug 2013 04:41:40 +000\r\nServer: stagefright/1.2 (Linux;Android 4.3)\r\nCSeq: ";


static int GetSessionID(char* pSrc, char* pDst)
{
    char * session = "Session: ";
    char * pstr;
    char * ptemp = pDst;
    int count = 0;
    pstr = strstr(pSrc,session);
    //  sprintf(ptemp,"%s",session);
    // ptemp += strlen(session);
    pstr += strlen(session);
    if(!pstr)
        return -1;
    while(*pstr)
    {
        if(isdigit(*pstr))
        {
            ptemp[count++] = *pstr;
            pstr++;
        }
        else
            break;
    }
    ptemp[count] =  '\0';
    return 0;
}


int sink(char* ip, int port)
{
    struct sockaddr_in src;
    struct ifreq if_wlan;
    int fd, len, count, tmplen;
    char opt[]="wlan0";
    int portnum= port==0 ? 7236 : port;
    int cseq = 5;
    int keepalives_sent = 0;
    char * req_teardown= "TEARDOWN";
    char recvbuf[BUFSIZE] = {0};
    if ((fd=socket(AF_INET, SOCK_STREAM, 0))<0)//ipv4,SOCK_STREAM(TCP) we use TCP
    {
        printf("create socket failed!\n");
        return -1;
    }

    memset(&src, 0, sizeof(src));
    src.sin_family = AF_INET;
    src.sin_port = htons(portnum);
    src.sin_addr.s_addr = inet_addr(ip);

    /*connect socket to server's internet address*/
    if (connect(fd, (struct sockaddr*)&src, sizeof(src))<0)
    {
        close(fd);
        printf("connect failed!\n");
        return -1;
    }
    printf("connected to server\n");

    strncpy(if_wlan.ifr_name, opt, IFNAMSIZ);
/*

    if (setsockopt(fd, SOL_SOCKET, SO_BINDTODEVICE, (char *)&if_wlan, sizeof(if_wlan)) < 0)
    {
        close(fd);
        printf("setsockopt failed!\n");
        return -1;
    }
*/

    //Wait to m1 receive data
    memset(recvbuf,0,BUFSIZE);
    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0) //receive server-side message
    {
        close(fd);
        printf("receive data error!\n");
        return -1;
    }
    printf("M1 Received:\n%s\n",recvbuf);//print server-side message

    //Send m1 response
    count = 0;
    tmplen = strlen(m1_options_rsp);
    printf("M1 send:\n%s\n",m1_options_rsp);//print m1 send
    while(1)
    {
        len = send(fd, m1_options_rsp + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send m1_options_rsp error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }
    //Send m2 request
    count = 0;
    tmplen = strlen(m2_req);
    printf("M2 send:\n%s\n",m2_req);//print m2 send
    while(1)
    {
        len = send(fd, m2_req + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send m2 request error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }

    //Receive m2 response
    memset(recvbuf,0,BUFSIZE);
    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0)
    {
        close(fd);
        printf("receive m2 response error!\n");
        return -1;
    }
    printf("M2 Received:\n%s\n",recvbuf);

#if 1
    //Receive: M3 GET_PARAMETER Request
    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0)
    {
        close(fd);
        printf("receive M3 GET_PARAMETER Request error!\n");
        return -1;
    }
    printf("M3 Req:\n%s\n",recvbuf);
#endif

    //Send: M3 Response
    memset(recvbuf,0,BUFSIZE);
    len = sprintf(recvbuf,"%s%d%s%s",m3_rsp_1,strlen(m3_body),m7_req_3,m3_body);

    printf("M3 send:\n%s\n",recvbuf);

    count = 0;
    tmplen = len;
    while(1)
    {
        len = send(fd, recvbuf + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send M3 Response error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }

    //Receive: M4 RTSP SET_PARAMETER Request
//    memset(recvbuf,0,BUFSIZE);
//    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0)
//    {
//        close(fd);
//        printf("receive  M4 RTSP SET_PARAMETER Request error!\n");
//        return -1;
//    }
//    printf("M4 Received:\n%s\n",recvbuf);
//
//
//    printf("M4 send:\n%s\n",m4_rsp);
// 接收 M4 RTSP SET_PARAMETER Request
    memset(recvbuf, 0, BUFSIZE);
    int recv_len = recv(fd, recvbuf, BUFSIZE - 1, 0);  // 预留1个字节用于字符串结束符
    if (recv_len < 0) {
        close(fd);
        printf("receive M4 RTSP SET_PARAMETER Request error!\n");
        return -1;
    } else if (recv_len == 0) {
        close(fd);
        printf("connection closed by server\n");
        return -1;
    }
    recvbuf[recv_len] = '\0';
    printf("M4 Received:\n%s\n", recvbuf);


    //Send: M4 response
    count = 0;
    tmplen = strlen(m4_rsp);
    while(1)
    {
        len = send(fd, m4_rsp + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send M4 response error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }

    //Receive: M5 RTSP SET_PARAMETER Request (setup)
    memset(recvbuf,0,BUFSIZE);
    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0)
    {
        close(fd);
        printf("receive  M5 RTSP SET_PARAMETER Request (setup) error!\n");
        return -1;
    }
    printf("M5 Received:\n%s\n",recvbuf);
	
    //Send: M5 Response
    count = 0;
    tmplen = strlen(m5_rsp);
    printf("M5 send:\n%s\n",m5_rsp);
    while(1)
    {
        len = send(fd, m5_rsp + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send M5 Response error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }
	
    //Send: M6 RTSP SETUP  ip = char
    memset(recvbuf,0,BUFSIZE);
    len = sprintf(recvbuf,"%s%s%s",m6_req_1,ip,m6_req_2);
    printf("M6 send:\n%s\n",recvbuf);
    count = 0;
    tmplen = len;
    while(1)
    {
        len = send(fd, recvbuf + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send M6 RTSP SETUP  ip  error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }

    //Receive: M6 RTSP SETUP response
    memset(recvbuf,0,BUFSIZE);
    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0)
    {
        close(fd);
        printf("receive   M6 RTSP SETUP response error!\n");
        return -1;
    }
    printf("M6 Received:\n%s\n",recvbuf);

    //TODO: extract session ID ,add the number
    char sessionid[SESSIONIDBUFSIZE] = {0};
    if(GetSessionID(recvbuf, sessionid))
    {
        printf("have no session id\n");
        return -1;
    }

    //Send: M7 Request
    memset(recvbuf,0,BUFSIZE);
    len = sprintf(recvbuf,"%s%s%s%s%s",m7_req_1,ip,m7_req_2,sessionid,m7_req_3);
    printf("M7 send:\n%s\n",recvbuf);
	
    count = 0;
    tmplen = len;
    while(1)
    {
        len = send(fd, recvbuf + count, tmplen -count , 0);
        if(len < 0)
        {
            close(fd);
            printf("send M6 Request  error!\n");
            return -1;
        }
        else if(len < tmplen -count)
            count += len;
        else
            break;
    }

    //Receive:M7 Rsp
    memset(recvbuf,0,BUFSIZE);
    if((recv(fd, recvbuf, BUFSIZE, 0))  < 0)
    {
        close(fd);
        printf("receive  M7 Rsp error!\n");
        return -1;
    }
    printf("M7 Received:\n%s\n",recvbuf);
    cseq = 5;

    keepalives_sent = 0;

    while(1)
    {

        if((len = (recv(fd, recvbuf, BUFSIZE, 0))) <= 0)
        {
            printf("socket closed!\n");
            break;
        }
        recvbuf[len] = '\0';
        printf("Receive req :\n%s\n",recvbuf);

        //match if there is TEARDOWN
        if (strstr(recvbuf,req_teardown))
        {
            printf("Teardown received!\n");
            break;
        }

        //Send m16_rsp
        len = sprintf(recvbuf,"%s%d%s",m16_rsp_1,cseq,m7_req_3);

        count = 0;
        tmplen = len;
        while(1)
        {
            len = send(fd, recvbuf + count, tmplen -count , 0);
            if(len < 0)
            {
                close(fd);
                printf("send keepalive  error!\n");
                return -1;
            }
            else if(len < tmplen -count)
                count += len;
            else
                break;
        }


        //   printf("Send m16_rsp : %s\n",m16_tmp);
        printf("sent keepalive!\r\n\r\n\r\n");

        cseq += 1;
        keepalives_sent += 1;
    }

    close(fd);//close socket
    return 0;
}
