<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>


<div id="page-wrapper">
    <div class="row">
        <form action="import" enctype="multipart/form-data" method="post" id="form">

            <div class="col-lg-12" style="margin: 5px"></div>

            <div class="col-lg-12" >
                <span class="modal-title">请选择Excel文件:</span>
                <input type="file" name="multiFile" id="multiFile" class="file-loading" style="display: inline;"/>

            </div>

            <div class="col-lg-12" style="margin: 5px"></div>
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

<script>
    $(function(){
        $('#multiFile').change(function(e){
            var _this = $(this);
            if(_this.val() == ''){
                alert("请选择文件");
                return false;
            }

           $('#form').submit();
        });
    });
</script>
<jsp:include page="../include/footer.jsp"/>
