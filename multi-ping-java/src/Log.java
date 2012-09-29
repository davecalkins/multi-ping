import java.text.DateFormat;
import java.util.Date;

public class Log
{
    synchronized public static void log(String logtext)
    {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                       DateFormat.SHORT);
        System.out.println(df.format(new Date()) + ", " + logtext);
    }
}