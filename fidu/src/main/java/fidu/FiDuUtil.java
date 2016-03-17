package fidu;

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


}
