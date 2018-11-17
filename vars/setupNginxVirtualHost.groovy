def call(APP_ID) {
    def stageUrlPrefix = APP_ID.toLowerCase()
    def stageUrl = stageUrlPrefix + ".jenkins.playwing.com"
    def nginxConfigFile = "/etc/nginx/branches/$APP_ID"
    def branchDigitId

    def pattern = /\d+/
    def expression = (APP_ID =~ pattern)

    if (expression.find()) {
        branchDigitId = expression.group(0).toInteger();
    } else {
        branchDigitId = 0
    }
    def webserverPort = 49152 + branchDigitId;
    expression = null;

    sh """
       echo '
           server {
    
               listen 80;
               listen [::]:80;
               server_name $stageUrl;
    
               location / {
                   proxy_set_header Host \$host;
                   proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
                   proxy_pass http://127.0.0.1:$webserverPort;               
               }
           }
       '> $nginxConfigFile
       sudo /etc/init.d/nginx reload
       """
    return true;
}