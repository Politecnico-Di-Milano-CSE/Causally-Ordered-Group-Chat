#include "GenericNode.h"
#include "VectorClockMsg_m.h"
#include <omnetpp.h>

Define_Module(GenericNode);

void GenericNode::initialize() {
    // Creates a self-invocation message
    cMessage *timerMessage = new cMessage("createRoomTimer");

    // Schedules the first message to be sent after 10 seconds
    scheduleAt(simTime() + 5 + uniform(1, 2), timerMessage);
}

void GenericNode::handleMessage(cMessage *msg) {
    // Checks if the received message is the self-invocation message
    if (strcmp(msg->getName(), "createRoomTimer") == 0) {
        // Creates a room
        createRoom("Room_" + std::to_string(simTime().dbl()));

        // Schedules the next self-invocation message to be sent after 10 seconds
        scheduleAt(simTime() + 10 + uniform(1, 2), msg);
    } else {
        VectorClockMsg *vcMsg = check_and_cast<VectorClockMsg *>(msg);
        if (msg->isName("Broadcast message")) {
            // Ignores the broadcast message if it comes from the same node
            if (vcMsg->getSrcNode() != getIndex()) {
                // Handles the broadcast message
                handleBroadcastMessage(vcMsg);
            }
        } else {
            // Handles other types of messages here
            // ...
        }
    }
}

void GenericNode::createRoom(std::string roomName) {
    // Gets the number of nodes in the system
    int numNodes = getAncestorPar("n");

    // Generates a random node index
    int nodeIndex = intuniform(0, numNodes - 1);

    // Checks if the current node matches the randomly chosen index
    if (getIndex() == nodeIndex) {
        // Gets the chat rooms associated with this node index
        auto& rooms = chatRoom[nodeIndex];

        // Checks if the room with the given name already exists
        if (rooms.find(roomName) == rooms.end()) {
            // Generates a random number of participants for the room
            int numParticipants = intuniform(1, 10);

            // Creates a vector to store participant information
            std::vector<int> participants;
            for (int i = 0; i < numParticipants; ++i) {
                // Clock values for each participant initialized to zero
                participants.push_back(0);
            }

            // Adds the room with its clock or participant value to the chat room map
            rooms[roomName] = participants;

            // Prints a message indicating that the room has been created
            EV << "Room " << roomName << " created with " << numParticipants << " participants.\n";

            // Creates a new message to be broadcasted
            VectorClockMsg *msg = new VectorClockMsg("Broadcast message");
            // Sets the size of the vector clock in the message
            msg->setVectorClockArraySize(numParticipants);
            // Sets the values of the vector clock in the message
            for (int i = 0; i < numParticipants; i++) {
                msg->setVectorClock(i, participants[i]);
            }
            // Sets the source node of the message
            msg->setSrcNode(getIndex());
            // Sets the room name
            msg->setRoomName(roomName.c_str());
            // Sends the message to all outgoing gates except the last one
            for (int i=0; i<gateSize("g$o")-1; i++) {
                send(msg->dup(), "g$o", i);
            }
            // Deletes the message as it has been sent
            delete msg;
        } else {
            // Prints a message indicating that the room already exists
            EV << "Room " << roomName << " already exists.\n";
        }
    }
}


void GenericNode::deleteRoom(std::string roomName) {
    // Search for the chat room by name and delete it if it exists
    for (auto &nodeChatRooms : chatRoom) {
        auto &rooms = nodeChatRooms.second;
        auto it = rooms.find(roomName);
        if (it != rooms.end()) {
            // Erase the room from the map
            rooms.erase(it);
            // Output a message indicating the deletion of the room
            EV << "Room " << roomName << " deleted.\n";
            return; // Exit the function after deleting the room
        }
    }
    // Output a message if the room does not exist
    EV << "Room " << roomName << " does not exist.\n";
}

// Function to get the index of the output gate connecting to a specific destination node, in a dynamic manner
int GenericNode::getOutputGateIndex(int srcNodeIndex, int destNodeIndex) {
    // Get the number of output gates for this node
    int numOutputGates = gateSize("g$o");

    // Iterate through all output gates
    for (int i = 0; i < numOutputGates; ++i) {
        cGate* outputGate = gate("g$o", i);
        // Check if the gate is connected and leads to the destination node
        if (outputGate && outputGate->getNextGate() && outputGate->getNextGate()->getOwnerModule()->getIndex() == destNodeIndex) {
            // Return the index of the matching output gate
            return i;
        }
    }
    // Return -1 if no matching output gate is found
    return -1;
}

// Function to handle broadcast messages received by the node
void GenericNode::handleBroadcastMessage(VectorClockMsg *msg) {
    // Get the name of the chat room from the message
    std::string roomName = msg->getRoomName();
    // Get the index of the source node from the message
    int srcNodeIndex = msg->getSrcNode();
    // Create a vector to store participant information
    std::vector<int> participants;

    // Extract participant information from the message
    for (int i = 0; i < msg->getVectorClockArraySize(); i++) {
        participants.push_back(msg->getVectorClock(i));
    }

    // Update the chat room information for the source node
    chatRoom[srcNodeIndex][roomName] = participants;

    // Output a message showing the updated chat room information for the node
    EV << "ChatRoom update for node " << getIndex() << ": ";
    for (auto& nodeEntry : chatRoom) {
        EV << "{" << nodeEntry  .first << ":{";
        for (auto& roomEntry : nodeEntry.second) {
            EV << roomEntry.first << ":[";
            for (size_t i = 0; i < roomEntry.second.size(); ++i) {
                EV << roomEntry.second[i];
                if (i < roomEntry.second.size() - 1) EV << ", ";
            }
            EV << "]}";
        }
        EV << "}";
    }
    EV << "\n";
}

void GenericNode::incrementLocalClock(std::string roomName, int participantIndex) {
    // Find the chat rooms associated with this node index
    auto it = chatRoom.find(getIndex());
    if (it != chatRoom.end()) {
        auto& rooms = it->second;
        auto roomIt = rooms.find(roomName);
        if (roomIt != rooms.end()) {
            // Increment the value of the clock for the i-th participant
            roomIt->second[participantIndex]++;
        } else {
            // Output a message if the room does not exist
            EV << "Room " << roomName << " does not exist.\n";
        }
    } else {
        // Output a message if the node has no rooms
        EV << "Node " << getIndex() << " has no rooms.\n";
    }
}

void GenericNode::updateClock(std::string roomName, VectorClockMsg *msg) {
    // Find the chat rooms associated with this node index
    auto it = chatRoom.find(getIndex());
    if (it != chatRoom.end()) {
        auto& rooms = it->second;
        auto roomIt = rooms.find(roomName);
        if (roomIt != rooms.end()) {
            // Get the local clock and the message clock
            std::vector<int>& localClock = roomIt->second;
            std::vector<int> msgClock;
            for (size_t i = 0; i < msg->getVectorClockArraySize(); i++) {
                msgClock.push_back(msg->getVectorClock(i));
            }

            // Check if the local clock value is less than the message clock value
            for (size_t i = 0; i < localClock.size(); i++) {
                if (localClock[i] < msgClock[i]) {
                    // Set the local clock value to the maximum of the two clock values
                    localClock[i] = std::max(localClock[i], msgClock[i]);
                }
            }

            // Increment the local clock value for the current node
            localClock[getIndex()]++;
        } else {
            // Output a message if the room does not exist
            EV << "Room " << roomName << " does not exist.\n";
        }
    } else {
        // Output a message if the node has no rooms
        EV << "Node " << getIndex() << " has no rooms.\n";
    }
}
