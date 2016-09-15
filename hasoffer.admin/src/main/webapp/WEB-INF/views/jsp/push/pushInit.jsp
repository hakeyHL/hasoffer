<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<style>
    .p_list_image {
        width: 40px;
        max-height: 60px;
    }
</style>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">push创建</h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>

    <div class="row" style="margin-bottom: 10px">
    </div>

    <form class="form-horizontal" role="form">
        <div class="form-group">
            <label for="pushType" class="col-sm-2 control-label">推送类型</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="pushType" name="pushType" placeholder="Deal"
                       value="${pushType}" disabled>
            </div>
        </div>
        <div class="form-group">
            <label for="crowd" class="col-sm-2 control-label">推送人群</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="crowd" name="crowd" placeholder="所有设备" value="${crowd}"
                       disabled>
            </div>
        </div>
        <div class="form-group">
            <label for="pushSourceId" class="col-sm-2 control-label">配置跳转参数</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="pushSourceId" value="${pushSourceId}">
            </div>
        </div>
        <div class="form-group">
            <label for="pushTitle" class="col-sm-2 control-label">推送文案标题</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="pushTitle" value="${pushTitle}">
            </div>
        </div>
        <div class="form-group">
            <label for="pushContent" class="col-sm-2 control-label">推送文案详情</label>

            <div class="col-sm-10">
                <textarea class="form-control" id="pushContent" name="pushContent" rows="13">${pushContent}</textarea>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-default">确认无误,推送</button>
            </div>
        </div>
    </form>

    <jsp:include page="../include/page.jsp"/>
</div>

<jsp:include page="../include/footer.jsp"/>
