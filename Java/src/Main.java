import java.io.Console;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Console console = System.console();
        System.out.print("请输入NetID：");
        String username = console.readLine();
        System.out.print("请输入密码：");
        String password = new String(console.readPassword());
        System.out.println("================================================================================");
        Demo demo = new Demo();
        try {
            String loginUrl = demo.loginCas(username, password);
            System.out.println("登录CAS成功");
            String hello = demo.loginSsfw(loginUrl);
            System.out.println("登录师生服务系统成功");
            System.out.println(hello);
            List<String[]> courses = demo.listCourse();
            System.out.println("读取课程列表成功");
            for (String[] course : courses) {
                try {
                    System.out.println("================================================================================");
                    System.out.println(course[0]);
                    if (!course[2].equals("评教")) {
                        continue;
                    }
                    demo.evalCourse(course[1]);
                    System.out.println("评教成功");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
