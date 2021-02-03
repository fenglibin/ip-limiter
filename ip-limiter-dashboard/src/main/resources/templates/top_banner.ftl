选择应用：<select id="appNameId" onchange="changeApp(this.value)">
</select>
<input type="hidden" name="appName" id="appName" value="${appName!''}">
<hr />
<a href="/limiter/getMinutesDataPretty<#if appName??>?appName=${appName}</#if>">分钟纬度IP访问TOP统计</a>&nbsp;&nbsp;|&nbsp;&nbsp;
<a href="/limiter/getIpSecondAccessPretty<#if appName??>?appName=${appName}</#if>">秒钟纬度IP访问TOP统计</a>&nbsp;&nbsp;|&nbsp;&nbsp;
<a href="/limiter/whiteIp/getAllWhiteIpsPretty<#if appName??>?appName=${appName}</#if>">IP白名单列表</a>&nbsp;&nbsp;|&nbsp;&nbsp;
<a href="/limiter/blackIp/getAllBlackIpsPretty<#if appName??>?appName=${appName}</#if>">IP黑名单列表</a>&nbsp;&nbsp;|&nbsp;&nbsp;
<a href="/limiter/qpsLimit/resetLimitPretty<#if appName??>?appName=${appName}</#if>">设置单个IP每秒访问的最大请求数</a>
<hr />
<script>
	function changeApp(appName){
		if(appName==null || appName==""){
			return;
		}
		window.location.href="/limiter/getMinutesDataPretty?appName="+appName;
	}
	function loadAllApps(){
		var xhr = new XMLHttpRequest();
		var url = "/limiter/getAllAppNames";
		xhr.open('GET', url, true);
    	xhr.onreadystatechange = function() {
			// readyState == 4说明请求已完成
			if (xhr.readyState == 4 && xhr.status == 200 || xhr.status == 304) { 
				// 将从服务器获得数据转为JSON
				var obj = JSON.parse(xhr.responseText);
				var appName = document.getElementById("appName").value;
				for(var i=0; i<obj.length;i++){
					var opt = new Option(obj[i],obj[i]);
					if(appName==obj[i]){
						opt.selected='selected';
					}
					document.getElementById("appNameId").options.add(opt);
				}
			}
    	};
    	xhr.send();
	}
	loadAllApps();
</script>