def call(execScript, image, containerName, workspaceDirectory ){

    def shellScript = '''
        docker stop $containerName || true
        docker rm $containerName || true
    
        docker run -d --name $containerName --volume=$workspaceDirectory:/workspace $image
    
        docker exec $containerName bash -c \"$execScript\"
        docker stop $containerName
    '''
    sh shellScript;

}