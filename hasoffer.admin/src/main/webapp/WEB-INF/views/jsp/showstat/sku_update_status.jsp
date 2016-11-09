<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">SKU 更新状态</h1>
        </div>

        <div class="col-lg-12" style="margin: 5px">
            <form action="/stat/sku_update_result_hour" method="GET">
                <div class="col-md-1">
                    <div class="checkbox">
                        <input type="text" id="ymd_hh" name="ymd_hh" value="">
                        <button type="submit">更新状态</button>
                    </div>
                </div>
            </form>
        </div>

        <div class="row">
            <div class="col-lg-12">
                <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                    <thead>
                    <tr>
                        <td>日期</td>
                        <td>总计 | 已更新</td>
                        <td>Flipkart</td>
                        <td>Amazon</td>
                        <td>Snapdeal</td>
                        <td>Ebay</td>
                        <td>shopclues</td>
                        <td>paytm</td>
                        <td>myntra</td>
                        <td>infibeam</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${datas}" var="data">
                        <tr>
                            <td>${data.ymd}</td>
                            <td>${data.allTotal} | ${data.allSuccess}</td>
                            <td>${data.flipkartTotal} | ${data.flipkartSuccess}</td>
                            <td>${data.amazonTotal} | ${data.amazonSuccess}</td>
                            <td>${data.snapdealTotal} | ${data.snapdealSuccess}</td>
                            <td>${data.ebayTotal} | ${data.ebaySuccess}</td>
                            <td>${data.shopcluesTotal} | ${data.shopcluesSuccess}</td>
                            <td>${data.paytmTotal} | ${data.paytmSuccess}</td>
                            <td>${data.myntraTotal} | ${data.myntraSuccess}</td>
                            <td>${data.infibeamTotal} | ${data.infibeamSuccess}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>

        <%--<jsp:include page="../include/page.jsp"/>--%>
    </div>

<jsp:include page="../include/footer.jsp"/>