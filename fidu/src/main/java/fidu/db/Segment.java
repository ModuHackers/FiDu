package fidu.db;

/**
 * 下载分片
 * <p/>
 * Created by fengshzh on 16/3/17.
 */
public class Segment {
    public String file;
    public String url;
    public int totalSegments;
    public int segmentNum;
    public long start;
    public long end;
    public int complete; // 0未完成,1完成
}