Boolean call(filePath){

    def res = sh(script: "test -f ${filePath} && echo '1' || echo '0' ", returnStdout: true).trim()

    boolean exists = (res == 1)

    return  exists;


}