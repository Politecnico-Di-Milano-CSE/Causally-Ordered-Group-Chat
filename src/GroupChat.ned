//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see http://www.gnu.org/licenses/.
// 

package groupchat;

network DistributedChat
{
    parameters:
        int numNodes = default(5);
    submodules:
        node[numNodes]: GenericNode {
            @display("p=100,300,ring,150");
        }
    connections allowunconnected:
        for i=0..numNodes-2, for j=i+1..numNodes-1 {
            node[i].gate++ <--> node[j].gate++;
        }
}