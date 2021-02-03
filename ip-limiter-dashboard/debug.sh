#以Apollo做为配置中心的启动方式
#java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Dspring.profiles.active=DEV -Dapollo.meta=http://apollo.dev.xxx.com:8072 -jar target/ip-limiter-dashboard.jar 
java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar target/ip-limiter-dashboard.jar 
