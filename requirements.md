
# Project Requirements: Highly Available, Causally Ordered Group Chat

## System Design:

### Distributed Peer-to-Peer Architecture:
- Each client operates as both a server and a client, forming a peer-to-peer network.
- There is no centralized server; clients communicate directly with each other.

### Room and User Management:
- A room is created with an initial set of participants, which does not change over time.
- Users can create and delete rooms. Room metadata is stored locally.

### Message Exchange:
- Users can post messages to rooms they are a part of.
- Messages are broadcast to all room participants.

## Networking:

### Network Discovery and Membership:
- Implement discovery for peers (e.g., multicast or peer discovery protocol).
- Each peer maintains a list of active participants for each room.

### Reliable Communication:
- Implement acknowledgments for message delivery to ensure no messages are missed.

## Data Structures:

### Message Store:
- Store messages with timestamps or vector clocks for causal ordering.
- Use a message queue for each room to ensure ordered delivery.

### Vector Clocks:
- Use vector clocks for each message to maintain causal order.

## Algorithms:

### Causal Ordering:
- Use vector clocks to implement causal ordering.
- Ensure all causally preceding messages have been delivered before delivering a message.

### Conflict Resolution:
- Implement a mechanism to resolve conflicts from out-of-order messages.

## Fault Tolerance and Availability:

### Handling Disconnections:
- Implement local caching of messages.
- Use a local outbox to store messages and send them when the network is available.

### State Preservation:
- Persist room state and messages.

### Joining and Leaving:
- Implement synchronization when a client joins.
- Detect disconnections through a timeout mechanism.

## Implementation Steps:

### Set Up Project Structure:
- Use Maven for project building and dependencies.

### Define Protocols:
- Define the protocol for message exchange and room management.

### Implement Peer-to-Peer Networking:
- Establish connections using Java networking APIs.

### Implement Room and User Management:
- Develop logic for managing rooms and users.

...

## Simulation Alternative:

If opting to simulate the application using OmNet++, you'll need to:
- Define the network topology.
- Implement the logic for message broadcasting and causal ordering.
