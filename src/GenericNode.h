#ifndef GENERICNODE_H_
#define GENERICNODE_H_

#include <omnetpp.h>

using namespace omnetpp;

class GenericNode : public cSimpleModule
{
  private:
    std::map<std::string, std::vector<int>> chatRooms; // Map room name to participant node IDs

  protected:
    virtual void initialize() override;
    virtual void handleMessage(cMessage *msg) override;

  public:
    void createRoom(std::string roomName);
    void deleteRoom(std::string roomName);
};



#endif
