package it.polimi.chat.dto;

import it.polimi.chat.dto.message.RoomMessage;
import org.apache.commons.collections4.BidiMap;

import java.util.*;

public class MessageQueue {
    private BidiMap<String,String> participants; //userid-username of each participant
    private Map<String, ArrayList<LoggedMessage>> messageLog; //log of the messages of the room (each user gets their own list)
    private VectorClock speciallocalVectorClock; //saves the clock of the room

    public MessageQueue(BidiMap<String,String> participants) {
        this.participants = participants;
        messageLog = new HashMap<String, ArrayList<LoggedMessage>>();
        for (String id : participants.keySet()) {
            messageLog.put(id,new ArrayList<LoggedMessage>());
        }
    }
    public void addMessageToLog (RoomMessage message) { //once checked that the node is up to date with the message add it to the log of the user
        LoggedMessage msg= new LoggedMessage();
        msg.content=message.getContent();
        msg.userid=message.getUserID();
        msg.clock= new HashMap<>(message.getVectorClock().getClock());
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
public void printLog(){
        Map <String, Integer> logChecks = new HashMap<>();
        boolean printdone= false;
    for (String id : participants.keySet()) { //creates a map that stores all the indexes to make sure every message gets printed
        logChecks.put(id,0);
        }
    int i=0;
    while (!printdone && i<10){
        printdone= true;
        i++;
        for (Map.Entry<String,ArrayList<LoggedMessage>> entry: messageLog.entrySet()) { //stays on a single user until it prints all of their messsages or it sees a message that was has a vector clock that registers another message being sent in between
            ArrayList<LoggedMessage> localUserLog = entry.getValue();
            String username = participants.get(entry.getKey());
            Integer checkedlog =logChecks.get(entry.getKey());
            while (checkedlog< localUserLog.size()) { //if we printed all the messages of the user skip to the next one
                printdone= false;
                LoggedMessage currentMsg = localUserLog.get(checkedlog);
                if (compareLogClocks(currentMsg, logChecks)) { //the logchecks function as a list of the previous message seen and a vectorclock so that causal ordering is respected even in printing
                    System.out.println( username + ": " + localUserLog.get(logChecks.get(entry.getKey())).content);
                    logChecks.put(entry.getKey(), checkedlog+1);
                    checkedlog =logChecks.get(entry.getKey());}
                else{ //skips to the next user
                    break;
                }
            }
        }
}
}

    public Map<String, ArrayList<LoggedMessage>> getTrimmedMessageLog(VectorClock remoteVectorClock) { //trims the log message to the desired length for each user, supposed to be used specifically to be sent to other users
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
        speciallocalVectorClock = vectorClock;
    }
    public VectorClock getLocalVectorClock(){
        return speciallocalVectorClock;
    }

    public Boolean compareLogClocks (LoggedMessage currentmessage, Map<String, Integer>  previousclock){
        for (Map.Entry<String,Integer> entry : currentmessage.clock.entrySet()) {
            if ((!currentmessage.userid.equals(entry.getKey())) && entry.getValue()>previousclock.get(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

}

