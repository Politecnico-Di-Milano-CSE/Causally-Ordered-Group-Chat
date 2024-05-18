package it.polimi.chat.dto;

import it.polimi.chat.dto.message.RoomMessage;
import org.apache.commons.collections4.BidiMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MessageQueue  implements Serializable {
    private BidiMap<String,String> participants;
    private ArrayList <LoggedMessage> messageLog;
    private ArrayList <Integer> checkpoint;

    public MessageQueue(BidiMap<String,String> participants) {
        this.participants = participants;
        messageLog = new ArrayList<>();
        checkpoint = new ArrayList<>();
    }
    public void addMessageToLog (RoomMessage message) {
        LoggedMessage msg= new LoggedMessage();
        msg.content=message.getContent();
        msg.userid=message.getUserID();
        msg.ischeckpoint=false;
        msg.clock=message.getVectorClock().getClock();
        int size =messageLog.size(); //saves the index of the msg we need to add

        int i=checkpoint.size()-1;
        int j=0;
            for(String id : participants.keySet()) {
                if (j< message.getVectorClock().getClock().get(id)){
                    j=message.getVectorClock().getClock().get(id);
                }//gets lowest vectorclock
            }
        if (i<j){
            msg.ischeckpoint=true;
            checkpoint.add(size); //logs the checkpoint to the relevant message
        }
        messageLog.add(msg);
    }
    public void updatelog (ArrayList<LoggedMessage>  trimmedLog){
        Integer i = getLastCheckpoint();
        int j=0;
        for (j =0;j< trimmedLog.size() && i< messageLog.size();j++){
                switch (messageLog.get(i).compareTo(trimmedLog.get(j))){
                   case 1:
                       messageLog.add(i,trimmedLog.get(j));
                       System.out.println(participants.get(trimmedLog.get(j).userid)+": " + trimmedLog.get(j).content);
                       i++;
                       if(trimmedLog.get(j).ischeckpoint){
                           checkpoint.add(i);
                       }
                       break;
                       case 0: if(!trimmedLog.get(i).userid.equals(trimmedLog.get(j).userid)){
                           {
                           messageLog.add(i+1,trimmedLog.get(j));
                           System.out.println(participants.get(trimmedLog.get(j).userid)+": " + trimmedLog.get(j).content);
                               i++;
                       }
                           if(trimmedLog.get(j).ischeckpoint){
                           checkpoint.add(i);
                       }
                       }
                       break;
                       case -1:
                           i++;
                           j--;
                       break;
               }
        }
        while (j< trimmedLog.size()){
                    messageLog.add(trimmedLog.get(j));
                if (trimmedLog.get(j).ischeckpoint){
                    checkpoint.add(messageLog.size()-1);
                }
                    j++;
            }
    }

    public ArrayList<LoggedMessage> getMessageLog() {
        return messageLog;
    }
    public ArrayList <LoggedMessage> getTrimmedMessageLog(Integer check) { //trims the log message to the desired length, supposed to be used specifically to be sent to other users
        ArrayList <LoggedMessage> trimmedMessageLog = new ArrayList<>(messageLog.subList(check,messageLog.size()));
        return trimmedMessageLog;
    }

    public ArrayList<Integer> getCheckpoint() {
        return checkpoint;
    }
    public Integer getLastCheckpoint() {

        if (!checkpoint.isEmpty()) {
            return checkpoint.get(checkpoint.size() - 1);
        }else{
            return 0;
        }
    }
}