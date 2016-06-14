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
    <div class="row" style="padding-top: 20px; margin-bottom: 20px;">
        <form action="/s2/list" method="get">
            <div class="col-lg-2">
                <div class="input-group">
                    <span class="input-group-addon">结果</span>
                    <select class="form-control" name="precise">
                        <option value="ALL" <c:if test="${page.pageParams.precise=='ALL'}">selected</c:if>>
                            全部
                        </option>
                        <option value="NOCHECK" <c:if test="${page.pageParams.precise=='NOCHECK'}">selected</c:if>>
                            未检查
                        </option>
                        <option value="MANUALSET" <c:if test="${page.pageParams.precise=='MANUALSET'}">selected</c:if>>
                            已人工关联
                        </option>
                    </select>
                </div>
            </div>

            <div class="col-lg-2">
                <button type="submit" class="btn btn-primary">查询</button>
            </div>
        </form>
    </div>

    <jsp:include page="./list-table.jsp"/>

    <jsp:include page="../include/page.jsp"/>
</div>

<jsp:include page="../include/footer.jsp"/>