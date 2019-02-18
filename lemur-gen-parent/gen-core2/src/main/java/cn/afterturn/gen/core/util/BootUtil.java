package cn.afterturn.gen.core.util;

import org.apache.commons.lang.WordUtils;

/**
 * 对代码生成器的 额外的扩展，因为guns-core 的 toolUtil DE jar 的不太方便修改的原因
 * 
 * @ClassName BootUtil
 * @author <a href="892042158@qq.com" target="_blank">于国帅</a>
 * @date 2018年12月6日 下午4:50:06
 *
 */
public class BootUtil {
    /**
     * 首字母小写 ,工具类已经做了null 处理了
     * 
     * @Title uncap
     * @author 于国帅
     * @date 2018年12月6日 下午4:54:42
     * @return String
     */
    public static String uncap(String str) {
        return WordUtils.uncapitalize(str);
    }

    /**
     * 首字母小写 ，并且删除第一个字母 ，作为接口的标准名称
     * 
     * @Title uncap
     * @author 于国帅
     * @date 2018年12月6日 下午4:54:42
     * @return String
     */
    public static String uncapInterface(String str) {
        if (str != null && str.length() > 1) {
            str = str.substring(1);
            str = WordUtils.uncapitalize(str);
        }
        return str;
    }

    /*  public static void main(String[] args) {
        // 首字母小写
        String s = BootUtil.uncapInterface("ITSystemUserService");
        System.err.println(s);
        System.exit(0);
    }*/
}
