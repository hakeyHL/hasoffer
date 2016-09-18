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
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">Spell Checker</h1>
        </div>
    </div>
    <form action="/s/spell" method="get">
        <div class="row" style="margin-bottom: 20px;">
            <div class="input-group col-lg-4">
                <input type="text" name="text" class="form-control" placeholder="..." value="${text}">
                <span class="input-group-btn">
                    <button type="submit" class="btn btn-primary">Check it</button>
                </span>
            </div>
        </div>

    </form>
    <div class="row col-lg-5">
        <table class="table">
            <tr>
                <td>Status:${result}</td>
            </tr>
            <c:forEach items="${sugs}" var="sug">
                <tr>
                    <td>
                            ${sug}
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>

</div>

<jsp:include page="../include/footer.jsp"/>