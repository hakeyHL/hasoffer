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
                        <td>${updateRst.allTotal} | ${updateRst.allSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.allSuccess/updateRst.allTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.flipkartTotal} | ${updateRst.flipkartSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.flipkartSuccess/updateRst.flipkartTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.amazonTotal} | ${updateRst.amazonSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.amazonSuccess/updateRst.amazonTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.snapdealTotal} | ${updateRst.snapdealSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.snapdealSuccess/updateRst.snapdealTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.ebayTotal} | ${updateRst.ebaySuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.ebaySuccess/updateRst.ebayTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.shopcluesTotal} | ${updateRst.shopcluesSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.shopcluesSuccess/updateRst.shopcluesTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.paytmTotal} | ${updateRst.paytmSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.paytmSuccess/updateRst.paytmTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.myntraTotal} | ${updateRst.myntraSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.myntraSuccess/updateRst.myntraTotal}"></fmt:formatNumber>
                            %)
                        </td>
                        <td>${updateRst.infibeamTotal} | ${updateRst.infibeamSuccess}
                            (<fmt:formatNumber pattern="##.##"
                                               value="${100 * updateRst.infibeamSuccess/updateRst.infibeamTotal}"></fmt:formatNumber>
                            %)
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>

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
                    <c:forEach items="${updateRst2}" var="rst">
                        <tr>
                            <td>${rst.id}</td>
                            <td>${rst.allTotal} | ${rst.allSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.allSuccess/rst.allTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.flipkartTotal} | ${rst.flipkartSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.flipkartSuccess/rst.flipkartTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.amazonTotal} | ${rst.amazonSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.amazonSuccess/rst.amazonTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.snapdealTotal} | ${rst.snapdealSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.snapdealSuccess/rst.snapdealTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.ebayTotal} | ${rst.ebaySuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.ebaySuccess/rst.ebayTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.shopcluesTotal} | ${rst.shopcluesSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.shopcluesSuccess/rst.shopcluesTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.paytmTotal} | ${rst.paytmSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.paytmSuccess/rst.paytmTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.myntraTotal} | ${rst.myntraSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.myntraSuccess/rst.myntraTotal}"></fmt:formatNumber>
                                %)
                            </td>
                            <td>${rst.infibeamTotal} | ${rst.infibeamSuccess}
                                (<fmt:formatNumber pattern="##.##"
                                                   value="${100 * rst.infibeamSuccess/rst.infibeamTotal}"></fmt:formatNumber>
                                %)
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <%--<jsp:include page="../include/page.jsp"/>--%>
        </div>

<jsp:include page="../include/footer.jsp"/>