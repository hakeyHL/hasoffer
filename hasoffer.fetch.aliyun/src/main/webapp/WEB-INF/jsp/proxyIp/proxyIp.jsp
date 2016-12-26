<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<%
    String contextPath = request.getContextPath();
%>
<html>
<head>
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
<div class="col-infos" id="info-div">
    <h2 style="display: inline;"><i class="fa fa-bar-chart-o"></i>IP代理</h2>
</div>
<div class="table-responsive" id="toolbar-table">
    <%--<div>--%>

    <%--<!-- 在此填写窗口内容，自定义HTML -->--%>
    <%--<table class="table table-hover table-bordered" style="background-color: #F5F5F5;">--%>
    <%--<tr class="odd">--%>
    <%--<td style="vertical-align: middle; text-align: center;">--%>
    <%--<label>查询日期</label>--%>
    <%--</td>--%>
    <%--<td style="vertical-align: middle; text-align: center;">--%>
    <%--<input id="queryDate" type="date" class="form-control "--%>
    <%--style="background:#fff url(<%=contextPath%>/js/skin/datePicker.gif) no-repeat right;height:34px"--%>
    <%--onClick="WdatePicker()" data-errormessage-value-missing="* 请输入日期" value="${queryDate}"/>--%>
    <%--</td>--%>
    <%--<td style="vertical-align: middle; text-align: center;">--%>
    <%--<a href="#" onclick="queryUpdateState()" class="btn btn-sm btn-info">查找</a>--%>
    <%--</td>--%>
    <%--</table>--%>
    <%--</div>--%>
    <div>

        <!-- 在此填写窗口内容，自定义HTML -->
        <table class="table table-bordered" style="background-color: #F5F5F5;">
            <tr class="odd">
                <td colspan="5" style="vertical-align: middle; text-align: left;">新增IP</td>
            </tr>
            <tr class="odd">
                <td style="vertical-align: middle; text-align: center;">
                    <label>IP</label>
                </td>
                <td style="vertical-align: middle; text-align: center;">
                    <input id="ip" type="text" class="form-control"/>
                </td>
                <td style="vertical-align: middle; text-align: center;">
                    <label>Port</label>
                </td>
                <td style="vertical-align: middle; text-align: center;">
                    <input id="port" type="number" class="form-control"/>
                </td>
                <td style="vertical-align: middle; text-align: center;">
                    <a href="#" onclick="insertProxyIP()" class="btn btn-sm btn-info">新增</a>
                </td>
            </tr>
        </table>
    </div>
    <table id="table"
           data-show-refresh="true"
           data-show-columns="true"
           data-show-export="true"
    >
    </table>
</div>

<script>
    var $table = $('#table');

    function initTable() {
        $table.bootstrapTable({
            url: "${ctx}/proxyIP/selectList",
            method: 'post',
            height: getHeight(),
            contentType: 'application/json',
            dataType: 'json',
            sortable: false,
            idField: 'id',
            columns: [
                [{
                    field: 'ip',
                    title: 'IP',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'port',
                    title: 'PORT',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'reqNum',
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
                    field: 'status',
                    title: '状态',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'startDate',
                    title: '启用时间',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'stopDate',
                    title: '关闭时间',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'id',
                    title: '操作',
                    align: 'center',
                    halign: 'center',
                    events: operateEvents,
                    formatter: operateFormatter
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

    function operateFormatter(value, row, index) {
        return [
            '<a class="remove" href="javascript:void(0)" title="Remove">',
            '<i class="glyphicon glyphicon-remove"></i>',
            '</a>'
        ].join('');
    }

    window.operateEvents = {
        'click .remove': function (e, value, row, index) {
            $table.bootstrapTable('remove', {
                field: 'id',
                values: [row.id]
            });
            stopProxyIP(row.id);
        }
    };

    function getHeight() {
        return $(window).height() - $('#info-div').outerHeight(true) - $('#toolbar-table').outerHeight(true);
    }

    function stopProxyIP(proxyIpId) {
        var url = "${ctx}/proxyIP/stopProxyIP";
        var args = {
            id: proxyIpId,
        };
        $.ajax({
            cache: true,
            type: 'POST',
            url: url,
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(args),
            async: false,
            success: function (data) {
                $table.bootstrapTable('refresh')
            }
        });
    }

    function insertProxyIP() {

        var $ip = $("#ip").val();
        var $port = $("#port").val();


        var url = "${ctx}/proxyIP/insertProxyIP";
        var args = {
            ip: $ip,
            port: $port
        };
        $.ajax({
            cache: true,
            type: 'POST',
            url: url,
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(args),
            async: false,
            success: function (data) {
                $table.bootstrapTable('refresh');
                alert(data.msg);
                $("#ip").val("");
                $("#port").val("");
            }
        });
    }

    function queryUpdateState() {

        var $dateStr = $('#queryDate').val();

        var url = "${ctx}/updateState/selectUpdateByDay/" + $dateStr;

        $.ajax({
            cache: true,
            type: 'POST',
            url: url,
            contentType: 'application/json',
            dataType: 'json',
            async: false,
            success: function (data) {
                $table.bootstrapTable('load', data);
            }
        });
    }

    $(function () {
        initTable();
    });


</script>
</body>
</html>
