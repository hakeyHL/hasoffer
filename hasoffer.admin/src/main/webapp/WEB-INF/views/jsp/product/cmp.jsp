<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fun" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>
<%
    String contextPath = request.getContextPath();
%>
<style>
    .p_detail_image {
        width: 60px;
        max-height: 100px;
    }

    .p_detail_image_div {
        float: left;
        margin: 20px;
    }

    .p_detail_name_td {
        width: 35%;
        text-align: right;
    }

    .p_list_image {
        width: 40px;
        max-height: 60px;
    }
</style>
<!-- 批量删除确认 -->
<div class="modal fade" id="batchDeleteModel">
    <div class="modal-dialog">
        <div class="modal-content message_align">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">×</span></button>
                <h4 class="modal-title">提示信息/Friendly Reminder</h4>
            </div>
            <div class="modal-body">
                <p>您确认要删除选中记录吗？</p><br/>

                <p>Are you sure you want to delete the selected record</p>
            </div>
            <div class="modal-footer">
                <input type="hidden" id="batchUrl"/>
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    <span>取消</span><br/>
                    <span>Cancel</span>
                </button>
                <a onclick="batchUrlSubmit()" class="btn btn-success" data-dismiss="modal">
                    <span>确定</span><br/>
                    <span>Ok</span>
                </a>
            </div>
        </div>
    </div>
