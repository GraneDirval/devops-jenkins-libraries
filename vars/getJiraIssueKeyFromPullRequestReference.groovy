import hudson.*
import hudson.model.*

def call(String sourceReference) {

    def expression = (sourceReference =~ /refs\/heads\/(.*)/)

    if (expression.find()) {
        return expression.group(1);
    } else {
        return null;
    }
}
