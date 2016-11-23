import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
    public static MatchResult match(String pattern, String string, String errMsg) throws Exception {
        try {
            Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = r.matcher(string);
            if (m.find()) {
                return m.toMatchResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception(errMsg);
    }

    public static List<MatchResult> matchAll(String pattern, String string, String errMsg) throws Exception {
        try {
            Pattern r = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher m = r.matcher(string);
            List<MatchResult> results = new ArrayList<>();
            while (m.find()) {
                results.add(m.toMatchResult());
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception(errMsg);
    }
}
