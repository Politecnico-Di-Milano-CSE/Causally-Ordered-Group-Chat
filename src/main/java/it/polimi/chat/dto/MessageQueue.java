package it.polimi.chat.dto;

import it.polimi.chat.dto.message.RoomMessage;
import org.apache.commons.collections4.BidiMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageQueue {
    private BidiMap<String,String> participants;
    private Map<String, ArrayList<LoggedMessage>> messageLog;
    private ArrayList <Integer> checkpoint;
    private VectorClock localVectorClock;

    public MessageQueue(BidiMap<String,String> participants) {
        this.participants = participants;
        messageLog = new HashMap<String, ArrayList<LoggedMessage>>();
        checkpoint = new ArrayList<>();
        checkpoint.add(0);
        this.localVectorClock= new VectorClock(participants.keySet());
        for (String id : participants.keySet()) {
            messageLog.put(id,new ArrayList<LoggedMessage>());
        }
    }
    public void addMessageToLog (RoomMessage message) {
        LoggedMessage msg= new LoggedMessage();
        msg.content=message.getContent();
        msg.userid=message.getUserID();
        msg.clock=message.getVectorClock().getClock();
        messageLog.get(message.getUserID()).add(msg);
    }
    public void updatelog (ArrayList<LoggedMessage>  trimmedLog, Integer remoteCheckpoint){
        Integer i = remoteCheckpoint;
        int j=0;
        for (j =0;j< trimmedLog.size() && i< messageLog.size();j++){
            System.out.println("do i get heredcc");
            switch (trimmedLog.get(j).compareTo(messageLog.get(i))){
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
                 System.out.println(participants.get(trimmedLog.get(j).userid)+": " + trimmedLog.get(j).content);
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
    public Integer getLastCheckpoint(){
        return checkpoint.size()-1;
    }
    public Integer getLastCheckpointIndex(){
            return checkpoint.get(checkpoint.size()-1);
    }
    public void updateLocalClock(VectorClock vectorClock){
        localVectorClock = vectorClock;
    }
    public VectorClock getLocalVectorClock(){
        return localVectorClock;
    }

}