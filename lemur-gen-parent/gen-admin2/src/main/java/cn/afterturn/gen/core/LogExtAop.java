package cn.afterturn.gen.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * 日志记录 打印需要的信息,支持快速定位到具体的文件
 *
 * @author fengshuonan
 * @date 2016年12月6日 下午8:48:30
 */
@Aspect
@Component
//@Slf4j
public class LogExtAop {
    @Pointcut(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void cutLogService() {
    }

    @Around("cutLogService()")
    public Object recordSysLog(ProceedingJoinPoint point) throws Throwable {
//        boolean logOut = ConfigurationKit.CONFIG.getBoolean("log.out", false);
        boolean logOut = true;
        if (logOut) { // 开启日志
            // 记录请求执行时间
            long start = System.currentTimeMillis();
            Object result = point.proceed();
            long end = System.currentTimeMillis();
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                Annotation[] annotations = method.getAnnotations();
                // 记录日志文件定位信息
                LogExtModel logExtModel = new LogExtModel(method);
                // 记录日志方法
                logExtModel.setMethodArgs(point.getArgs());
                // 记录文件返回值
                logExtModel.setResult(result);
                // 记录方法请求url
                logExtModel.setUrl(request.getRequestURL().toString()); // 获得方法进来的url
                logExtModel.setIp(request.getRemoteAddr()); // 获得方法进来的ip
                logExtModel.setExecuteTime(LogExtUtil.getTime(start, end)); // 方法的执行时间
                logExtModel.setAnnotations(annotations);
                logExtModel.setRequestArgs(LogExtUtil.getRequestQueryString(request));
                System.err.println(logExtModel.toString());
            } catch (Exception e) {
//                log.error("日志记录出错!", e);
            }
            return result;
        } else {
            return point.proceed(); // 否则什么都不管
        }
    }
}

/**
 * 
 * @ClassName LogExtModel
 * @author <a href="892042158@qq.com" target="_blank">于国帅</a>
 * @date 2018年8月28日 下午4:27:19
 *
 */
//@Data
final class LogExtModel {
    private String url; // 访问的url
    private String requestArgs; // 请求的参数
    private String ip; // 到指定的ip
    private Object result; // 返回到前台的数据
    private Object[] methodArgs; // 方法的参数
    private String executeTime; // 方法执行时间
    private Annotation[] annotations; // 方法上的注解
    // ====================参考 StackTraceElement 对象
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

    public String getRequestArgs() {
        return requestArgs;
    }

