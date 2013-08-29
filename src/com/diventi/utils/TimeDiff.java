package com.diventi.utils;

import java.util.Date;

public class TimeDiff {

  public static long minutesSince(long from) {

    Date now = new Date();
    long seconds = Math.round( Math.abs(now.getTime() - from)/1000 );
    
    return seconds/60;
  }
  
  public static String timeAgo(long from) {

    String[] periods = new String[] { "segundo", "minuto", "hora", "dia", "semana", "mes", "año", "década" };
    double[] lengths = new double[] {        60,      60,     24,      7,     4.35,   12,    10 };

    Date now = new Date();
    double difference = (now.getTime() - from)/1000;
    
    int j = 0;
    for(j=0; difference >= lengths[j] && j<lengths.length; j++)
      difference /= lengths[j];
    
    long ldifference = (long)Math.round(difference);

    if(j==0)
      return "Recién actualizado";
    
    return String.format("Actualizado hace %d %s%s", ldifference, periods[j], ldifference != 1 ? "s" : "");
  }
  
}
