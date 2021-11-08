// SPDX-License-Identifier: GPL-3.0
pragma solidity >=0.4.22 <0.9.0;
pragma experimental ABIEncoderV2;

contract AISChain {

    struct AISData {
        address sender;
        string timestamp;
        string shipid;
        string lon;
        string lat;
        string heading;
        string course;
        string speed;
        string shiptype;
        string destination;
    }

    AISData[] private AISDataArray;

    function addAISData(string memory ts, string memory shipid, string memory lon, string memory lat, string memory heading, string memory course, string memory speed, string memory shiptype, string memory destination) public {
        AISDataArray.push(AISData(msg.sender, ts, shipid, lon, lat, heading, course, speed, shiptype, destination));
    }

    function getAISData() public view returns(AISData[] memory) {
        return AISDataArray;
    }
}