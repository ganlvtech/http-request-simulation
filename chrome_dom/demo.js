if (document.querySelector('div.title span').textContent == '学生评教') {
    var courses = document.querySelectorAll('#queryGridf1161 tbody tr');
    var evaluated = false;
    for (var i = 0; i < courses.length; ++i) {
        var tds = courses[i].children;
        var a = tds[tds.length - 1].children[0];
        if (a.textContent == '评教') {
            setInterval(function () { a.click(); }, 1000);
            evaluated = true;
            break;
        }
    }
    var submited = false;
    if (!evaluated) {
        var ztpj = document.getElementById('ztpj');
        var pgyj = document.getElementById('pgyj');
        if (ztpj.value == '' && pgyj.value == '') {
            ztpj.value = '很好';
            pgyj.value = '很好';
            var items = document.querySelectorAll('#zbdfTable tr');
            for (i = 0; i < items.length; ++i) {
                if (items[i].children[0].textContent.trim() == '教师评价') {
                    j = 3 + Math.round(Math.random());
                    items[i].children[j].children[0].children[0].click();
                }
            }
            var submit_button = document.querySelector('input[value="提交"]');
            if (submit_button) {
                submit_button.click();
                submited = true;
            }
        }
        if (!submited) {
            location.href = 'http://ssfw.xjtu.edu.cn/index.portal?.p=Znxjb20ud2lzY29tLnBvcnRhbC5zaXRlLmltcGwuRnJhZ21lbnRXaW5kb3d8ZjExNjF8dmlld3xub3JtYWx8YWN0aW9uPXF1ZXJ5';
        }
    }
}
