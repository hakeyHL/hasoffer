<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>

<!-- Javascipt -->
<script src="${ctx}/js/jquery.js"></script>
<script src="${ctx}/js/main.js "></script>
<nav>
    <ul>
        <li>
            <a href="javascript:;"><i class="fa fa-lg fa-fw fa-inbox"></i> <span class="menu-item-parent">功能菜单</span><b
                    class="collapse-sign"><em class="fa fa-expand-o"></em></b></a>
            <ul>
                <li>
                    <a href="${ctx}/layout/showUpdateStats" target="frameContent">更新统计</a>
                </li>
                <li>
                    <a href="${ctx}/layout/showJob" target="frameContent">任务调度</a>
                </li>
            </ul>
        </li>
    </ul>
</nav>
