package chat.util;

import java.util.Map;

public class CausalOrdering {

    // Increment the vector clock for the current user
    public static void incrementClock(Map<String, Integer> vectorClock, String userId) {
        vectorClock.put(userId, vectorClock.getOrDefault(userId, 0) + 1);
    }

    // Update the local vector clock with the received message's vector clock
    public static void updateClock(Map<String, Integer> localClock, Map<String, Integer> receivedClock, String userId) {
        for (Map.Entry<String, Integer> entry : receivedClock.entrySet()) {
            String entryUserId = entry.getKey();
            localClock.put(entryUserId, Math.max(localClock.getOrDefault(entryUserId, 0), entry.getValue()));
        }
        incrementClock(localClock, userId); // Correctly increment the clock for the local user
    }

    // Method to compare two vector clocks for causal ordering
    public static boolean isCausallyOrdered(Map<String, Integer> clock1, Map<String, Integer> clock2) {
        // Check if clock1 is causally ordered before clock2
        boolean allLessOrEqual = true;
        boolean anyLess = false;

        for (Map.Entry<String, Integer> entry : clock1.entrySet()) {
            int clock1Time = entry.getValue();
            int clock2Time = clock2.getOrDefault(entry.getKey(), 0);
            if (clock1Time > clock2Time) {
                return false;
            }
            if (clock1Time < clock2Time) {
                anyLess = true;
            }
        }

        // clock1 is causally ordered before clock2 if it is less than or equal for all
        // entries
        // and less for at least one entry.
        return allLessOrEqual && anyLess;
    }
}
