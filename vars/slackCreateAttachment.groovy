import groovy.json.*

def call(pretext, fallback, color, fields) {
  def attachmentPayload = [
      [
          pretext : pretext,
          fallback: fallback,
          color   : color,
          fields  : fields
      ]]




  return new JsonBuilder(attachmentPayload).toPrettyString();

}