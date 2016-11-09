<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">SKU <a href="/stat/sku_update_status_today">更新</a>状态</h1>
        </div>

        <div class="row">
            <div class="col-lg-12">
                <p>wait4Update : ${wait4Update} | updateProcessd : ${updateProcessd}</p>
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
                        <tr>
                            <td>${updateRst.ymd}</td>
                            <td>${updateRst.allTotal} | ${updateRst.allSuccess}</td>
                            <td>${updateRst.flipkartTotal} | ${updateRst.flipkartSuccess}</td>
                            <td>${updateRst.amazonTotal} | ${updateRst.amazonSuccess}</td>
                            <td>${updateRst.snapdealTotal} | ${updateRst.snapdealSuccess}</td>
                            <td>${updateRst.ebayTotal} | ${updateRst.ebaySuccess}</td>
                            <td>${updateRst.shopcluesTotal} | ${updateRst.shopcluesSuccess}</td>
                            <td>${updateRst.paytmTotal} | ${updateRst.paytmSuccess}</td>
                            <td>${updateRst.myntraTotal} | ${updateRst.myntraSuccess}</td>
                            <td>${updateRst.infibeamTotal} | ${updateRst.infibeamSuccess}</td>
                        </tr>
                    </tbody>
                </table>
            </div>

        </div>

        <%--<jsp:include page="../include/page.jsp"/>--%>
    </div>

<jsp:include page="../include/footer.jsp"/>