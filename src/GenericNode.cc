#include "GenericNode.h"

Define_Module(GenericNode);

void GenericNode::initialize()
{

}

void GenericNode::handleMessage(cMessage *msg)
{

}

void GenericNode::createRoom(std::string roomName)
{
    // Create a new chat room if it doesn't already exist
    if (chatRooms.find(roomName) == chatRooms.end()) {
        chatRooms[roomName] = std::vector<int>();
        EV << "Room " << roomName << " created.\n";
    } else {
        EV << "Room " << roomName << " already exists.\n";
    }
}


void GenericNode::deleteRoom(std::string roomName)
{
    // Delete a chat room if it exists
    auto it = chatRooms.find(roomName);
    if (it != chatRooms.end()) {
        chatRooms.erase(it);
        EV << "Room " << roomName << " deleted.\n";
    } else {
        EV << "Room " << roomName << " does not exist.\n";
    }
}
