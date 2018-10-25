def call(){
    sh 'composer install --ignore-platform-reqs --no-scripts --classmap-authoritative --optimize-autoloader --no-suggest'
}