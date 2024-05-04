package it.polimi.chat.dto;

import java.io.Serializable;
import java.util.Map;

public class LoggedMessage implements Comparable<LoggedMessage>, Serializable  {
    public String content;
    public String userid;
    public Map<String, Integer> clock;
    public boolean ischeckpoint;
    @Override
    public int compareTo(LoggedMessage remote) {


         for(String id:clock.keySet()) {
             if (clock.get(id) < remote.clock.get(id)) {
                 if (!remote.userid.equals(id)) {
                         return (-1);
                 } else{
                     if (clock.get(userid) != remote.clock.get(remote.userid)+1 && clock.get(userid)+1 != remote.clock.get(userid)) {
                         return (-1);
                     }
                     }
             }
             if(clock.get(id)>remote.clock.get(id)) {
                 if (!userid.equals(id)) {
                     return (1);
                 } else {
                     if (clock.get(userid) != remote.clock.get(remote.userid)+1 && clock.get(userid)+1 != remote.clock.get(userid)) {
                         return (1);
                     }
                 }
             }
             }

             return 0;
         }
}
