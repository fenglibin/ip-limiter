<script language="JavaScript">
	function loadAllAppNodes(){
		var appName = document.getElementById("appName").value;
		var xhr = new XMLHttpRequest();
		var url = "/limiter/getAppRegisteredIps?appName="+appName;
		xhr.open('GET', url, true);
    	xhr.onreadystatechange = function() {
			// readyState == 4说明请求已完成
			if (xhr.readyState == 4 && xhr.status == 200 || xhr.status == 304) { 
				// 将从服务器获得数据转为JSON
				var obj = JSON.parse(xhr.responseText);
				var clientIp = document.getElementById("clientIp").value;
				for(var i=0; i<obj.length;i++){
					var opt = new Option(obj[i],obj[i]);
					if(clientIp==obj[i]){
						opt.selected='selected';
					}
					document.getElementById("ipId").options.add(opt);
				}
			}
    	};
    	xhr.send();
	}
	loadAllAppNodes();
</script>