function output(str) {
    var lines = str.split('\n');
    for (var i = 0; i < lines.length; ++i) {
        var li = document.createElement('li');
        li.textContent = lines[i];
        document.getElementById('output').appendChild(li);
    }
}
var Request = {
    newXHR: function(callback, errMsg) {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4) {
                callback(xhr.responseText);
            }
        };
        xhr.addEventListener("error", function () {
            output(errMsg);
        }, false);
        return xhr;
    },
    get: function(url, callback, errMsg) {
        var xhr = Request.newXHR(callback, errMsg);
        xhr.open("GET", url, true);
        xhr.send();
    },
    post: function(url, form, callback, errMsg) {
        var params = [];
        for (var i in form) {
            params.push({
                name: i,
                value: form[i]
            });
        }
        Request.postParams(url, params, callback, errMsg);
    },
    postParams: function(url, params, callback, errMsg) {
        var body = [];
        for (var i = 0; i < params.length; ++i) {
            body.push(encodeURIComponent(params[i].name) + '=' + encodeURIComponent(params[i].value));
        }
        body = body.join('&');
        var xhr = Request.newXHR(callback, errMsg);
        xhr.open('POST', url, true);
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhr.send(body);
    }
};
var Regex = {
    match: function(regexp, str, errMsg) {
        var matches;
        if ((matches = regexp.exec(str)) === null) {
            output(errMsg);
        }
        return matches;
    },
    matchAll: function(regexp, str) {
        var result = [];
        var matches;
        while ((matches = regexp.exec(str)) != null) {
            result.push(matches);
        }
        return result;
    }
};
var Demo = {
    htmlToText: function(html) {
        return html.replace(/<[\s\S]*?>/g, ' ').replace(/&nbsp;/g, ' ').replace(/[\s\x0B\xC2\xA0]+/g, ' ').trim();
    },
    username: '',
    password: '',
    main: function(username, password) {
        Demo.username = username;
        Demo.password = password;
        Request.get('http://ssfw.xjtu.edu.cn/logout.portal', function(body) {
            Request.get('https://cas.xjtu.edu.cn/logout', function(body) {
                var matches
                if (matches = Regex.match(/注销成功/g, body, '初始化失败')) {
                    output('初始化成功');
                    Demo.loadCas();
                }
            }, '初始化失败');
        }, '初始化失败');
    },
    loadCas: function() {
        Request.get('https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal', function(body) {
            var matches
            if (matches = Regex.match(/name="lt" value="(.*?)"[\s\S]*?name="execution" value="(.*?)"[\s\S]*?name="_eventId" value="(.*?)"/g, body, '获取登录凭证失败')) {
                var lt = matches[1];
                var execution = matches[2];
                var _eventId = matches[3];
                output('获取登录凭证成功');
                Demo.loginCas(lt, execution, _eventId);
            }
        }, '读取CAS登录页面失败');
    },
    loginCas: function(lt, execution, _eventId) {
        Request.post('https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal', {
            username: Demo.username,
            password: Demo.password,
            code: '',
            lt: lt,
            execution: execution,
            _eventId: _eventId
        }, function(body) {
            var matches
            if (matches = Regex.match(/url=(.*?)"/g, body, '用户名或密码错误')) {
                var ssfwUrl = matches[1];
                output('登录CAS成功');
                Demo.loginSsfw(ssfwUrl);
            }
        }, '登录CAS失败');
    },
    loginSsfw: function(ssfwUrl) {
        Request.get(ssfwUrl, function(body) {
            var matches
            if (matches = Regex.match(/<li style="color:#fff;font-weight:bold;">(.*?)<\/li>/g, body, '获取师生服务首页失败')) {
                var hello = matches[1];
                output('登录师生服务系统成功');
                output(hello);
                Demo.listCourse();
            }
        }, '登录师生服务系统失败');
    },
    listCourse: function() {
        Request.get('http://ssfw.xjtu.edu.cn/index.portal?.p=Znxjb20ud2lzY29tLnBvcnRhbC5zaXRlLmltcGwuRnJhZ21lbnRXaW5kb3d8ZjExNjF8dmlld3xub3JtYWx8YWN0aW9uPXF1ZXJ5', function(body) {
            var matches
            if (matches = Regex.match(/<tbody>([\s\S]*?)<\/tbody>/g, body, '解析课程列表失败')) {
                var table = matches[1];
                output('读取课程列表成功');
                var courses = Regex.matchAll(/<tr[\s\S]*?>([\s\S]*?)<a href="(.*?)">(.*?)<\/a>/g, table);
                for (var i = 0; i < courses.length; ++i) {
                    var course = courses[i];
                    var desc = Demo.htmlToText(course[1]).replace(/ /, '\t');
                    var action = course[3];
                    if (action !== '评教') {
                        output('================================================================================');
                        output(desc);
                    } else {
                        var url = 'http://ssfw.xjtu.edu.cn/index.portal' + course[2];
                        try {
                            Demo.loadCourse(url, desc);
                        } catch (e) {
                            output(e.message);
                        }
                    }
                }
            }
        }, '读取课程列表失败');
    },
    loadCourse: function(url, desc) {
        var msgPrefix = '================================================================================\n' + desc + '\n';
        Request.get(url, function(body) {
            var matches;
            if (matches = Regex.match(/post" action="(.*?)"/, body, msgPrefix + '获取提交网址失败')) {
                var url = 'http://ssfw.xjtu.edu.cn/index.portal' + matches[1];
                var params = [];
                var errMsg = msgPrefix + '解析课程失败';
                params.push({ name: 'wid_pgjxb', value: Regex.match(/wid_pgjxb" value="(.*?)"/g, body, errMsg)[1] });
                params.push({ name: 'wid_pgyj', value: Regex.match(/wid_pgyj" value="(.*?)"/g, body, errMsg)[1] });
                params.push({ name: 'type', value: '2' });
                params.push({ name: 'sfytj', value: Regex.match(/sfytj" value="(.*?)"/g, body, errMsg)[1] });
                params.push({ name: 'pjType', value: Regex.match(/pjType" value="(.*?)"/g, body, errMsg)[1] });
                params.push({ name: 'wid_pjzts', value: Regex.match(/wid_pjzts" value="(.*?)"/g, body, errMsg)[1] });
                params.push({ name: 'status', value: Regex.match(/status" value="(.*?)"/g, body, errMsg)[1] });
                params.push({ name: 'ztpj', value: '很好' });
                params.push({ name: 'sfmxpj', value: Regex.match(/sfmxpj" value="(.*?)"/g, body, errMsg)[1] });
                var trs = Regex.matchAll(/教师评价([\s\S]*?)<\/tr>/g, body);
                for (var i = 0; i < trs.length; ++i) {
                    var tr = trs[i][1];
                    params.push({ name: 'zbbm', value: Regex.match(/zbbm" type="hidden" value="(.*?)"/g, tr)[1] }, errMsg);
                    var matches = Regex.match(/(wid_.*?)" type="hidden" value="(.*?)"/g, tr, errMsg);
                    params.push({ name: matches[1], value: matches[2] });
                    matches = Regex.match(/(qz_.*?)" type="hidden" value="(.*?)"/g, tr, errMsg);
                    params.push({ name: matches[1], value: matches[2] });
                    var scoreMatches = Regex.matchAll(/(pfdj_.*?)"  value="(.*?)"/g, tr);
                    var j = parseInt(Math.random() + 0.5);
                    params.push({ name: scoreMatches[j][1], value: scoreMatches[j][2] });
                }
                params.push({ name: 'pgyj', value: '很好' });
                params.push({ name: 'actionType', value: '2' });
                Demo.evalCourse(url, desc, params);
            }
        }, '读取课程信息失败');
    },
    evalCourse: function(url, desc, params) {
        var msgPrefix = '================================================================================\n' + desc + '\n';
        Request.postParams(url, params, function(body) {
            output(msgPrefix + '评教成功');
        }, msgPrefix + '评教失败');
    }
};
document.getElementById('submit').onclick = function() {
    try {
        Demo.main(document.getElementById('username').value, document.getElementById('password').value);
    } catch (e) {
        output(e.message);
    }
};
