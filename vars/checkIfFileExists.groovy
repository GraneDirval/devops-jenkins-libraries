Boolean call(filePath){

    def files = findFiles filePath
    boolean exists = files.length > 0

    return  exists;
}