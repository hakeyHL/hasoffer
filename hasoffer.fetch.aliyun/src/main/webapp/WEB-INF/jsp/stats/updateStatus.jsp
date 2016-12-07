<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<%
    String contextPath = request.getContextPath();
%>
<html>
<head>
    <title>Order Status</title>
    <link rel="stylesheet" href="<%=contextPath%>/extensions/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="<%=contextPath%>/extensions/bootstrap-table/bootstrap-table.css">
    <link rel="stylesheet" href="<%=contextPath%>/extensions/bootstrap-table/extensions/edittable/css/edittable.css">
    <link href="<%=contextPath%>/bootstrap/css/bootstrap-datetimepicker.min.css" rel="stylesheet" media="screen">
    <%@ include file="/common/commonCss.jsp" %>
    <link rel="stylesheet" href="<%=contextPath%>/css/page.css">
    <link rel="stylesheet" href="<%=contextPath%>/css/icomoon.min.css">

    <script src="<%=contextPath%>/extensions/jquery.min.js"></script>
    <script src="<%=contextPath%>/js/WdatePicker.js"></script>
    <script src="<%=contextPath%>/extensions/bootstrap/js/bootstrap.js"></script>
    <script src="<%=contextPath%>/extensions/bootstrap-table/bootstrap-table.js"></script>
    <script src="<%=contextPath%>/extensions/bootstrap-table/extensions/exporttable/js/bootstrap-table-export.js"></script>
    <script src="<%=contextPath%>/extensions/bootstrap-table/extensions/exporttable/js/exporttable.js"></script>
</head>
<body class="scrollY frameContent" style="OVERFLOW-X: scroll">
<div class="col-infos">
    <h2 style="display: inline;"><i class="fa fa-bar-chart-o"></i>更新统计</h2>
</div>
<div class="table-responsive">
    <div>

        <!-- 在此填写窗口内容，自定义HTML -->
        <table class="table table-hover table-bordered" style="background-color: #F5F5F5;">
            <tr class="odd">
                <td style="vertical-align: middle; text-align: center;">
                    <label>查询日期</label>
                </td>
                <td style="vertical-align: middle; text-align: center;">
                    <input id="queryDate" type="date" class="form-control "
                           style="background:#fff url(<%=contextPath%>/js/skin/datePicker.gif) no-repeat right;height:34px"
                           onClick="WdatePicker()" data-errormessage-value-missing="* 请输入日期" value="${queryDate}"/>
                </td>
                <td style="vertical-align: middle; text-align: center;">
                    <a href="#" onclick="queryUpdateState()" class="btn btn-sm btn-info">查找</a>
                </td>
        </table>
    </div>
    <table id="table"
           data-show-refresh="true"
           data-show-columns="true"
           data-show-export="true"
    <%--data-show-footer="false"--%>
    >
    </table>
</div>

<script>
    var $table = $('#table');
    var $dateStr = $('#queryDate').val();
    function initTable() {
        $table.bootstrapTable({
            url: "${ctx}/updateState/selectUpdateByDay/" + $dateStr,
            height: getHeight(),
            columns: [
                [{
                    field: 'updateDate',
                    title: '时间',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'taskTarget',
                    title: '任务类型',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'pushNum',
                    title: '请求数',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'finishNum',
                    title: '成功',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'exceptionNum',
                    title: '异常',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'stopNum',
                    title: '停止',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'logTime',
                    title: '已签收',
                    align: 'right',
                    halign: 'center'
                }
                ]
            ]
        });
        $(window).resize(function () {
            $table.bootstrapTable('resetView', {
                height: getHeight()
            });
        });
    }

    function getHeight() {
        return $(window).height() - $('h4').outerHeight(true);
    }

    $(function () {
        initTable();
    });


</script>
</body>
</html>
