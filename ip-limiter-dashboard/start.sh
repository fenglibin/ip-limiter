#以Apollo做为配置中心的启动
#java -Dspring.profiles.active=DEV -Dapollo.meta=http://apollo.dev.xxx.com:8072 -jar target/ip-limiter-dashboard.jar
java -jar target/ip-limiter-dashboard.jar
