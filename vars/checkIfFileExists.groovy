Boolean call(filePath){

    def files = findFiles glob: filePath
    boolean exists = files.length > 0

    return  exists;
}