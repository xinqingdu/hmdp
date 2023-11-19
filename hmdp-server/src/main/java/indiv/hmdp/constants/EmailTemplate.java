package indiv.hmdp.constants;

public class EmailTemplate {

    private static final String MESSAGE_PREFIX = "【HMDP】";
    private static final String MESSAGE_BODY = "您本次的验证码是";
    public static final String EMAIL_KEY = "email-login";

    public static final Integer VALIDATE_TIME = 5;
    public static final String EMAIL_SUBJECT = "HMDP Code";

    public static final String CONTENT_TYPE = "text/html;charset=utf-8";
    private static final String MESSAGE_SUFFIX = "分钟内有效，请勿向其他人提供验证码,感谢关注！";

    public static String getEmailMessage(String code, Integer time) {
        return MESSAGE_PREFIX + MESSAGE_BODY + code + "," + time + MESSAGE_SUFFIX;
    }
}
