<html>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<body>
	<#include "top_banner.ftl">
	选择应用节点：
	<select id="ipId" onchange="changeIp(this.value)">
		<option value ="">全部节点</option>
	</select>
	&nbsp;&nbsp;选择查看的时间：
	<select id="lastSecondsId" name="lastSeconds" onchange="changeLastSeconds(this.value)">
		<#if !lastSeconds??>
			 <#assign lastSeconds=10/> 
		</#if>
		<option value ="5" <#if lastSeconds??><#if lastSeconds==5>selected</#if></#if>>最近5秒</option>
		<option value ="10" <#if lastSeconds??><#if lastSeconds==10>selected</#if></#if>>最近10秒</option>
		<option value ="20" <#if lastSeconds??><#if lastSeconds==20>selected</#if></#if>>最近20秒</option>
		<option value ="30" <#if lastSeconds??><#if lastSeconds==30>selected</#if></#if>>最近30秒</option>
		<option value ="60" <#if lastSeconds??><#if lastSeconds==60>selected</#if></#if>>最近60秒</option>
	</select>
	&nbsp;&nbsp;页面刷新频率:
	<select id="refreshIntervalId" name="refreshInterval" onchange="changeRefreshInterval(this.value)">
		<#if !refreshInterval??>
			 <#assign refreshInterval=5/> 
		</#if>
		<option value ="3" <#if refreshInterval??><#if refreshInterval==5>selected</#if></#if>>3秒</option>
		<option value ="5" <#if refreshInterval??><#if refreshInterval==5>selected</#if></#if>>5秒</option>
		<option value ="10" <#if refreshInterval??><#if refreshInterval==10>selected</#if></#if>>10秒</option>
		<option value ="20" <#if refreshInterval??><#if refreshInterval==20>selected</#if></#if>>20秒</option>
		<option value ="30" <#if refreshInterval??><#if refreshInterval==30>selected</#if></#if>>30秒</option>
	</select>
	&nbsp;&nbsp;<a href="javascript:void(0)" onclick="showIpDisplayType()">以时间秒统计维度查看</a>
	<input type="hidden" name="appName" id="appName" value="${appName!''}">
	<input type="hidden" name="clientIp" id="clientIp" value="${ip!''}">
	<input type="hidden" name="lastSeconds" id="lastSeconds" value="${lastSeconds!''}">
	<input type="hidden" name="refreshInterval" id="refreshInterval" value="${refreshInterval!''}">
	<input type="hidden" name="displayType" id="displayType" value="${displayType!''}">
	<br>
	<table width="100%" border="1">
		<tr>
			<td colspan="6" align="center" style="font-weight:bold">当前应用最近${lastSeconds!'10'}秒的IP访问TOP统计</td>
		</tr>
		<tr>
			<td style="font-weight:bold;background-color:yellow" width="16%"><b>IP</b></td>
			<td style="font-weight:bold;background-color:yellow" width="16%">统计时间</td>
			<td style="font-weight:bold;background-color:yellow" width="16%">访问总量</td>
			<td style="font-weight:bold;color:green;background-color:yellow" width="16%">正常访问总量</td>
			<td style="font-weight:bold;color:red;background-color:yellow" width="16%">拒绝访问总量</td>
			<td style="font-weight:bold;color:red;background-color:yellow" width="20%">操作</td>
		</tr>
		<#if secondsAccess??>
		<#list secondsAccess?keys as ipKey>
		<#assign index=0>
		<#assign secondAccess=secondsAccess[ipKey]>
		<#list secondAccess?keys as secondKey>
		<#assign access=secondAccess[secondKey]>
		<tr>
			<#if index==0>
			<td rowspan="${secondAccess?size}">${ipKey}</td>
			</#if>
			<#assign seconds=secondKey?number>
			<#assign seconds=seconds*1000>
			<td>${seconds?number_to_datetime?string("yyyy-MM-dd HH:mm:ss")}</td>
			<td>${access.total}</td>
			<td>${access.normal}</td>
			<td>${access.block}</td>
			<#if index==0>
			<td rowspan="${secondAccess?size}"><a href="/limiter/addBlackIpPretty?ip=${ipKey}">IP加入黑名单</a></td>
			</#if>
			<input type="hidden" name="urls" value="${access.urlsAccessStr}">
		</tr>
		<#assign index=index+1>
		</#list>
		</#list>
		</#if>
	</table>
</body>
<#include "loadAppNodesJs.ftl">
<script language="JavaScript">
	function changeIp(ip){
		var appName = document.getElementById("appName").value;
		var lastSeconds = document.getElementById("lastSecondsId").value;
		var ts = new Date().getTime();
		var refreshInterval = document.getElementById("refreshInterval").value;
		var displayType = document.getElementById("displayType").value;
		window.location.href=window.location.pathname+"?appName="+appName+"&ip="+ip+"&ts="+ts+"&lastSeconds="+lastSeconds+"&refreshInterval="+refreshInterval+"&displayType="+displayType;
	}
	function changeLastSeconds(lastSeconds){
		var appName = document.getElementById("appName").value;
		var ip = document.getElementById("ipId").value;
		var ts = new Date().getTime();
		var refreshInterval = document.getElementById("refreshInterval").value;
		var displayType = document.getElementById("displayType").value;
		window.location.href=window.location.pathname+"?appName="+appName+"&ip="+ip+"&ts="+ts+"&lastSeconds="+lastSeconds+"&refreshInterval="+refreshInterval+"&displayType="+displayType;
	}
	function changeRefreshInterval(refreshInterval){
		var appName = document.getElementById("appName").value;
		var lastSeconds = document.getElementById("lastSecondsId").value;
		var ip = document.getElementById("ipId").value;
		var ts = new Date().getTime();
		var displayType = document.getElementById("displayType").value;
		window.location.href=window.location.pathname+"?appName="+appName+"&ip="+ip+"&ts="+ts+"&lastSeconds="+lastSeconds+"&refreshInterval="+refreshInterval+"&displayType="+displayType;
	}
	function page_refresh()
	{
	   var ip = document.getElementById("ipId").value;
	   var appName = document.getElementById("appName").value;
	   var lastSeconds = document.getElementById("lastSecondsId").value;
	   var ts = new Date().getTime();
	   var refreshInterval = document.getElementById("refreshInterval").value;
	   var displayType = document.getElementById("displayType").value;
	   window.location.href=window.location.pathname+"?appName="+appName+"&ip="+ip+"&ts="+ts+"&lastSeconds="+lastSeconds+"&refreshInterval="+refreshInterval+"&displayType="+displayType;
	}
	function showIpDisplayType()
	{
	   var ip = document.getElementById("ipId").value;
	   var appName = document.getElementById("appName").value;
	   var lastSeconds = document.getElementById("lastSecondsId").value;
	   var ts = new Date().getTime();
	   var refreshInterval = document.getElementById("refreshInterval").value;
	   var displayType = document.getElementById("displayType").value;
	   window.location.href=window.location.pathname+"?appName="+appName+"&ip="+ip+"&ts="+ts+"&lastSeconds="+lastSeconds+"&refreshInterval="+refreshInterval+"&displayType=s";
	}
	var refreshInterval = document.getElementById("refreshInterval").value;
	refreshInterval = refreshInterval * 1000;
	setTimeout('page_refresh()',refreshInterval); //指定１秒刷新一次
</script>
</html>