    public void setRequestArgs(String requestArgs) {
        this.requestArgs = requestArgs;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object[] getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LogExtModel(Method method) {
        this.declaringClass = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.fileName = method.getDeclaringClass().getSimpleName() + ".java";
        this.lineNumber = 1;
    }

    // ====================参考 StackTraceElement 对象
    public String getUrlMaping() { // 获得映射的地址
        return declaringClass + "." + methodName + "(" + StringUtils.join(methodArgs, ", ") + ")";
    }

    public String getUrl() {
        return this.url + "   "
                + (isNativeMethod() ? "(Native Method)"
                        : (fileName != null && lineNumber >= 0 ? "(" + fileName + ":" + lineNumber + ")"
                                : (fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
    }

    @Override
    public String toString() {
        // 扩展模式控制扩展打印 基础模式控制基础
        String nextLine = "\r\n";
        String msg = "开发 debug 日志模式" + nextLine;
//        boolean baseFlag = ConfigurationKit.CONFIG.getBoolean("log.out.base", false);
        boolean baseFlag = true;
        // 基础日志记录
        if (baseFlag) {
            msg += "@请求url+文件定位===" + this.getUrl() + "点击左侧 () 可快速定位到文件" + nextLine;
            msg += "@映射类.方法(参数值) ===" + this.getUrlMaping() + nextLine;
            if (!Arrays.toString(this.annotations).contains("@org.springframework.web.bind.annotation.ResponseBody")) {
                if (this.result != null && this.result instanceof String) {
                    msg += "@项目磁盘路径===" + LogExtUtil.getProjectPath() + nextLine;
                    msg += "@跳转的页面===" + this.getView() + nextLine;
                }
            }
        }
        // 扩展日志记录
//        boolean extFlag = ConfigurationKit.CONFIG.getBoolean("log.out.ext", false);
//        if (extFlag) {
//            msg += "扩展日志===" + nextLine;
//            msg += "@ip===" + this.getIp() + nextLine;
//            msg += "@请求方法执行时间===" + this.executeTime;
//        }
        return msg;
    }

    private String getView() { // 检查返回值 如果符合一定的规则 那么补充对应的页面的后缀
        String suffix = ".jsp";
//        ConfigurationKit.CONFIG.getString("log.out.suffix", ".html");
        String result = "";
        if (this.result instanceof ModelAndView) { // 证明这里从ModelAndView 里面获取
            result = ((ModelAndView) this.result).getViewName();
        } else {
            result = (String) ObjectUtils.defaultIfNull(this.result.toString(), "");
            // 只打印文件名
//            result = StringUtils.substringAfterLast(result, "/"); 
            // 修改为打印全路径 便于支持搜索
            result = result == "" ? this.result.toString() : result;
        }
        return "(" + result + suffix + ")"; // 跳转的页面
    }

    public boolean isNativeMethod() {
        return lineNumber == -2;
    }

}

//@Slf4j
final class LogExtUtil {
    protected static final int DATA_COUNT_NUMER = 10 * 10000; // 每10w数据记录一次
    protected static final int SECOND = 1 * 1000; // 秒
    protected static final int MINUTE = 60 * 1000; // 分钟
    protected static final int HOUR = 60 * 60 * 1000; // 小时

    /**
     * 根据一定的规则展示合适的时间 / 1秒内 用 毫秒 1分钟内用秒 1小时内用分钟 2小时外 用小时
     * 
     * @Title getTime
     * @author 于国帅
     * @date 2018年7月11日 下午4:46:12
     * @param start
     * @param end
     * @return String
     */
    public static String getTime(long start, long end) {
        end = end - start;
        if (end < SECOND) {
            return end + "毫秒";
        } else if (end < MINUTE) {
            return end / SECOND + "秒";
        } else if (end < HOUR) { // * 2
            return end / MINUTE + "分" + (end % MINUTE) / SECOND + "秒";
        } else {
            return end / HOUR + "小时" + (end % HOUR) / MINUTE + "分";
        }
    }

    /**
     * 描述:获取 request 中请求的内容
     * 
     * <pre>
     *  
     * 举例：
     * </pre>
     * 
     * @param request
     * @return
     * @throws IOException
     */
    public static String getRequestQueryString(HttpServletRequest request) throws IOException {
        String submitMehtod = request.getMethod();
        // GET
        if (submitMehtod.equals("GET")) {
            submitMehtod = request.getQueryString();
            return submitMehtod == null ? null : new String(request.getQueryString().getBytes("iso-8859-1"), "utf-8");
        } else {
            return getRequestPostStr(request);
        }
    }

    /***
     * 获取 request 中 json 字符串的内容
     * 
     * @param request
     * @return : <code>byte[]</code>
     * @throws IOException
     */
    public static String getRequestJsonString(HttpServletRequest request) throws IOException {
        String submitMehtod = request.getMethod();
        // GET
        if (submitMehtod.equals("GET")) {
            submitMehtod = request.getQueryString();
            return submitMehtod == null ? null
                    : new String(request.getQueryString().getBytes("iso-8859-1"), "utf-8").replaceAll("%22", "\"");
            // POST
        } else {
            return getRequestPostStr(request);
        }
    }

    /**
     * 描述:获取 post 请求的 byte[] 数组
     * 
     * <pre>
     *  
     * 举例：
     * </pre>
     * 
     * @param request
     * @return
     * @throws IOException
     */
    public static byte[] getRequestPostBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength;) {

            int readlen = request.getInputStream().read(buffer, i, contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    /**
     * 描述:获取 post 请求内容
     * 
     * <pre>
     *  
     * 举例：
     * </pre>
     * 
     * @param request
     * @return
     * @throws IOException
     */
    public static String getRequestPostStr(HttpServletRequest request) throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }

    public static String getProjectPath() {
        return getSystemProperty("user.dir");
    }

    private static String getSystemProperty(String property) {
        try {
            return System.getProperty(property);
        } catch (SecurityException ex) {
//            // we are not allowed to look at this property
//            log.warn("Caught a SecurityException reading the system property '" + property
//                    + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }
}