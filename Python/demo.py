#!/usr/bin/python3
# -*- coding: utf-8 -*-
import getpass
import html
import http.cookiejar
import random
import re
import urllib.request
import urllib.parse

cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))

def output(content):
    print(str(content))

def request(url, data=None, err_msg='发送请求失败'):
    try:
        if data is None:
            r = opener.open(url)
        else:
            r = opener.open(url, data.encode('utf-8'))
        return r.read().decode('utf-8')
    except:
        output(err_msg)
        raise

def match(pattern, string, flags=0, err_msg='正则匹配错误'):
    m = re.search(pattern, string, flags)
    if m is None:
        raise Exception(err_msg)
    return m

def match_all(pattern, string, flags=0, err_msg='正则匹配错误'):
    m = re.finditer(pattern, string, flags)
    if m is None:
        raise Exception(err_msg)
    return m

def html_to_text(str):
    return re.sub(r'[\s\x0B\xC2\xA0]+', ' ', html.unescape(re.sub('<.*?>', ' ', str)), re.S).strip()

username = input('请输入NetID：')
password = getpass.getpass('请输入密码：')
output('=' * 80)
try:
    f = request('https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal',
                err_msg='读取CAS登录页面失败')
    m = match('name="lt" value="(.*?)".*?name="execution" value="(.*?)".*?name="_eventId" value="(.*?)"', f, re.S,
              err_msg='获取登录凭证失败')
    f = request('https://cas.xjtu.edu.cn/login?service=http%3A%2F%2Fssfw.xjtu.edu.cn%2Findex.portal',
                urllib.parse.urlencode({
                    'username': username,
                    'password': password,
                    'code': '',
                    'lt': m.group(1),
                    'execution': m.group(2),
                    '_eventId': m.group(3),
                    'submit': '登录'
                }), err_msg='登录CAS失败')
    login_url = match('url=(.*?)"', f, err_msg='用户名或密码错误').group(1)
    output('登录CAS成功')
    f = request(login_url, err_msg='登录师生服务系统失败')
    hello = match('<li style="color:#fff;font-weight:bold;">(.*?)</li>', f, err_msg='获取师生服务首页失败').group(1)
    output('登录师生服务系统成功')
    output(hello)
    f = request(
        'http://ssfw.xjtu.edu.cn/index.portal?.p=Znxjb20ud2lzY29tLnBvcnRhbC5zaXRlLmltcGwuRnJhZ21lbnRXaW5kb3d8ZjExNjF8dmlld3xub3JtYWx8YWN0aW9uPXF1ZXJ5',
        err_msg='读取课程列表失败')
    table = match('<tbody>(.*?)</tbody>', f, re.S, err_msg='解析课程列表失败').group(1)
    courses = match_all('<tr.*?>(.*?)<a href="(.*?)">(.*?)</a>', table, re.S, err_msg='解析课程列表失败')
    output('读取课程列表成功')
    for course in courses:
        try:
            output('=' * 80)
            course_info = html_to_text(course.group(1)).replace(' ', '\t')
            output(course_info)
            if course.group(3) != '评教':
                continue
            f = request('http://ssfw.xjtu.edu.cn/index.portal' + course.group(2), err_msg='读取课程信息失败')
            action = match('post" action="(.*?)"', f).group(1)
            post_fields = urllib.parse.urlencode({
                'wid_pgjxb': match('wid_pgjxb" value="(.*?)"', f, err_msg='解析课程失败').group(1),
                'wid_pgyj': match('wid_pgyj" value="(.*?)"', f, err_msg='解析课程失败').group(1),
                'type': 2,
                'sfytj': match('sfytj" value="(.*?)"', f, err_msg='解析课程失败').group(1),
                'pjType': match('pjType" value="(.*?)"', f, err_msg='解析课程失败').group(1),
                'wid_pjzts': match('wid_pjzts" value="(.*?)"', f, err_msg='解析课程失败').group(1),
                'status': match('status" value="(.*?)"', f, err_msg='解析课程失败').group(1),
                'ztpj': '很好',
                'sfmxpj': match('sfmxpj" value="(.*?)"', f, err_msg='解析课程失败').group(1)
            })
            trs = match_all('教师评价(.*?)</tr>', f, re.S, err_msg='解析课程信息失败')
            for tr in trs:
                tmp = {
                    'zbbm': match('zbbm" type="hidden" value="(.*?)"', tr.group(1), err_msg='解析课程信息失败').group(1)
                }
                m = match('(wid_.*?)" type="hidden" value="(.*?)"', tr.group(1), err_msg='解析课程信息失败')
                tmp[m.group(1)] = m.group(2)
                m = match('(qz_.*?)" type="hidden" value="(.*?)"', tr.group(1), err_msg='解析课程信息失败')
                tmp[m.group(1)] = m.group(2)
                m = match_all('(pfdj_.*?)"  value="(.*?)"', tr.group(1), err_msg='解析课程信息失败')
                m = list(m)
                i = random.randint(0, 1)
                tmp[m[i].group(1)] = m[i].group(2)
                post_fields += '&' + urllib.parse.urlencode(tmp)
            post_fields += '&' + urllib.parse.urlencode({
                'pgyj': '很好',
                'actionType': 2
            })
            output('读取课程信息成功')
            f = request('http://ssfw.xjtu.edu.cn/index.portal' + action, post_fields, err_msg='评教失败')
            output('评教成功')
        except Exception as e:
            output(e)
except Exception as e:
    output(e)
