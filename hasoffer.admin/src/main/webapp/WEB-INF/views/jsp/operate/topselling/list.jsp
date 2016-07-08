<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../../include/header.jsp"/>
<jsp:include page="../../include/left.jsp"/>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">top selling 列表</h1>
        </div>

        <div class="row">
            <div class="col-lg-12">
                <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                    <thead>
                    <tr>
                        <td>日期</td>
                        <td>图片</td>
                        <td>标题</td>
                        <td colspan="2">操作</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${topSellingVoList}" var="topSellingVo">
                        <tr>
                            <td>${topSellingVo.ymd}</td>
                            <td><img src="${topSellingVo.imageurl}" height="130" width="100"/></td>
                            <td>
                                <a href="/p/cmp/${topSellingVo.productId}">${topSellingVo.name} ${topSellingVo.skuNumber}个比价</a>
                            </td>
                            <td><a href="detail/${topSellingVo.productId}">修改图片</a></td>
                                <%--todo--%>
                            <td><a href="/s/reSearchByLogKeyword/${topSellingVo.logid}">手工关联</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>
        <%--<jsp:include page="../include/page.jsp"/>--%>
        <jsp:include page="../../include/page.jsp"/>
    </div>

<jsp:include page="../../include/footer.jsp"/>