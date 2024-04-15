package it.polimi.chat.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VectorClock implements Serializable {
    private Map<String, Integer> clock;

    public VectorClock(Set<String> participants) {
        this.clock = new HashMap<>();
        initializeClockForParticipants(participants);
    }

    private void initializeClockForParticipants(Set<String> participants) {
        for (String participant : participants) {
            clock.put(participant, 0);
        }
    }

    public void incrementLocalClock(String userId) {
        clock.compute(userId, (k, currentTimestamp) -> currentTimestamp != null ? currentTimestamp + 1 : 0);
    }

    public Map<String, Integer> getClock() {
        return clock;
    }

    public void updateClock(Map<String, Integer> receivedClock, String currentUser) {
        for (Map.Entry<String, Integer> entry : receivedClock.entrySet()) {
            String userId = entry.getKey();
            Integer remoteTimestamp = entry.getValue();
            Integer localTimestamp = clock.get(userId);
            if (localTimestamp == null || remoteTimestamp >= localTimestamp) {
                clock.put(userId, remoteTimestamp);
            }
        }
    }

    public boolean isClockLocallyUpdated(Map<String, Integer> receivedClock) {
        boolean isUpdated = true;
        for (Map.Entry<String, Integer> entry : receivedClock.entrySet()) {
            String userId = entry.getKey();
            Integer remoteTimestamp = entry.getValue();
            Integer localTimestamp = clock.getOrDefault(userId, 0);
            if (remoteTimestamp > localTimestamp) {
                isUpdated = false;
                break;
            }
        }
        return isUpdated;
    }

    public void printVectorClock() {
        System.out.println("Vector Clock:");
        for (Map.Entry<String, Integer> entry : clock.entrySet()) {
            System.out.println("- User ID: " + entry.getKey() + ", Timestamp: " + entry.getValue());
        }
    }

}
