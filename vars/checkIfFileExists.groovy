Boolean call(filePath){

    def res = ("test -f ${filePath} && echo '1' || echo '0' ".execute().text)

    boolean exists = (res == "1")

    return  exists;


}