<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<body>
	<#include "top_banner.ftl">
	<table width="100%" border="1">
		<#if addIp??>
		<tr><td style="font-weight:bold;color:red">IP ${addIp} 已经加入到白名单中</td></tr>
		</#if>
		<tr>
			<td align="center">增加IP白名单</td>
		</tr>
		<tr>
			<td>
				<form action="/limiter/whiteIp/addWhiteIpPretty<#if appName??>?appName=${appName}</#if>" method="post">
				  白名单ＩＰ: <input type="text" name="ip" />
				  <input type="submit" value="增加" />
				  <br>
				  注：<br>
				  １、支持IP及IP段，IP段支持的格式如127.0.0.*、127.0.*.*、127.*.*.* <br>
				  ２、白名单的规则优于黑名单规则，在白名单中的IP不受访问QPS限制，也不会因为在黑名单中被限制
				</form>
			</td>
		</tr>
	</table>
	<br>
	<table width="100%" border="1">
		<#if delIp??>
		<tr><td  colspan="3" style="font-weight:bold;color:red">IP ${delIp} 已经从白名单中移除</td></tr>
		</#if>
		<tr>
			<td colspan="3" align="center">IP白名单列表</td>
		</tr>
		<tr>
			<td style="font-weight:bold" width="35%">IP</td>
			<td style="font-weight:bold" width="35%">加入日期</td>
			<td style="font-weight:bold" width="30%">操作</td>
		</tr>
		<#if whiteIpList??>
		<#list whiteIpList as whiteIp>
		<tr>
			<td>${whiteIp.ip}</td>
			<td>${(whiteIp.addDate?string("yyyy-MM-dd HH:mm:ss"))!}</td>
			<td><a href="/limiter/whiteIp/removeOneIpFromWhiteIpsPretty?ip=${whiteIp.ip}<#if appName??>&appName=${appName}</#if>">删除</a></td>
		</tr>
		</#list>
		</#if>
	</table>
	<br>
	<table width="100%" border="1">
		<tr>
			<td colspan="3" align="center">全局IP白名单列表</td>
		</tr>
		<tr>
			<td style="font-weight:bold" width="35%"><b>IP</b></td>
			<td style="font-weight:bold" width="35%">加入日期</td>
			<td style="font-weight:bold" width="30%">操作</td>
		</tr>
		<#if globalWhiteIpList??>
		<#list globalWhiteIpList as whiteIp>
		<tr>
			<td>${whiteIp.ip}</td>
			<td>${(whiteIp.addDate?string("yyyy-MM-dd HH:mm:ss"))!}</td>
			<td>不可操作</td>
		</tr>
		</#list>
		</#if>
	</table>
</body>
</html>