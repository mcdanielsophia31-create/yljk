import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordChecker {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的哈希
        String hashedPassword = \
.qCf2.arFKrjVK89g.kkFK.u61pgXTAQD/Lru56o177OF3GIy\;
        
        // 验证密码
        boolean isMatch123456 = encoder.matches(\123456\, hashedPassword);
        boolean isMatchDefault = encoder.matches(\\, hashedPassword);
        
        System.out.println(\密码
123456
是否匹配:
\ + isMatch123456);
        System.out.println(\默认密码是否匹配:
\ + isMatchDefault);
        System.out.println(\数据库中的哈希:
\ + hashedPassword);
    }
}
