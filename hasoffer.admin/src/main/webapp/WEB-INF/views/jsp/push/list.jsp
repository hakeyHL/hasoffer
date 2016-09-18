<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">appPush 列表</h1>
        </div>

        <div class="row" style="margin-bottom: 10px">

            <form action="/topselling/list" method="get">

                <div class="col-lg-2">
                    <div class="input-group">
                        <span class="input-group-addon">push类型</span>
                        <select id="pushSourceTypeString" class="form-control" name="pushSourceTypeString">
                            <option value="DEAL">DEAL</option>
                        </select>
                    </div>
                </div>

                <div class="col-lg-2">
                    <div class="input-group">
                        <span class="input-group-addon">创建日期</span>
                        <input size="16" type="text" class="form-control form_datetime"
                               id="createTime" name="createTime" value="${createTime}">
                    </div>
                    <script>
                        $("#startTime").datepicker();
                    </script>
                </div>

                <div class="col-lg-2">
                    <button type="submit" class="btn btn-primary">查询</button>
                </div>
            </form>
        </div>

        <div class="row">
            <div class="col-lg-12">
                <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                    <thead>
                    <tr>
                        <td>ID</td>
                        <td>创建时间</td>
                        <td>类型</td>
                        <td>跳转参数</td>
                        <td>文案标题</td>
                        <td>文案详情</td>
                        <td>推送人群</td>
                        <td>预计推送设备数</td>
                        <td>实际推送成功数</td>
                        <td>点击推送设备数</td>
                        <td colspan="3">操作</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${appPushLists}" var="appPush">
                        <tr>
                            <td>${appPush.id}</td>
                            <td>${appPush.createTime}</td>
                            <td>${appPush.pushSourceType}</td>
                            <td>${appPush.sourceId}</td>
                            <td>${appPush.title}</td>
                            <td>
                                <textarea>${appPush.content}</textarea>
                            <td>${appPush.pushExpectDeviceNumber}</td>
                            <td>${appPush.receiveSuccessNumber}</td>
                            <td>${appPush.clickNumber}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>
        <jsp:include page="../include/page.jsp"/>
    </div>
</div>
<jsp:include page="../include/footer.jsp"/>
<script>

    //状态切换
    function change(topSellingId) {

        url = "/topselling/changeStatus/" + topSellingId;
        http.doGet(url);

        window.location.href = "/topselling/list?topSellingStatusString=${selectstatus}&tmp=" + Math.random() * 10000000000000000;
    }

</script>
