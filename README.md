# Distributed Group Chat Application

## Overview
This project is a distributed group chat application designed to ensure causal ordering of messages and high availability, without depending on any centralized server. Users can create and delete chat rooms, with a specified set of participants at creation time. The application supports posting new messages within these rooms, ensuring messages are delivered in causal order. It's built to operate fully distributed, enabling users to read and write messages even during temporary network disconnections.

## Features
- **Distributed Architecture**: No centralized server is required; clients communicate directly.
- **Room Management**: Users can create and delete rooms. Each room's participant list is fixed at creation.
- **Causal Message Ordering**: Ensures that messages within a room are delivered in the order they were sent, respecting causality.
- **High Availability**: Users can interact with the chat, reading and writing messages, even when offline.

## Assumptions
- Clients and network links are reliable.
- Clients can join and leave the network at any time.

## Getting Started

### Prerequisites
- Java SDK (for the Java implementation) or OmNet++ (for simulation)
- Git

### Installation
1. Clone the repository:
git clone https://github.com/yourgithubusername/distributed-group-chat.git

2. Navigate to the project directory:
cd distributed-group-chat

3. Follow the installation steps for either the Java implementation or OmNet++ simulation, as detailed in their respective directories.

### Usage
- To create a room, run...
- To send a message in a room, run...

## Implementation Details
The application uses a vector clock algorithm to ensure causal ordering of messages. Each client maintains a vector clock that is updated upon sending or receiving messages, allowing the system to determine the causal relationships between messages.

### Architecture
- **Client**: Each user runs a client instance, responsible for sending and receiving messages.
- **Message Protocol**: Custom protocol for message passing, ensuring reliability and causal ordering.

## Contributing
We welcome contributions! Please read `CONTRIBUTING.md` for guidelines on how to contribute to this project.

## License
This project is licensed under the MIT License - see the `LICENSE` file for details.

## Acknowledgments
- Inspiration from distributed systems principles and causal ordering mechanisms.
- Thanks to all contributors who have helped with testing, documentation, and adding new features.
