package by.kutsko.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogHelper {

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