</div>
<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">比价信息/Product Info</h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <div class="row">

        <div class="col-lg-12">
            <div class="col-lg-8">

                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">基本信息/Basic Info（点击标题查看商品详情）</div>
                        <div class="panel-body">
                            <p>
                                标题(title) : <a href="/p/detail/${product.id}" target="_blank">${product.title}</a>
                            </p>

                            <p>
                                类目(category) :
                                <c:forEach items="${product.categories}" var="cate" varStatus="vs">
                                    ${cate.name}
                                    <c:if test="${!vs.last}">/</c:if>
                                </c:forEach>
                            </p>

                            <p>颜色(color) : ${product.color}</p>

                            <p>大小(size) : ${product.size}</p>

                            <p>评论(reviews): ${product.rating} </p>

                            <div class="row">
                                <div class="col-lg-12">
                                    <button type="button" class="btn btn-primary"
                                            onclick="batchDelete('<%=contextPath%>/p/batchDelete')"
                                            data-toggle="modal" data-target="#confirm-delete">
                                        <span>批量删除SKU</span><br/><span>Batch Delete Product's Skus</span>
                                    </button>
                                    <%--</div>--%>
                                    <%--<div class="col-lg-12">--%>
                                    <button type="button" class="btn btn-primary"
                                            onclick="removeCache(${pId})"
                                            data-toggle="modal" data-target="#confirm-delete">
                                        <span>更新商品价格&清除缓存</span><br/><span>Update product price&Remove Product Cache</span>
                                    </button>
                                    <button id="changeStatus" class="btn btn-info" onclick="change(${pId})">
                                        <span>切换状态</span><br/><span>Toggle Status</span>
                                    </button>
                                    <button id="delBtn" class="btn btn-danger" onclick="delProduct(${pId})">
                                        <span>删除商品</span><br/><span>Delete Product</span>
                                    </button>
                                </div>

                            </div>
                            <div class="col-lg-12">
                                <c:forEach items="${imageUrls}" var="imageUrl">
                                    <div class="p_detail_image_div">
                                        <img src="${imageUrl}" class="p_detail_image">
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <div class="panel panel-default">
                        <!-- Default panel contents -->
                        <div class="panel-heading">比价列表/Sku List
                            <button onclick="compareCtrl.preAdd()">新增/Add Sku</button>
                        </div>

                        <!-- Table -->
                        <table class="table table-bordered table-hover table-condensed" style="font-size:12px;">
                            <thead>
                            <tr>
                                <td><input type="checkbox" id="checkAll"/><span>全选</span><br/><span>Select All</span>
                                </td>
                                <td><span>序号</span><br/><span>SkuId</span></td>
                                <td><span>网站</span><br/><span>Website</span></td>
                                <td><span>图片</span><br/><span>image</span></td>
                                <td><span>SKU信息</span><br/><span>Sku title</span></td>
                                <td><span>价格(Rs.)</span><br/><span>Sku price</span></td>
                                <td><span>状态</span><br/><span>Sku status</span></td>
                                <td><span>更新时间</span><br/><span>Sku last updateTime</span></td>
                                <td><span>执行操作</span><br/><span>Operation</span></td>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${cmpSkus}" var="cmpSku">
                                <tr onclick="clickOnTr(${cmpSku.id})">
                                    <td><input id="subBox${cmpSku.id}" type="checkbox" name="subBox"
                                               value="${cmpSku.id}"/></td>
                                    <td>${cmpSku.id}</td>
                                    <td>${cmpSku.website}</td>
                                    <td>
                                        <img src="${cmpSku.imageUrl}" class="p_list_image"/>
                                    </td>
                                    <td>
                                            ${cmpSku.title}
                                        <br/>
                                        <span style="color: #a0a0a0">${cmpSku.color}/${cmpSku.size}</span>
                                    </td>
                                    <td><a href="${cmpSku.url}" target="_blank">${cmpSku.price}</a></td>
                                    <td>${cmpSku.status}</td>
                                    <td>${cmpSku.updateTime}</td>
                                    <td>
                                        <a class="active" href="javascript:void(0);"
                                           onclick="compareCtrl.preModify(${cmpSku.id}, '${cmpSku.url}', ${cmpSku.price}, '${cmpSku.size}', '${cmpSku.color}')">
                                            <span>编辑</span><br/><span>Edit</span>
                                        </a>
                                    </td>
                                    <td><a href="javascript:void(0);"
                                           onclick="compareCtrl.delete(${cmpSku.id})">
                                        <span>删除</span><br/><span>Delete</span>
                                    </a></td>
                                        <%-- <td><a href="javascript:void(0)"
                                                onclick="compareCtrl.update(${cmpSku.id})">
                                             <span>更新</span><br/><span>Update</span>
                                             </a></td>--%>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${showCharts}">
                        <div class="panel panel-default">
                            <!-- Default panel contents -->
                            <div class="panel-heading">价格曲线</div>
                            <div id="priceLogs"></div>
                        </div>
                    </c:if>

                    <div class="panel panel-default">
                        <!-- Default panel contents -->
                        <div class="panel-heading">新建/编辑</div>
                        <div class="panel-body">
                            <form class="form-horizontal" method="post" action="/p/cmp/save">
                                <input type="hidden" name="productId" value="${product.id}">

                                <div class="form-group">
                                    <label class="col-sm-2 control-label">序号/Id</label>

                                    <div class="col-sm-4">
                                        <label class="form-control" id="label_id">新建</label>
                                        <input type="hidden" id="id" name="id">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">链接/Url</label>

                                    <div class="col-sm-10">
                                        <input type="text" class="form-control" name="url" id="url">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">价格（Rs.）</label>

                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" placeholder="0.00" name="price"
                                               id="price">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">颜色/Color</label>

                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" name="color" id="color">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">大小/Size</label>

                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" name="size" id="size">
                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-sm-offset-2 col-sm-10">
                                        <button type="submit" class="btn btn-default" id="btn_submit">创建</button>
                                        <button type="button" class="btn btn-default" onclick="compareCtrl.preAdd()">
                                            重置
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    var compareCtrl = {
        preModify: function (id, url, price, size, color) {
            $("#id").val(id);
            $("#url").val(url);
            $("#price").val(price);
            $("#size").val(size);
            $("#color").val(color);
            $("#label_id").html("更新/Update - " + id);
            $("#btn_submit").html("更新/Update");
            document.body.scrollTop = 1000000;
        },
        preAdd: function () {
            $("#id").val("");
            $("#url").val("");
            $("#price").val("");
            $("#size").val("");
            $("#color").val("");
            $("#label_id").html("新建");
            $("#btn_submit").html("创建");
            document.body.scrollTop = 1000000;
        },
        delete: function (id) {
            if (confirm("将会删除比价记录，继续？" +
                            "selected product will be delete ,continue")) {

                http.doGet("/p/cmp/del/" + id, function () {
                    window.location.reload()
                });
            }
        },
        update: function (id) {
            http.doPost("/p/cmp/update/" + id, null, function (data) {
                var status = data.status;
                if (status == 'success') {
                    alert("更新成功" +
                            "Update success");
                    window.location.reload()
                } else {
                    alert("更新失败" +
                            "Update fail");
                }
            });
        }
    };

    $('#color').typeahead({source:  <c:forEach items="${skuColors}" var="co">${co}, </c:forEach>});
    $('#size').typeahead({source:  <c:forEach items="${skuSizes}" var="co">${co}, </c:forEach>});

    $(function () {
        $('#priceLogs').highcharts({
            title: {
                text: '',
                x: -20 //center
            },
            subtitle: {
                text: '',
                x: -20
            },
            xAxis: {
                categories: ${priceDays}
            },
            yAxis: {
                title: {
                    text: '价格(Rs.)'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                valueSuffix: 'Rs.'
            },
            legend: {
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'middle',
                borderWidth: 0
            },
            series: ${priceMap}
        });
    });
</script>

<script>

    //清除缓存
    function removeCache(productId) {

        url = "/p/removeCache/" + productId;

        http.doPost(url, {productId: productId}, function (data) {
            if (data.status == 'success') {
                BootstrapDialog.alert("成功/Success");
            }
            if (data.status == 'fail') {
                BootstrapDialog.warning("失败/Fail");
            }
        });
    }

    function delProduct(pid) {

        if (!confirm("您正在删除商品\n这是一个危险的操作\n删除商品会导致与商品关联的搜索日志会被置为初始状态\n请确认")) {
            return;
        }

        http.doGet("/fixdata/deleteproductanyway/" + pid, function (data) {
            alert(data.result);
        })
    }

    //状态切换
    function change(topSellingId) {

        url = "/topselling/changeStatus/" + topSellingId;
        http.doGet(url);

        window.location.href = "/topselling/list?topSellingStatusString=ONLINE&tmp=" + Math.random() * 10000000000000000;
    }
    function batchDelete(batchUrl) {

        var arr = new Array();
        $("input[name='subBox']:checked").each(function () {
            arr.push($(this).val());
        });

        if (arr.length == 0) {
            BootstrapDialog.show({
                title: '提示',
                message: '请选择要删除的记录!'
            });

            return false;
        }

        $('#batchUrl').val(batchUrl);//给会话中的隐藏属性URL赋值
        $('#batchDeleteModel').modal();
    }
    //全选/全不选
    $("#checkAll").click(function () {
        $("input[name='subBox']:checkbox").prop("checked", this.checked);
    });

    var $subBox = $("input[name='subBox']");
    $subBox.click(function () {
        $("#checkAll").prop("checked", $subBox.length == $("input[name='subBox']:checked").length ? true : false);
    });

    function clickOnTr(skuId) {
        var checked = $("#subBox" + skuId).prop("checked");
        $("#subBox" + skuId).prop("checked", !checked);
    }

    function batchUrlSubmit() {
        var url = $.trim($("#batchUrl").val());//获取会话中的隐藏属性URL

        var arr = new Array();
        $("input[name='subBox']:checked").each(function () {
            arr.push($(this).val());
        });

        $.ajax({
            url: url,
            type: 'GET',
            data: {"ids": arr},
            dataType: "json",
            contentType: 'application/json;charset=utf-8', //设置请求头信息
            success: function (result) {
                if (result) {
                    $("#delete_success").css("display", "block").hide(3000);
                    window.location.reload();
                }
            },
            error: function (xhr, textStatus, errorThrown) {
                $("#delete_fail").css("display", "block").hide(3000);
            }
        });
    }
</script>

<jsp:include page="../include/footer.jsp"/>