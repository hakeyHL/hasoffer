<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fun" %>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/left.jsp"/>

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

<div id="page-wrapper">
    <div class="row">
        <div class="col-lg-12">
            <h1 class="page-header">比价信息</h1>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <div class="row">

        <div class="col-lg-12">
            <div class="col-lg-8">

                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">基本信息（点击标题查看商品详情）</div>
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
                        <div class="panel-heading">比价列表
                            <button onclick="compareCtrl.preAdd()">新增</button>
                        </div>

                        <!-- Table -->
                        <table class="table table-condensed table-bordered">
                            <thead>
                            <tr>
                                <td>序号</td>
                                <td>网站</td>
                                <td>图片</td>
                                <td>SKU信息</td>
                                <td>价格(Rs.)</td>
                                <td>状态</td>
                                <td>更新时间</td>
                                <td></td>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${cmpSkus}" var="cmpSku">
                                <tr>
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
                                        <a href="javascript:void(0);"
                                           onclick="compareCtrl.preModify(${cmpSku.id}, '${cmpSku.url}', ${cmpSku.price}, '${cmpSku.size}', '${cmpSku.color}')">
                                            编辑</a>
                                        <a href="javascript:void(0);"
                                           onclick="compareCtrl.delete(${cmpSku.id})">删除</a>
                                        <a href="javascript:void(0)"
                                           onclick="compareCtrl.update(${cmpSku.id})">更新</a>
                                    </td>
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
                                    <label class="col-sm-2 control-label">序号</label>

                                    <div class="col-sm-4">
                                        <label class="form-control" id="label_id">新建</label>
                                        <input type="hidden" id="id" name="id">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">链接</label>

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
                                    <label class="col-sm-2 control-label">颜色</label>

                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" name="color" id="color">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="col-sm-2 control-label">大小</label>

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
            $("#label_id").html("更新 - " + id);
            $("#btn_submit").html("更新");
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
            if (confirm("将会删除比价记录，继续？")) {
                http.doGet("/p/cmp/del/" + id, function () {
                    window.location.reload()
                });
            }
        },
        update: function (id) {
            http.doPost("/p/cmp/update/" + id,null, function (data) {
                var status = data.status;
                if (status == 'success') {
                    alert("更新成功");
                    window.location.reload()
                } else {
                    alert("更新失败");
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

<jsp:include page="../include/footer.jsp"/>