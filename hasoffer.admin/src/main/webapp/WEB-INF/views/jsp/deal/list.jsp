<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>


<div id="page-wrapper">

    <div class="modal fade in" id="import_result" tabindex="-1" role="dialog"
         aria-labelledby="myModalLabel" style="display: none;top:20%">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" id="myModalLabel">导入结果</h4>
                </div>
                <div class="modal-body">
                    <ul>
                        <li>本次导入表格共<span id="totalRows"></span>条</li>
                        <li>创建deal成功数量<span id="successRows"></span></li>
                        <li>创建失败数量<span id="failRows"></span></li>
                        <li>因网站名/deal名称/deal跳转链接为空失败<span id="nullRows"></span>条</li>
                        <li>因deal链接重复失败<span id="repeatRows"></span>条</li>
                        <li>其他失败<span id="otherFailRows"></span>条</li>
                    </ul>
                </div>
                <div class="modal-footer">
                    <button id="confirm_button" type="button" class="btn btn-primary">确定</button>
                </div>
            </div>
        </div>
    </div>


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
                        <td>${data.createTime}</td>
                        <td>${data.expireTime}</td>
                        <td><a href="detail/${data.id}" >编辑</a></td>
                        <td><a href="#">删除</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        <jsp:include page="../include/page.jsp"/>
    </div>

<script>
    $(function(){
        $('#multiFile').change(function(e){
            $("#form").ajaxSubmit({
                //定义返回JSON数据，还包括xml和script格式
                dataType:'json',
                beforeSend: function() {
                    //表单提交前做表单验证
                },
                success: function(data) {
                    if(data.success){
                        $("#totalRows").html(data.totalRows);
                        $("#successRows").html(data.successRows);
                        $("#failRows").html(data.failRows);
                        $("#nullRows").html(data._nullRows);
                        $("#repeatRows").html(data.repeatRows);
                        $("#otherFailRows").html(data.otherFailRows);
                        $('#import_result').modal('show');
                        $("#confirm_button").click(function(){
                            $('#import_result').modal('hide');
                            window.location.reload();
                        });
                    }else{
                        alert("导入失败，请检查文件格式之后重新导入！")
                    }


                }
            });
        });

    });

</script>
<jsp:include page="../include/footer.jsp"/>
