1. Install npm distribution
2. Run npm install --global gulp-cli
3. Run npm install --global bower
4. Run npm install
5. Run bower install

6. Install Postgresql database 9.4, Java 8, IntelliJ IDEA with Kotlin support
7. Restore db from {projectRoot}/backups/db.dmp file using pg_restore Ex. pg_restore -U some_user -d some_db_name db.dmp 
8. Rename file {projectRoot}/back/src/main/resources/application-default.properties.template to application-default.properties and adjust properties for your development environment.
9. Install nginx. Use file {projectRoot}/conf/template/default.dev.template as main nginx.conf file
10. Adjust following settings in nginx configuration:
 Set port in server rule listen 9090 to free port in your local environment
 Set path in server root rule to your output folder of gulp build
 Set proxy_pass in location /data/ rule to your backend server /data/ handler url

To run frontend environment:
1. Move to {projectRoot}/front
2. Run *gulp watch* or *gulp watch --app {appName}*
Valid app names are: 'control' (default), 'school'

To run backend environment:
1. Run KidCenterApplication.kt