package com.speakerz.model.network.threads.audio.util;

import org.apisc.ytdecode.DownloadUrlResolver;
import org.apisc.ytdecode.VideoInfo;

import java.util.List;

public class YouTubeStreamAPI {
    public static void printInfo(VideoInfo info){
        System.out.println(
                "\t" + info.FormatCode + " " + info.AudioType + " " + info.VideoType + " " + info.Resolution);
        System.out.println(info.DownloadUrl);
    }
 public void play(String youTubeID) {
        // if(args.length == 0) args = new String[] {"yqZ-WdclDoU"};

      //  List<VideoInfo> infos = DownloadUrlResolver.GetDownloadUrls("https://www.youtube.com/watch?v=TW9d8vYrVFQ");
        List<VideoInfo> infos = DownloadUrlResolver.GetDownloadUrls("TW9d8vYrVFQ");

        printInfo(infos.get(0));

        // Ez leválogatja az audio streamet a listából




        /*try {
            YoutubeBufferedStream s = new YoutubeBufferedStream(infos.get(0).DownloadUrl);
            FileOutputStream fs = new FileOutputStream("C:\\Users\\Bendeguz\\result.mp4");
            byte[] buffer = new byte[1024*8];
            int l;
            while((l = s.read(buffer))> 0){
                fs.write(buffer, 0, l);
            }
            fs.close();

            System.out.println("done");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }
}
