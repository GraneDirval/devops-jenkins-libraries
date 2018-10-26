import groovy.json.*

def call(fallback, color, fields) {
    def attachmentPayload = [
            [
                    fallback: fallback,
                    color   : color,
                    fields  : fields
            ]]




    return new JsonBuilder(attachmentPayload).toPrettyString();

}