package it.polimi.chat.dto;

import it.polimi.chat.dto.message.RoomMessage;
import org.apache.commons.collections4.BidiMap;

import java.util.*;

public class MessageQueue {
    private BidiMap<String,String> participants;
    private Map<String, ArrayList<LoggedMessage>> messageLog;
    private VectorClock localVectorClock;

    public MessageQueue(BidiMap<String,String> participants) {
        this.participants = participants;
        messageLog = new HashMap<String, ArrayList<LoggedMessage>>();
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
    public void updatelog (Map<String,ArrayList<LoggedMessage>>  trimmedLog){
        for (Map.Entry<String,ArrayList<LoggedMessage>> entry : trimmedLog.entrySet()) {
            ArrayList<LoggedMessage> localUserLog = messageLog.get(entry.getKey());
            ArrayList<LoggedMessage> remoteUserLog= trimmedLog.get(entry.getKey());
            for (int i=0; i<remoteUserLog.size();i++) {
                if (remoteUserLog.get(i).clock.get(remoteUserLog.get(i).userid)>localUserLog.size()) {
                    localUserLog.add(remoteUserLog.get(i));
                    System.out.println(participants.get(remoteUserLog.get(i).userid) + ":"+ remoteUserLog.get(i).content);
                }
            }
        }

    }
public void dumbPrintLog(){ //todo remove
        for (ArrayList<LoggedMessage> userLog : messageLog.values()) {
            for (LoggedMessage msg : userLog) {
                debugprint(msg.clock);
                System.out.println(participants.get(msg.userid) + ": "+ msg.content);
            }
        }
}
public void printLog(){
        Map <String, Integer> logChecks = new HashMap<>();
        boolean printdone= false;
    for (String id : participants.keySet()) {
        logChecks.put(id,0);
        }
    int i=0;
    while (!printdone && i<10){
        printdone= true;
        i++;
        System.out.println("printdone true");
        for (Map.Entry<String,ArrayList<LoggedMessage>> entry: messageLog.entrySet()) {
            ArrayList<LoggedMessage> localUserLog = entry.getValue();
            String username = participants.get(entry.getKey());
            Integer checkedlog =logChecks.get(entry.getKey());
            System.out.println("local user size :" + localUserLog.size()+ username + "check log :" + checkedlog);
            while (checkedlog< localUserLog.size()) {
                printdone= false;
                LoggedMessage currentMsg = localUserLog.get(checkedlog);
                if (compareLogClocks(currentMsg, logChecks)) {
                    System.out.println("i got to the compare log clocks");
                    System.out.println( participants.get(entry.getKey()) + ": " + localUserLog.get(logChecks.get(entry.getKey())).content);
                    logChecks.put(entry.getKey(), checkedlog+1);
                    checkedlog =logChecks.get(entry.getKey());
                    System.out.println("updated log clocks hopefully:" + logChecks.get(entry.getKey()));
                } else{
                    System.out.println("sei nel break");
                    break;
                }
            }
        }
}
}

    public Map<String, ArrayList<LoggedMessage>> getTrimmedMessageLog(VectorClock remoteVectorClock) { //trims the log message to the desired length, supposed to be used specifically to be sent to other users
        Map<String, ArrayList<LoggedMessage>> trimmedLog = new HashMap<>();
        Map<String,Integer> remoteClock= remoteVectorClock.getClock();
        for(Map.Entry<String, Integer> entry : remoteClock.entrySet()){
            ArrayList<LoggedMessage> localUserLog = messageLog.get(entry.getKey());
            if (entry.getValue()<localUserLog.size()) {
                trimmedLog.put(entry.getKey(),new ArrayList<>(localUserLog.subList(entry.getValue(), localUserLog.size())));
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

    public Boolean compareLogClocks (LoggedMessage currentmessage, Map<String, Integer>  previousclock){
        System.out.println("in log clocks log:" + participants.get(currentmessage.userid)+" content: "+currentmessage.content);
        for (Map.Entry<String,Integer> entry : currentmessage.clock.entrySet()) {
            System.out.println(participants.get(entry.getKey())+ "current:"+ entry.getValue()+"checks:"+ previousclock.get(entry.getKey()));
            if ((!currentmessage.userid.equals(entry.getKey())) ) {
                if (entry.getValue()>previousclock.get(entry.getKey())) {
                    return false;
                }
            }
        }
        return true;
    }
    public void debugprint(Map<String,Integer> clock){
        System.out.println("Vector Clock:");
        for (Map.Entry<String, Integer> entry : clock.entrySet()) {
            System.out.println("- User ID: " + participants.get(entry.getKey()) + ", Timestamp: " + entry.getValue());
        }
    }
    }
