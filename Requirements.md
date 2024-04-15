Project Requirements and Areas for Improvement
Implementation Requirements
1. Participant Management in Rooms:
- We need to implement a functionality in order to register the users in the Rooms, so that
  is possible to retrieve how many users are connected in the current room, the userId of the 
  participants and other attributes
2. Message Posting and Causal Delivery:
- We need to implement a mechanism that allows "delayed" messages to be sent to unsynchronized users
3. Handling Disconnections and Reconnections:
- We need to implement a strategy for handling user disconnections and reconnections to ensure
  high availability. This could include caching messages locally when a user is disconnected and
  synchronizing missed messages upon reconnection in a way that respects causal ordering.
4. Simulation or Real Distributed Application:
- We need to decide whether to simulate the network behavior using OmNet++ or implement it as
  a real distributed application. If the latter, we need to ensure that our network communication code
  is robust and efficient for a P2P environment.
  Project Requirements and Areas for Improvement
  Areas for Improvement
1. Efficiency and Scalability:
- We need to review and optimize the handling of vector clocks and message passing to ensure
  the system scales well with an increasing number of users and chat rooms.
2. Error Handling and Logging:
- We need to improve error handling for network operations and other critical paths. Implement
  comprehensive logging for easier debugging and monitoring.
3. Security Considerations:
- We need to implement authentication for users to ensure that only authorized users can join
  rooms and send messages. Consider encrypting messages between users to ensure privacy and
  security in communications.
4. User Interface and Experience:
- We need to develop a simple CLI (Command Line Interface) or GUI (Graphical User Interface) to
  improve usability.
5. Testing and Validation:
- We need to implement unit tests and integration tests to validate the functionality and reliability of
  our system, especially the causal ordering of messages and the system's behavior under
  disconnections.
  Project Requirements and Areas for Improvement
6. Documentation:
- We need comprehensive documentation covering the architecture, setup, usage, and any
  assumptions made during the development process.