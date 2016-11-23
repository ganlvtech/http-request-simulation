<?php
define('USERNAME', '改成你的NetID');
define('PASSWORD', '改成你的密码');
define('COOKIE_FILE', 'cookie.txt');
function curl($url, $postfields = null, $err_msg = '发送请求失败')
{
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_COOKIEFILE, COOKIE_FILE);
    curl_setopt($ch, CURLOPT_COOKIEJAR, COOKIE_FILE);
    if (substr($url, 0, 8) === 'https://') {
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    }
    if (!is_null($postfields)) {
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/x-www-form-urlencoded',
        ));
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postfields);
        curl_setopt($ch, CURLOPT_POST, true);
    }
    curl_setopt($ch, CURLOPT_ENCODING, 'gzip,deflate');
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_MAXREDIRS, 10);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_PROXY, '127.0.0.1:8888');
    $content = curl_exec($ch);
    curl_close($ch);
    if (false === $content) {
        throw new Exception($err_msg . curl_getinfo($ch));
    }
    return $content;
}
function &match($pattern, $subject, $err_msg = '正则匹配错误')
{
    if (1 !== preg_match($pattern, $subject, $matches)) {
        throw new Exception($err_msg);
    }
    return $matches;
}
function &match_all($pattern, $subject, $err_msg = '正则匹配错误')
{
    if (false === preg_match_all($pattern, $subject, $matches, PREG_SET_ORDER)) {
        throw new Exception($err_msg);
    }
    return $matches;
}
function html_to_text($html)
{
    return trim(preg_replace('/[\s\0\x0B\xC2\xA0]+/su', ' ', html_entity_decode(preg_replace('/<.*?>/su', ' ', $html))));
}
function output($str)
{
    echo $str, "\n";
}
// 主程序
try {
    if (file_exists(COOKIE_FILE)) {
        unlink(COOKIE_FILE);
    }
    $html = curl('https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal', null, '读取CAS登录页面失败');
    $matches = match('/name="lt" value="(.*?)".*?name="execution" value="(.*?)".*?name="_eventId" value="(.*?)"/s', $html, '获取登录凭证失败');
    $html = curl('https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal', http_build_query(array(
        'username' => USERNAME,
        'password' => PASSWORD,
        'code' => '',
        'lt' => $matches[1],
        'execution' => $matches[2],
        '_eventId' => $matches[3],
        'submit' => '登录',
    )), '登录CAS失败');
    $url = match('/url=(.*?)"/', $html, '用户名或密码错误')[1];
    output('登录CAS成功');
    $html = curl($url, '登录师生服务系统失败');
    $hello = match('/<li style="color:#fff;font-weight:bold;">(.*?)<\\/li>/', $html, '获取师生服务首页失败')[1];
    output('登录师生服务系统成功');
    output($hello);
    $html = curl('http://ssfw.xjtu.edu.cn/index.portal?.p=Znxjb20ud2lzY29tLnBvcnRhbC5zaXRlLmltcGwuRnJhZ21lbnRXaW5kb3d8ZjExNjF8dmlld3xub3JtYWx8YWN0aW9uPXF1ZXJ5', null, '读取课程列表失败');
    $table = match('/<tbody>(.*?)<\\/tbody>/su', $html, '解析课程列表失败')[1];
    $courses = match_all('/<tr.*?>(.*?)<a href="(.*?)">(.*?)<\\/a>/su', $table, '解析课程列表失败');
    output('读取课程列表成功');
    foreach ($courses as $course) {
        try {
            output(str_repeat('=', 80));
            $course_info = str_replace(' ', "\t", html_to_text($course[1]));
            output($course_info);
            if ($course[3] !== '评教') {
                continue;
            }
            $html = curl('http://ssfw.xjtu.edu.cn/index.portal' . $course[2], null, '读取课程信息失败');
            $action = match('/post" action="(.*?)"/', $html)[1];
            $postfields = http_build_query(array(
                'wid_pgjxb' => match('/wid_pgjxb" value="(.*?)"/', $html, '解析课程信息失败')[1],
                'wid_pgyj' => match('/wid_pgyj" value="(.*?)"/', $html, '解析课程信息失败')[1],
                'type' => 2,
                'sfytj' => match('/sfytj" value="(.*?)"/', $html, '解析课程信息失败')[1],
                'pjType' => match('/pjType" value="(.*?)"/', $html, '解析课程信息失败')[1],
                'wid_pjzts' => match('/wid_pjzts" value="(.*?)"/', $html, '解析课程信息失败')[1],
                'status' => match('/status" value="(.*?)"/', $html, '解析课程信息失败')[1],
                'ztpj' => '很好',
                'sfmxpj' => match('/sfmxpj" value="(.*?)"/', $html, '解析课程信息失败')[1],
            ));
            $trs = match_all('/教师评价(.*?)<\\/tr>/su', $html, '解析课程信息失败');
            foreach ($trs as $tr) {
                $tmp = array(
                    'zbbm' => match('/zbbm" type="hidden" value="(.*?)"/', $tr[1], '解析课程信息失败')[1],
                );
                $matches = match('/(wid_.*?)" type="hidden" value="(.*?)"/', $tr[1], '解析课程信息失败');
                $tmp[$matches[1]] = $matches[2];
                $matches = match('/(qz_.*?)" type="hidden" value="(.*?)"/', $tr[1], '解析课程信息失败');
                $tmp[$matches[1]] = $matches[2];
                $matches = match_all('/(pfdj_.*?)"  value="(.*?)"/', $tr[1], '解析课程信息失败');
                $i = mt_rand(0, 1);
                $tmp[$matches[$i][1]] = $matches[$i][2];
                $postfields .= '&' . http_build_query($tmp);
            }
            $postfields .= '&' . http_build_query(array(
                'pgyj' => '很好',
                'actionType' => 2,
            ));
            output('读取课程信息成功');
            $html = curl('http://ssfw.xjtu.edu.cn/index.portal' . $action, $postfields, '评教失败');
            output('评教成功');
        } catch (Exception $e) {
            output($e->getMessage());
        }
        break;
    }
} catch (Exception $e) {
    output($e->getMessage());
}
