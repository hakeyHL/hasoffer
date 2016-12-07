<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<%
    String contextPath1 = request.getContextPath();
%>
<html>
<head>
    <title>Order Status</title>
    <link rel="stylesheet" href="<%=contextPath1%>/extensions/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="<%=contextPath1%>/extensions/bootstrap-table/bootstrap-table.css">
    <link rel="stylesheet" href="<%=contextPath1%>/extensions/bootstrap-table/extensions/edittable/css/edittable.css">

    <script src="<%=contextPath1%>/extensions/jquery.min.js"></script>
    <script src="<%=contextPath1%>/extensions/bootstrap/js/bootstrap.js"></script>
    <script src="<%=contextPath1%>/extensions/bootstrap-table/bootstrap-table.js"></script>
    <script src="<%=contextPath1%>/extensions/bootstrap-table/extensions/exporttable/js/bootstrap-table-export.js"></script>
    <script src="<%=contextPath1%>/extensions/bootstrap-table/extensions/exporttable/js/exporttable.js"></script>
</head>
<body>
<div class="container" style="width: 100%">
    <h4>订单状态</h4>
    <table id="table"
           data-show-refresh="true"
    <%--data-show-columns="true"--%>
           data-show-export="true"
    <%--data-minimum-count-columns="2"--%>
    <%--data-show-pagination-switch="true"--%>
    <%--data-show-footer="false"--%>
           data-url="${ctx}/orderCtl/queryOrderStatus"
    >
    </table>
</div>

<script>
    var $table = $('#table');

    function initTable() {
        $table.bootstrapTable({
            height: getHeight(),
            columns: [
                [{
                    field: 'dateTime',
                    rowspan: 2,
                    title: '订单时间',
                    align: 'center',
                    valign: 'middle',
                    halign: 'center'
                }, {
                    title: '订单统计',
                    colspan: 5,
                    align: 'center',
                    halign: 'center'
                }, {
                    title: '货运状态',
                    colspan: 4,
                    align: 'center',
                    halign: 'center'
                }
                ],
                [{
                    field: 'orderCount',
                    title: '总数',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'stockN',
                    title: '未采购',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'stockY',
                    title: '已采购',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'shipN',
                    title: '未发货',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'shipY',
                    title: '已发货',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'shipIng',
                    title: '运送中',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'signY',
                    title: '已签收',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'signN',
                    title: '拒收',
                    align: 'right',
                    halign: 'center'
                }, {
                    field: 'signE',
                    title: '运单号异常',
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
