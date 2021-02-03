<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<body>
	<#include "top_banner.ftl">
	<table width="100%" border="1">
		<tr>
			<td align="center"><b>设置默认单个IP的QPS</b></td>
		</tr>
		<tr>
			<td>
				<form action="/limiter/qpsLimit/resetLimitPretty<#if appName??>?appName=${appName}</#if>" method="post">
				  <#if save??>
				  <font color="red">设置保存成功</font><br>
				  </#if>
				  （默认）单个IP的QPS（当前<#if permitsPerSecondEachIp??>${permitsPerSecondEachIp?c!'50'}<#else>-1</#if>）: <input type="number" name="permitsPerSecondEachIp" value="<#if permitsPerSecondEachIp??>${permitsPerSecondEachIp?c!'50'}<#else>-1</#if>"/>
				  <input type="hidden" name="save" value="true">
				  <input type="submit" value="保存" />
				</form>
			</td>
		</tr>
	</table>
	<br/>
	<table width="100%" border="1">
		<#if opType??>
		<#if opType=="add">
		<tr>
			<td align="left"><font color="red"><b>IP ${ip!''} 设置QPS成功！</b></font></td>
		</tr>
		</#if>
		</#if>
		<tr>
			<td align="center"><b>新增IP或IP段的QPS设置</b></td>
		</tr>
		<tr>
			<td align="left">
			<form action="/limiter/qpsLimit/setIpLimitPretty<#if appName??>?appName=${appName}</#if>" method="post">
				IP或IP段：<input type="text" name="ip"/>&nbsp;&nbsp;
				QPS：<input type="number" name="limit"/>&nbsp;&nbsp;
				<input type="hidden" name="opType" value="add" />
				<input type="submit" value="增加" />
				<br>
				注：支持IP及IP段，IP段支持的格式如127.0.0.*、127.0.*.*、127.*.*.*
			</form>
			</td>
		</tr>
	</table>
	<br/>
	<table width="100%" border="1">
		<#if opType??>
		<#if opType=="modify">
		<tr>
			<td align="left" colspan="4"><font color="red"><b>IP ${ip!''} 修改QPS成功！</b></font></td>
		</tr>
		</#if>
		</#if>
		<tr>
			<td align="center" colspan="4"><b>查看及修改IP及IP段的QPS设置</b></td>
		</tr>
		<tr>
			<td style="font-weight:bold" width="30%">IP或IP段</td>
			<td style="font-weight:bold" width="30%">加入日期</td>
			<td style="font-weight:bold" width="20%">QPS</td>
			<td style="font-weight:bold" width="20%">操作</td>
		</tr>
		<#if ipQpsList??>
		<#list ipQpsList as ipQps>
		<tr>
			<td>${ipQps.ip!''}</td>
			<td>${ipQps.addDate?string("yyyy-MM-dd HH:mm:ss")}</td>
			<td>
			<form action="/limiter/qpsLimit/setIpLimitPretty<#if appName??>?appName=${appName}</#if>" method="post">
			<input type="hidden" name="ip" value="${ipQps.ip!''}" />
			<input type="text" name="limit" value="${ipQps.limit?c!0}" />
			<input type="hidden" name="opType" value="modify" />
			<input type="submit" value="修改" />
			</form>
			</td>
			<td>
			<form action="/limiter/qpsLimit/delIpLimitPretty<#if appName??>?appName=${appName}</#if>" method="post">
			<input type="hidden" name="ip" value="${ipQps.ip!''}" />
			<input type="hidden" name="opType" value="del" />
			<input type="submit" value="删除" />
			</form>
			</td>
		</tr>
		</#list>
		</#if>
	</table>
	<br/>
	<table width="100%" border="1">
		<tr>
			<td align="center" colspan="4"><b>查看全局IP及IP段的QPS设置</b></td>
		</tr>
		<tr>
			<td style="font-weight:bold" width="30%">IP或IP段</td>
			<td style="font-weight:bold" width="30%">加入日期</td>
			<td style="font-weight:bold" width="20%">QPS</td>
			<td style="font-weight:bold" width="20%">操作</td>
		</tr>
		<#if globalIpQpsList??>
		<#list globalIpQpsList as ipQps>
		<tr>
			<td>${ipQps.ip!''}</td>
			<td>${ipQps.addDate?string("yyyy-MM-dd HH:mm:ss")}</td>
			<td>${ipQps.limit?c!0}</td>
			<td>不可操作</td>
		</tr>
		</#list>
		</#if>
	</table>
</body>
</html>