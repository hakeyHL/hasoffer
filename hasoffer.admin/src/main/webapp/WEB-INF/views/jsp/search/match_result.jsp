<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<style>
    table {
        font-size: 12px;
    }
</style>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">匹配结果 - <a href="/cmp/${result.relatedProId}"
                                              target="_blank">${result.relatedProId}</a></h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <div class="row">
        <div class="panel panel-default">
            <!-- Default panel contents -->
            <div class="panel-heading">基本信息</div>
            <!-- Table -->
            <table class="table">
                <tr>
                    <td>标题</td>
                    <td>价格</td>
                    <td>来源</td>
                </tr>
                <tr>
                    <%--<td><fmt:formatDate value="${result.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"></fmt:formatDate></td>--%>
                    <td>${result.title}</td>
                    <td>${result.price}</td>
                    <td>${result.fromWebsite}</td>
                </tr>
            </table>
        </div>
    </div>

    <div class="row">
        <div class="panel panel-default">

            <div class="panel-heading">搜索到的SKU</div>

            <div class="panel-body">
                <ul class="nav nav-pills">
                    <c:forEach items="${result.sitePros}" var="sitePro">
                        <li role="presentation">
                            <a href="#" <c:if test="${sitePro.value.productList.size() > 0}">style="color: red" </c:if>>
                                    ${sitePro.key}(${sitePro.value.productList.size()})</a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="panel panel-default">

            <div class="panel-heading">分析结果</div>

            <div class="panel-body">
                <ul class="nav nav-pills">
                    <c:forEach items="${result.finalSkus}" var="finalSku">
                        <li role="presentation"><a href="javascript:void(0);"
                                                   onclick="click_final_skus_btn('${finalSku.key}')">${finalSku.key}(${finalSku.value.size()})</a>
                        </li>
                    </c:forEach>
                </ul>

                <c:forEach items="${result.finalSkus}" var="finalSku">
                    <table id="table_${finalSku.key}" name="finalskutable" class="table table-bordered table-condensed"
                           style="display: none">
                        <tr>
                            <td>SourceID</td>
                            <td>Title</td>
                            <td>Title-Score</td>
                            <td>Price</td>
                            <td>Price-Score</td>
                        </tr>
                        <c:forEach items="${finalSku.value}" var="sku">
                            <tr>
                                <td><a href="${sku.url}">${sku.sourceId}</a></td>
                                <td>${sku.title}</td>
                                <td>${sku.titleScore}</td>
                                <td>${sku.price}</td>
                                <td>${sku.priceScore}</td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:forEach>
            </div>
        </div>
    </div>

    <script>
        function click_final_skus_btn(site) {
            $("table[name='finalskutable']").css("display", "none");
            var tb = "#table_" + site;
            $(tb).css("display", "block");
        }
    </script>

    <jsp:include page="../include/page.jsp"/>
</div>

<jsp:include page="../include/footer.jsp"/>