package com.example.demo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Demo {
    private static String htmlToText(String html) {
        html = Pattern.compile("<.*?>", Pattern.DOTALL).matcher(html).replaceAll(" ");
        html = html.replaceAll("&nbsp;", " ");
        return Pattern.compile("[\\s\\x0B\\xC2\\xA0]+", Pattern.DOTALL).matcher(html).replaceAll(" ").trim();
    }

    private Http http;

    public Demo() {
        http = new Http();
    }

    public String loginCas(String username, String password) throws Exception {
        String html = http.get("https://cas.xjtu.edu.cn/loginCas?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal", "读取CAS登录页面失败");
        MatchResult matches = Regex.match("name=\"lt\" value=\"(.*?)\".*?name=\"execution\" value=\"(.*?)\".*?name=\"_eventId\" value=\"(.*?)\"", html, "获取登录凭证失败");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("code", ""));
        params.add(new BasicNameValuePair("lt", matches.group(1)));
        params.add(new BasicNameValuePair("execution", matches.group(2)));
        params.add(new BasicNameValuePair("_eventId", matches.group(3)));
        html = http.post("https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal", params, "登录CAS失败");
        return Regex.match("url=(.*?)\"", html, "用户名或密码错误").group(1);
    }

    public String loginSsfw(String loginUrl) throws Exception {
        String html = http.get(loginUrl, "登录师生服务系统失败");
        return Regex.match("<li style=\"color:#fff;font-weight:bold;\">(.*?)</li>", html, "获取师生服务首页失败").group(1);
    }

    public List<String[]> listCourse() throws Exception {
        String html = http.get(
                "http://ssfw.xjtu.edu.cn/index.portal?.p=Znxjb20ud2lzY29tLnBvcnRhbC5zaXRlLmltcGwuRnJhZ21lbnRXaW5kb3d8ZjExNjF8dmlld3xub3JtYWx8YWN0aW9uPXF1ZXJ5",
                "读取课程列表失败");
        String table = Regex.match("<tbody>(.*?)</tbody>", html, "解析课程列表失败").group(1);
        List<MatchResult> courseMatches = Regex.matchAll("<tr.*?>(.*?)<a href=\"(.*?)\">(.*?)</a>", table, "解析课程列表失败");
        List<String[]> courses = new ArrayList<>();
        for (MatchResult courseMatch : courseMatches) {
            String[] course = new String[3];
            course[0] = htmlToText(courseMatch.group(1)).replace(' ', '\n');
            course[1] = "http://ssfw.xjtu.edu.cn/index.portal" + courseMatch.group(2);
            course[2] = courseMatch.group(3);
            courses.add(course);
        }
        return courses;
    }

    public void evalCourse(String url) throws Exception {
        String html = http.get(url, "读取课程信息失败");
        String action = "http://ssfw.xjtu.edu.cn/index.portal" + Regex.match("post\" action=\"(.*?)\"", html, "获取提交网址失败").group(1);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("wid_pgjxb", Regex.match("wid_pgjxb\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        params.add(new BasicNameValuePair("wid_pgyj", Regex.match("wid_pgyj\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        params.add(new BasicNameValuePair("type", "2"));
        params.add(new BasicNameValuePair("sfytj", Regex.match("sfytj\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        params.add(new BasicNameValuePair("pjType", Regex.match("pjType\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        params.add(new BasicNameValuePair("wid_pjzts", Regex.match("wid_pjzts\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        params.add(new BasicNameValuePair("status", Regex.match("status\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        params.add(new BasicNameValuePair("ztpj", "很好"));
        params.add(new BasicNameValuePair("sfmxpj", Regex.match("sfmxpj\" value=\"(.*?)\"", html, "解析课程失败").group(1)));
        List<MatchResult> trs = Regex.matchAll("教师评价(.*?)</tr>", html, "解析课程信息失败");
        for (MatchResult tr : trs) {
            MatchResult matches;
            params.add(new BasicNameValuePair("zbbm", Regex.match("zbbm\" type=\"hidden\" value=\"(.*?)\"", tr.group(1), "解析课程信息失败").group(1)));
            matches = Regex.match("(wid_.*?)\" type=\"hidden\" value=\"(.*?)\"", tr.group(1), "解析课程信息失败");
            params.add(new BasicNameValuePair(matches.group(1), matches.group(2)));
            matches = Regex.match("(qz_.*?)\" type=\"hidden\" value=\"(.*?)\"", tr.group(1), "解析课程信息失败");
            params.add(new BasicNameValuePair(matches.group(1), matches.group(2)));
            List<MatchResult> scoreMatches = Regex.matchAll("(pfdj_.*?)\"  value=\"(.*?)\"", tr.group(1), "解析课程信息失败");
            int i = new Random().nextInt(2);
            params.add(new BasicNameValuePair(scoreMatches.get(i).group(1), scoreMatches.get(i).group(2)));
        }
        params.add(new BasicNameValuePair("pgyj", "很好"));
        params.add(new BasicNameValuePair("actionType", "2"));
        http.post(action, params, "评教失败");
    }
}