<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    String contextPath = request.getContextPath();
%>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>


<div id="page-wrapper">

    <div class="col-lg-12" style="margin: 20px"></div>

            <form class="form-horizontal" action="<%=contextPath%>/deal/edit" enctype="multipart/form-data" method="post">

                <input type="hidden" name="id" value="${deal.id}">
                <input type="hidden" name="website" value="${deal.website}">
                <div class="form-group">
                    <label  class="col-sm-3 control-label">Deal标题：</label>
                    <div class="col-sm-7">
                        <input type="text" name="title" class="form-control" value="${deal.title}" placeholder="Deal标题">
                    </div>
                </div>

                <div class="form-group">
                    <label  class="col-sm-3 control-label">Deal跳转链接：</label>
                    <div class="col-sm-7">
                        <input type="text" name="linkUrl" class="form-control" value="${deal.linkUrl}"  placeholder="Deal跳转链接">
                    </div>
                </div>


                <<div class="form-group">
                    <label  class="col-sm-3 control-label">Deal图片：</label>
                    <div class="col-sm-7">
                        <div class="control-group">
                            <div class="controls">
                                <div class="fileupload fileupload-new" data-provides="fileupload"><input type="hidden" value="" name="">
                                    <div class="fileupload-new thumbnail" style="width: 200px; height: 150px;">
                                        <img src="http://www.placehold.it/200x150/EFEFEF/AAAAAA&amp;text=没有图片" alt="">
                                    </div>
                                    <div class="fileupload-preview fileupload-exists thumbnail" style="max-width: 200px; max-height: 150px; line-height: 20px;"></div>
                                    <div>
                                <span class="btn btn-file"><span class="fileupload-new">选择图片</span>
                                    <span class="fileupload-exists">更换</span>
                                    <input type="file" class="default" name="file"></span>
                                        <a href="#" class="btn fileupload-exists" data-dismiss="fileupload">删除</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>


                <div class="form-group">
                    <label  class="col-sm-3 control-label">推送到banner展示：</label>
                    <div class="col-sm-7">

                        <label class="radio-inline">
                            <input type="radio" name="push" id="inlineRadio1" > 展示
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="push" id="inlineRadio2" > 不展示
                        </label>

                    </div>
                </div>

                <div class="form-group">
                    <label  class="col-sm-3 control-label">生效时间：</label>
                    <div class="col-sm-6">
                        <input type="text" name="createTime" id="createTime" value="${deal.createTime}" class="form-control form_datetime"  >
                    </div>
                </div>
                <script>
                    $("#createTime").datepicker();
                </script>

                <div class="form-group">
                    <label class="col-sm-3 control-label">失效时间：</label>
                    <div class="col-sm-6">
                        <input type="text" name="expireTime" id="expireTime" value="${deal.expireTime}" class="form-control form_datetime"  >
                    </div>
                </div>
                <script>
                    $("#expireTime").datepicker();
                </script>

                <div class="form-group">
                    <label  class="col-sm-3 control-label">deal描述：</label>
                    <div class="col-sm-7">
                        <textarea class="form-control" name="description" rows="5" content="${data.description}"></textarea>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-offset-3 col-sm-9">
                        <button type="submit" class="btn btn-default" id="button_submit">确定</button>
                    </div>
                </div>

            </form>


</div>

<script>
    $(function(){
        var push = "${deal.push}";
        var inlineRadio1 = $("#inlineRadio1");
        var inlineRadio2 = $("#inlineRadio2");

        if(push == true){
            inlineRadio1.attr("checked", "checked");
        }else{
            inlineRadio2.attr("checked", "checked");
        }
    });

    // 定义提交按钮btn
   /* var btn = $("#button_submit");
    *//**
     * 绑定btn按钮的监听事件
     *//*
    var bindBtn = function(){
        btn.click(function(){
            // 需要先禁用按钮（为防止用户重复点击）
            btn.unbind('click');

            $.ajax({
                // ...
            })
                    .done(function () {
                        alert('提交成功');
                        //成功，跳转
                    })
                    .fail(function () {
                        //失败，弹窗
                        alert('服务器错误');

                        // ** 重要：因为提交失败，所以要恢复提交按钮的监听 **
                        bindBtn();
                    });
        })
    }
*/

</script>

<jsp:include page="../include/footer.jsp"/>
