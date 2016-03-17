package fidu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static fidu.FiDuConstant.CONTENT_TYPE_AVI;
import static fidu.FiDuConstant.CONTENT_TYPE_JPEG;
import static fidu.FiDuConstant.CONTENT_TYPE_PDF;
import static fidu.FiDuConstant.CONTENT_TYPE_STREAM;

/**
 * 工具类
 * <p/>
 * Created by fengshzh on 16/3/16.
 */
public final class FiDuUtil {

    /**
     * 根据文件名猜测MIME
     *
     * @param file 文件名
     * @return MIME
     */
    public static String guessFileType(String file) {
        String type = CONTENT_TYPE_STREAM;
        if (file != null) {
            if (file.endsWith("pdf")) {
                type = CONTENT_TYPE_PDF;
            } else if (file.endsWith("jpeg")) {
                type = CONTENT_TYPE_JPEG;
            } else if (file.endsWith("avi")) {
                type = CONTENT_TYPE_AVI;
            }
            // TODO 可扩展上传支持的文件类型
        }
        return type;
    }

    public static void assembleSegments(String file, int segments) {
        File completeFile = new File(file);
        completeFile.delete();
        File firstSegment = new File(file + "_" + 0);
        firstSegment.renameTo(completeFile);
        try {
            FileOutputStream out = new FileOutputStream(file, true);
            for (int i = 1; i < segments; i++) {
                File segmentFile = new File(file + "_" + i);
                InputStream in = new FileInputStream(segmentFile);
                byte[] buf = new byte[2048];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
                segmentFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSegments(String file, int segments) {
        for (int i = 0; i < segments; i++) {
            File segmentFile = new File(file + "_" + i);
            segmentFile.delete();
        }
    }

}
