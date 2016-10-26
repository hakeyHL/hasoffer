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
            <h1 class="page-header">push创建</h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>

    <div class="row" style="margin-bottom: 10px">
    </div>

    <form action="/push/create/${pushSourceType}/${sourceId}" class="form-horizontal" role="form" method="post"
          enctype="multipart/form-data" onsubmit="dosubmit()">
        <div class="form-group">
            <label for="pushType" class="col-sm-2 control-label">推送类型</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="pushType" name="pushType" placeholder="Deal"
                       value="${pushType}" disabled>
            </div>
        </div>
        <div class="form-group">
            <label for="crowd" class="col-sm-2 control-label">推送人群</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="crowd" name="crowd" placeholder="所有设备" value="${crowd}"
                       disabled>
            </div>
        </div>
        <%--图片--%>
        <div class="form-group">
            <label class="col-sm-3 control-label">推送图片：</label>

            <div class="col-sm-7">
                <div class="control-group">
                    <div class="controls" style="width: 300px">
                        <div class="fileupload fileupload-new" data-provides="fileupload"><input type="hidden" value=""
                                                                                                 name="">

                            <div class="fileupload-new thumbnail" style="width: 200px; height: 150px;">
                                <img src="${imageUrl}" alt="" id="image_url">
                                <input name="imageUrl" value="${hasoferOriImageUrl}" id="imageUrl" type="hidden">
                            </div>
                            <div class="fileupload-preview fileupload-exists thumbnail"
                                 style="max-width: 200px; max-height: 150px; line-height: 20px;"
                                 id="dealImagePreview"></div>
                            <div>
                                        <span class="btn btn-file"><span class="fileupload-new">选择图片</span>
                                        <span class="fileupload-exists">更换</span>
                                        <input type="file" class="default" id="upload_img" name="pushImageFile"
                                               img_url="false"></span>
                            </div>
                        </div>
                    </div>
                    <div id="tip_div"
                         style="margin: 10px; width: 200px; color: rgb(255, 0, 0); display: none; position:absolute;top:60px;left:226px">
                        请选择图片
                    </div>
                </div>
            </div>
        </div>
        <div class="form-group">
            <label for="pushSourceId" class="col-sm-2 control-label">配置跳转参数</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="pushSourceId" value="${pushSourceId}">
            </div>
        </div>
        <div class="form-group">
            <label for="pushTitle" class="col-sm-2 control-label">推送文案标题</label>

            <div class="col-sm-10">
                <input type="text" class="form-control" id="pushTitle" value="${pushTitle}">
            </div>
        </div>
        <div class="form-group">
            <label for="pushContent" class="col-sm-2 control-label">推送文案详情</label>

            <div class="col-sm-10">
                <textarea class="form-control" id="pushContent" name="pushContent" rows="13">${pushContent}</textarea>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-default">确认无误,推送</button>
            </div>
        </div>
    </form>

    <jsp:include page="../include/page.jsp"/>
</div>
<script>

    $().ready(function () {
                var img = $("#image_url");
                var imgUrl = "${imageUrl}";
                if (imgUrl != "") {
                    img.attr("src", imgUrl);
                }
            }
    );

    function dosubmit() {
        var dealImagePreviewImg = $("#dealImagePreview img").length;
        if (dealImagePreviewImg > 0) {
            $("#upload_img").attr("img_url", true);
            $("#imageUrl").val("");
        }
        return true;
    }
</script>
<jsp:include page="../include/footer.jsp"/>
