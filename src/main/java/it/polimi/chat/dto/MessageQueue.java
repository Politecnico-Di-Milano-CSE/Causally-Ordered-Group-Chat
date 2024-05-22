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
    public void updatelog (Map<String,ArrayList<LoggedMessage>>  trimmedLog, VectorClock remoteClock){
        for (Map.Entry<String,ArrayList<LoggedMessage>> entry : trimmedLog.entrySet()) {
            ArrayList<LoggedMessage> localUserLog = messageLog.get(entry.getKey());
            ArrayList<LoggedMessage> remoteUserLog= trimmedLog.get(entry.getKey());
            for (int i=0; i<remoteUserLog.size();i++) {
                if (remoteUserLog.get(i).clock.get(remoteUserLog.get(i).userid)>localUserLog.size()) {
                    localUserLog.add(remoteUserLog.get(i));
                }
            }
        }

    }
public void dumbPrintLog(){ //todo remove
        for (ArrayList<LoggedMessage> userLog : messageLog.values()) {
            for (LoggedMessage msg : userLog) {
                System.out.println(participants.get(msg.userid) + ": "+ msg.content);
            }
        }
}
public void printLog(){
    for (ArrayList<LoggedMessage> userLog : messageLog.values()) {
        for (LoggedMessage msg : userLog) {
            System.out.println(participants.get(msg.userid) + ": "+ msg.content);
        }
    }
}

    public Map<String, ArrayList<LoggedMessage>> getTrimmedMessageLog(VectorClock remoteVectorClock) { //trims the log message to the desired length, supposed to be used specifically to be sent to other users
        Map<String, ArrayList<LoggedMessage>> trimmedLog = new HashMap<>();
        for (String id : participants.keySet()) {
            trimmedLog.put(id,new ArrayList<>());
        }
        Map<String,Integer> remoteClock= remoteVectorClock.getClock();
        for(Map.Entry<String, Integer> entry : remoteClock.entrySet()){
            ArrayList<LoggedMessage> localUserLog = messageLog.get(entry.getKey());
            if (entry.getValue()<localUserLog.size()) {
                trimmedLog.put(entry.getKey(),(ArrayList<LoggedMessage>) localUserLog.subList(entry.getValue(), localUserLog.size()));
            }
        }
        return trimmedLog;
    }

    public void updateLocalClock(VectorClock vectorClock){
        localVectorClock = vectorClock;
    }
    public VectorClock getLocalVectorClock(){
        return localVectorClock;
    }

}