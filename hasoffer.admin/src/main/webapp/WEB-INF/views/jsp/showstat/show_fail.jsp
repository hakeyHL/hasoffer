<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">更新失败的SKU</h1>
        </div>

        <div class="col-lg-12" style="margin: 5px"></div>

        <div class="row">
            <div class="col-lg-12">
                <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                    <thead>
                    <tr>
                        <td rowspan="2">id</td>
                        <td rowspan="2">pro-id</td>
                        <td colspan="7">title</td>
                        <td rowspan="2">update-time</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${skus}" var="sku">
                        <tr>
                            <td>${sku.id}</td>
                            <td><a href="/p/cmp/${sku.productId}" target="_blank">${sku.productId}</a></td>
                            <td>
                                <a href="${sku.url}" target="_blank">${sku.title}</a></td>
                            <td>${sku.updateTime}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>

        <%--<jsp:include page="../include/page.jsp"/>--%>
    </div>

<jsp:include page="../include/footer.jsp"/>