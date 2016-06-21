<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>


<div id="page-wrapper">
    <div class="row">
        <form action="deal/import" method="post">
            <div class="col-lg-2">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">请选择Excel文件</h4>
            </div>
        </form>

    </div>

    <div class="row">
        <div class="col-lg-12">
            <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                <thead>
                <tr>
                    <td>创建时间</td>
                    <td>Deal来源网站</td>
                    <td>Deal图片</td>
                    <td>是否在banner展示</td>
                    <td>Deal标题</td>
                    <td>生效时间</td>
                    <td>失效时间</td>
                    <td colspan="2">操作</td>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${datas}" var="data">
                    <tr>
                        <td>${data.createTime}</td>
                        <td>${data.website}</td>
                        <td>${data.imageUrl}</td>
                        <td>否</td>
                        <td>${data.title}</td>
                        <td>${device.createTime}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <jsp:include page="../include/page.jsp"/>
</div>


<jsp:include page="../include/footer.jsp"/>
