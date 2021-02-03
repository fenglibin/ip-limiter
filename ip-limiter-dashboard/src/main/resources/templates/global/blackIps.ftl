<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<body>
	<#include "top_banner.ftl">
	<table width="100%" border="1">
		<#if addIp??>
		<tr><td style="font-weight:bold;color:red">IP ${addIp} 已经加入到黑名单中</td></tr>
		</#if>
		<tr>
			<td align="center">增加IP黑名单</td>
		</tr>
		<tr>
			<td>
				<form action="/global-limiter/addBlackIpPretty" method="post">
				  黑名单ＩＰ: <input type="text" name="ip" />
				  <input type="submit" value="增加" />
				  <br>
				  注：支持IP及IP段，IP段支持的格式如127.0.0.*、127.0.*.*、127.*.*.*
				</form>
			</td>
		</tr>
	</table>
	<br>
	<table width="100%" border="1">
		<#if delIp??>
		<tr><td  colspan="3" style="font-weight:bold;color:red">IP ${delIp} 已经从黑名单中移除</td></tr>
		</#if>
		<tr>
			<td colspan="3" align="center">全局IP黑名单列表</td>
		</tr>
		<tr>
			<td style="font-weight:bold"><b>IP</b></td>
			<td style="font-weight:bold">加入日期</td>
			<td style="font-weight:bold">操作</td>
		</tr>
		<#if globalBlackIpList??>
		<#list globalBlackIpList as blackIp>
		<tr>
			<td>${blackIp.ip}</td>
			<td>${(blackIp.addDate?string("yyyy-MM-dd hh:mm:ss"))!}</td>
			<td><a href="/global-limiter/removeOneIpFromBlackIpsPretty?ip=${blackIp.ip}">删除</a></td>
		</tr>
		</#list>
		</#if>
	</table>
</body>
</html>