/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is.glitch.colloquium.main;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author glitch
 */
public class Utils {
	public static String joinString(Collection<?> col, String delim) {
	    StringBuilder sb = new StringBuilder();
	    Iterator<?> iter = col.iterator();
	    if (iter.hasNext())
		sb.append(iter.next().toString());
	    while (iter.hasNext()) {
		sb.append(delim);
		sb.append(iter.next().toString());
	    }
	    return sb.toString();
	}	

	public static String time(){
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(cal.getTime());
	}

	public static String escapeHTML(String s) {
	    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
	    for (int i = 0; i < s.length(); i++) {
		char c = s.charAt(i);
		if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
		    out.append("&#");
		    out.append((int) c);
		    out.append(';');
		} else {
		    out.append(c);
		}
	    }
	    return out.toString();
	}
}
