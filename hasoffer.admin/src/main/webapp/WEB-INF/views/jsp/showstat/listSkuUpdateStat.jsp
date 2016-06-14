<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">sku更新统计</h1>
        </div>

        <form action="/showstat/listskuupdate" method="get">

            <div class="col-lg-2">
                <div class="input-group">
                    <span class="input-group-addon">站点</span>
                    <select id="webSiteSelect" class="form-control" name="webSite">
                        <option value="ALL">全部渠道</option>
                        <option value="FLIPKART" <c:if test="${webSite=='flipkart'}">selected</c:if>>flipkart</option>
                        <option value="SNAPDEAL" <c:if test="${webSite=='snapdeal'}">selected</c:if>>snapdeal</option>
                        <option value="SHOPCLUES" <c:if test="${webSite=='shopclues'}">selected</c:if>>shopclues
                        </option>
                        <option value="PAYTM" <c:if test="${webSite=='paytm'}">selected</c:if>>paytm</option>
                        <option value="AMAZON" <c:if test="${webSite=='amazon'}">selected</c:if>>amazon</option>
                        <option value="EBAY" <c:if test="${webSite=='ebay'}">selected</c:if>>ebay</option>
                        <option value="INFIBEAM" <c:if test="${webSite=='infibeam'}">selected</c:if>>infibeam</option>
                    </select>
                </div>
            </div>

            <div class="col-lg-2">
                <div class="input-group">
                    <span class="input-group-addon">起始时间</span>
                    <input size="16" type="text" class="form-control form_datetime" id="startTime" name="startTime"
                           value="${startTime}">
                </div>
                <script>
                    $("#startTime").datepicker({dateFormat: 'yy-mm-dd'});
                </script>
            </div>

            <div class="col-lg-2">
                <div class="input-group">
                    <span class="input-group-addon">结束时间</span>
                    <input size="16" type="text" class="form-control form_datetime" id="endTime" name="endTime"
                           value="${endTime}">
                </div>
                <script>
                    $("#endTime").datepicker({dateFormat: 'yy-mm-dd'});
                </script>
            </div>
            <div class="col-lg-2">
                <button type="submit" class="btn btn-primary">查询</button>
            </div>

            <div class="col-lg-12" style="margin: 5px"></div>
        </form>

        <div class="col-lg-12" style="margin: 5px"></div>

        <div class="row">
            <div class="col-lg-12">
                <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                    <thead>
                    <tr>
                        <td>日期</td>
                        <td>站点</td>
                        <td>ONSALE数量</td>
                        <td>OUTSTOCK数量</td>
                        <td>OFFSALE数量</td>
                        <td>更新成功</td>
                        <td>3日未更新</td>
                        <td>当日新增</td>
                        <td>索引数量</td>
                        <td>当日新增索引数量</td>
                        <td>更新时间</td>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${cmpUpdateVoList}" var="cmpUpdateVo">
                        <tr>
                            <td>${cmpUpdateVo.date}</td>
                            <td>${cmpUpdateVo.website}</td>
                            <td>${cmpUpdateVo.onSaleAmount}</td>
                            <td>${cmpUpdateVo.soldOutAmount}</td>
                            <td>${cmpUpdateVo.offsaleAmount}</td>
                            <td>${cmpUpdateVo.updateSuccessAmount}/${cmpUpdateVo.proportion}%</td>
                            <td>${cmpUpdateVo.alwaysFailAmount}</td>
                            <td>${cmpUpdateVo.newSkuAmount}</td>
                            <td>${cmpUpdateVo.indexAmount}</td>
                            <td>${cmpUpdateVo.newIndexAmount}</td>
                            <td>${cmpUpdateVo.updateTime}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

        </div>
        <%--<jsp:include page="../include/page.jsp"/>--%>
        <jsp:include page="../include/page.jsp"/>
    </div>

<jsp:include page="../include/footer.jsp"/>