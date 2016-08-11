<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    String contextPath = request.getContextPath();
%>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>


<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">推送消息</h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <div class="row">
        <div class="panel panel-default">
            <div class="panel-heading">推送简介</div>
            <div class="panel-body">
                <p>
                    推送类型声明:
                </p>

                <p>
                    推送用户数量声明:
                </p>

                <p>其他声明:</p>
            </div>
        </div>

    </div>
    <div class="col-lg-12" style="margin: 20px"></div>
    <form class="form-horizontal" action="<%=contextPath%>/push/pushMessage" enctype="application/x-www-form-urlencoded"
          id="form_edit"
          method="post" onsubmit="return dosubmit()">
        <div class="form-group">
            <label class="col-sm-3 control-label">outline</label>

            <div class="col-sm-7">
                <input type="text" name="outline" class="form-control" value="" placeholder="outline">
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">title</label>

            <div class="col-sm-7">
                <input type="text" name="title" class="form-control" value="" placeholder="title">
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">content</label>

            <div class="col-sm-7">
                <input type="text" name="content" class="form-control" value="" placeholder="content">
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">要push的app版本version</label>

            <div class="col-sm-7">
                <div class="col-lg-8">
                    <c:forEach items="${versions}" var="version">
                        <div class="active">
                            <label class="checkbox-inline">
                                <input type="checkbox" id="version" value="${version}">
                            </label>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">推送内容类型type</label>

            <div class="col-sm-7">
                <div class="col-lg-8">
                    <div class="active">
                        <label class="radio-inline">
                            <input type="radio" name="type" id="type1" value="MAIN">MAIN
                        </label>
                    </div>
                    <div class="active">
                        <label class="radio-inline">
                            <input type="radio" name="type" id="type2" value="DEEPLINK">DEEPLINK
                        </label>
                    </div>
                    <div class="active">
                        <label class="radio-inline">
                            <input type="radio" name="type" id="type3" value="WEBVIEW">WEBVIEW
                        </label>
                    </div>
                    <div class="active">
                        <label class="radio-inline">
                            <input type="radio" name="type" id="type4" value="GOOGLEPLAY">GOOGLEPLAY
                        </label>
                    </div>
                    <div class="active">
                        <label class="radio-inline">
                            <input type="radio" name="type" id="type5" value="PRODUCT">PRODUCT
                        </label>
                    </div>
                    <div class="active">
                        <label class="radio-inline">
                            <input type="radio" name="type" id="type6" value="DEAL">DEAL
                        </label>
                    </div>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">类型值</label>

            <div class="col-sm-7">
                <input type="text" name="id" class="form-control" value="" placeholder="请根据推送类型填写相应值">
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">推送安装了那些app的用户</label>

            <div class="col-sm-7">
                <div class="col-lg-8">
                    <c:forEach items="${websites}" var="website">
                        <div class="active checkbox-inline">
                            <label class="checkbox-inline">
                                <input type="checkbox" id="website" value="${website.name()}">${website.name()}
                            </label>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">渠道</label>

            <div class="col-sm-7">
                <div class="col-lg-8">
                    <select multiple class="form-control">
                        <c:forEach items="${channels}" var="channel">
                            <option id="channel" value="${channel.name()}">${channel.name()}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label class="col-sm-3 control-label">此次推送用户的数量number</label>

            <div class="col-sm-7">
                <input type="text" name="number" class="form-control" value="" placeholder="此处不填,推送给所有符合条件的用户">
            </div>
        </div>
    </form>


</div>
<div class="modal fade" id="myModal" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close"
                        data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="myModalLabel">
                    提示消息:
                </h4>
            </div>
            <div class="modal-body">
                当Deal设置为前端显示时价格描述不能为空 !
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary"
                        data-dismiss="modal">关闭
                </button>
            </div>
        </div>
    </div>
</div>
<script>

    $().ready(function () {

    });

    function dosubmit() {
        return true;
    }

</script>

<jsp:include page="../include/footer.jsp"/>
